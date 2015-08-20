package cloudos.deploy;

import cloudos.cslib.compute.CsCloud;
import cloudos.cslib.compute.instance.CsInstance;
import cloudos.cslib.compute.instance.CsInstanceRequest;
import cloudos.cslib.compute.mock.MockCsInstance;
import cloudos.model.instance.CloudOsBase;
import cloudos.model.instance.CloudOsState;
import cloudos.model.instance.CloudOsStatusBase;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.cobbzilla.util.system.CommandResult;
import org.cobbzilla.util.system.CommandShell;
import org.cobbzilla.wizard.dao.DAO;
import org.cobbzilla.wizard.model.Identifiable;

import java.io.IOException;

import static org.cobbzilla.util.daemon.ZillaRuntime.die;
import static org.cobbzilla.util.json.JsonUtil.toJson;

@Slf4j
public abstract class CloudOsLauncherBase<A extends Identifiable, C extends CloudOsBase, S extends CloudOsStatusBase<A, C>>
        extends CloudOsChefDeployer<A, C, S> {

    @Getter protected S status;
    @Getter protected DAO<C> cloudOsDAO;

    protected A admin;
    protected C cloudOs;

    protected String getSimpleHostname() { return cloudOs.getName(); }

    @Getter(lazy=true) private final CsCloud cloud = buildCloud();
    protected CsInstance instance = null;

    protected abstract CsCloud buildCloud();
    protected abstract boolean addAppStoreAccount(String hostname, String ucid);

    protected int getMaxLaunchTries() { return 1; } // default: no retries

    public CloudOsLauncherBase(S status, DAO<C> cloudOsDAO) {
        this.status = status;
        this.cloudOsDAO = cloudOsDAO;
        this.admin = status.getAdmin();
        this.cloudOs = status.getCloudOs();
    }

    protected String getFqdn() { return getCloud().getConfig().getFqdn(instance.getHost()); }

    protected void updateState(C cloudOs, CloudOsState state) {
        cloudOs.updateState(state);
        cloudOsDAO.update(cloudOs);
    }

    public void run() {
        boolean success = false;
        final int maxRetries = getMaxLaunchTries();
        for (int tries = 0; tries < maxRetries; tries++) {
            status.initRetry();
            try {
                launch();
                if (!status.hasError()) {
                    // Give the server 10 seconds to get up and running. It should be already, but just in case.
                    Thread.sleep(10000);

                    if (cloudOsIsRunning()) {
                        status.success("{setup.success}");
                        success = true;
                        updateState(cloudOs, CloudOsState.live);
                        break;

                    } else {
                        final String fqdn = instance != null ? getFqdn() : "no-fqdn";
                        die("launch completed OK but instance ("+ fqdn +") was not running!");
                    }
                }

            } catch (Exception e) {
                if (tries == maxRetries-1) {
                    status.error("{setup.error.unexpected.final}", "An unexpected error occurred during setup, we are giving up");
                } else {
                    status.error("{setup.error.unexpected.willRetry}", "An unexpected error occurred during setup, we will retry");
                }

            } finally {
                if (!success && instance != null) {
                    updateState(status.getCloudOs(), CloudOsState.destroying);
                    try {
                        if (getCloud() != null && !getCloud().teardown(instance)) {
                            log.error("error tearing down instance that failed to come up properly (returned false)");
                            updateState(status.getCloudOs(), CloudOsState.error);
                        } else {
                            updateState(status.getCloudOs(), CloudOsState.destroyed);
                        }
                    } catch (Exception e) {
                        log.error("error tearing down instance that failed to come up properly: " + e, e);
                    }
                }
            }
        }
    }

    protected boolean cloudOsIsRunning() {
        if (instance == null || !status.isCompleted()) return false;
        if (instance instanceof MockCsInstance) return true;

        // todo: try this a few times before giving up
        final String url = "https://" + instance.getPublicIp() + "/";
        final CommandLine command = new CommandLine("curl")
                .addArgument("--insecure") // since we are requested via the IP address, the cert will not match
                .addArgument("--header").addArgument("Host: "+getFqdn()) // pass FQDN via Host header
                .addArgument("--silent")
                .addArgument("--location")                              // follow redirects
                .addArgument("--write-out").addArgument("%{http_code}") // just print status code
                .addArgument("--output").addArgument("/dev/null")       // and ignore data
                .addArgument(url);
        try {
            final CommandResult result = CommandShell.exec(command);
            final String statusCode = result.getStdout();
            return result.isZeroExitStatus() && statusCode != null && statusCode.trim().startsWith("2");

        } catch (IOException e) {
            log.warn("cloudOsIsRunning: Error fetching "+url+" with Host header="+getFqdn()+": "+e);
            return false;
        }
    }

    @Override protected void chefStart(C cloudOs) {
        status.update("{setup.cheffing}");
        updateState(cloudOs, CloudOsState.cheffing);
    }

    @Override protected void chefComplete(C cloudOs) {
        updateState(cloudOs, CloudOsState.cheffed);
        log.info("chef-solo succeeded");
    }

    @Override protected void chefError(C cloudOs, CommandResult commandResult, Exception e) {
        status.error("{setup.error.cheffing.serverError}", "Error running chef-solo");
        log.error("Error running chef (" + e + "): stdout:\n" + ((commandResult == null) ? null : commandResult.getStdout()) + "\n\nstderr:\n" + ((commandResult == null) ? null : commandResult.getStderr()));
        updateState(cloudOs, CloudOsState.error);
    }

    protected boolean resetCloudOs() {

        CsInstance instance;
        if (!cloudOs.getAdminUuid().equals(admin.getUuid())) {
            status.error("{setup.error.notOwner}", "Another user owns this cloud");
            return false;
        }

        // if the instance is running, stop it and relaunch
        status.update("{setup.instanceLookup}");
        instance = cloudOs.getInstance();

        if (instance != null) {
            try {
                status.update("{setup.teardownPreviousInstance}");
                updateState(cloudOs, CloudOsState.destroying);
                getCloud().teardown(instance);
                updateState(cloudOs, CloudOsState.destroyed);

            } catch (Exception e) {
                log.error("Error tearing down instance prior to relaunch (marching bravely forward!): " + e, e);
                status.update("{setup.teardownPreviousInstance.nonFatalError}");
                updateState(cloudOs, CloudOsState.error);
            }
        }
        return true;
    }

    protected boolean preLaunch() { return true; }
    protected boolean setupDns () { return true; }
    protected boolean setupMailCreds() { return true; }

    // todo: this can be massively parallelized... use promises/futures to do as much async as possible
    // only a few things truly need to happen "in order", to be documented :)
    protected void launch() {

        // this will handle tearing down any existing instance
        if (!resetCloudOs()) {
            // problem, error status set in resetCloudOs
            return;
        }

        if (!preLaunch()) return;

        // start instance
        final String hostname = getSimpleHostname();
        status.update("{setup.startingMasterInstance}");
        final CsInstanceRequest instanceRequest = new CsInstanceRequest().setHost(hostname);
        try {
            updateState(cloudOs, CloudOsState.starting);
            instance = getCloud().newInstance(instanceRequest);

            cloudOs.setInstanceJson(toJson(instance));
            cloudOs = cloudOsDAO.update(cloudOs);
            updateState(cloudOs, CloudOsState.started);

        } catch (Exception e) {
            if (e.getMessage().contains("Size is not available in this region")) {
                status.error("{setup.error.startingMasterInstance.sizeUnavailableInRegion}", "The size requested is not currently available in the region requested");
            } else {
                status.error("{setup.error.startingMasterInstance.serverError}", "Error booting compute instance in cloud: " + e);
            }
            updateState(cloudOs, CloudOsState.error);
            return;
        }

        if (!setupDns()) return;

        // notify app store of new cloud; set appstore connection info
        final String ucid = cloudOs.getUcid();
        if (!addAppStoreAccount(getFqdn(), ucid)) return;

        if (!setupMailCreds()) return;

        if (!chefDeploy(cloudOs, hostname, instance.getPublicIp(), instance.getKey())) return;

        log.info("launch completed OK: "+instance.getHost());
        updateState(cloudOs, CloudOsState.setup_complete);
        status.completed();
    }
}
