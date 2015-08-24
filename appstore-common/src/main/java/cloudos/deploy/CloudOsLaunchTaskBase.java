package cloudos.deploy;

import cloudos.cslib.compute.CsCloud;
import cloudos.cslib.compute.instance.CsInstance;
import cloudos.cslib.compute.instance.CsInstanceRequest;
import cloudos.cslib.compute.mock.MockCsInstance;
import cloudos.model.instance.CloudOsBase;
import cloudos.model.instance.CloudOsEvent;
import cloudos.model.instance.CloudOsState;
import cloudos.model.instance.CloudOsTaskResultBase;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cobbzilla.util.http.HttpUtil;
import org.cobbzilla.util.system.CommandResult;
import org.cobbzilla.wizard.dao.DAO;
import org.cobbzilla.wizard.model.Identifiable;

import java.util.concurrent.TimeUnit;

import static org.cobbzilla.util.daemon.ZillaRuntime.die;
import static org.cobbzilla.util.json.JsonUtil.toJson;

@Slf4j @NoArgsConstructor
public abstract class CloudOsLaunchTaskBase<A extends Identifiable,
                                          C extends CloudOsBase,
                                          R extends CloudOsTaskResultBase<A, C>>
        extends CloudOsChefDeployer<A, C, R> {

    @Getter protected DAO<C> cloudOsDAO;

    protected A admin () { return result.getAdmin(); }
    protected C cloudOs () { return result.getCloudOs(); }

    protected String getSimpleHostname() { return cloudOs().getName(); }

    @Getter(lazy=true) private final CsCloud cloud = buildCloud();
    protected CsInstance instance = null;

    protected abstract CsCloud buildCloud();
    protected abstract boolean addAppStoreAccount(String hostname, String ucid);

    @Override protected long getTerminationTimeout() { return TimeUnit.MINUTES.toMillis(5); }

    protected int getMaxLaunchTries() { return 1; } // default: no retries

    public void init (A admin, C cloudOs, DAO<C> cloudOsDAO, DAO<CloudOsEvent> eventDAO) {
        this.cloudOsDAO = cloudOsDAO;
        result.setAdmin(admin);
        result.setCloudOs(cloudOs);
        result.setEventDAO(eventDAO);
    }

    protected String getFqdn() { return getCloud().getConfig().getFqdn(instance.getHost()); }

    protected void updateState(C cloudOs, CloudOsState state) {
        cloudOs.updateState(state);
        cloudOsDAO.update(cloudOs);
    }

    @Override public synchronized R execute() {
        try {
            boolean success = false;
            final int maxRetries = getMaxLaunchTries();
            for (int tries = 0; tries < maxRetries; tries++) {
                result.initRetry();
                try {
                    launch();
                    if (!result.hasError()) {
                        // Give the server 10 seconds to get up and running. It should be already, but just in case.
                        Thread.sleep(10000);

                        if (cloudOsIsRunning()) {
                            result.success("{setup.success}");
                            success = true;
                            updateState(cloudOs(), CloudOsState.live);
                            break;

                        } else {
                            final String fqdn = instance != null ? getFqdn() : "no-fqdn";
                            die("launch completed OK but instance (" + fqdn + ") was not running!");
                        }
                    }

                } catch (Exception e) {
                    if (tries == maxRetries - 1) {
                        result.error("{setup.error.unexpected.final}", "An unexpected error occurred during setup, we are giving up");
                    } else {
                        result.error("{setup.error.unexpected.willRetry}", "An unexpected error occurred during setup, we will retry");
                    }

                } finally {
                    if (!success && instance != null) {
                        if (!teardown()) log.error("error tearing down instance that failed to come up properly");
                    }
                }
            }
            return result;

        } finally {
            if (cancelled || cloudOs().getState() != CloudOsState.live) {
                teardown();
            }
        }
    }

    public synchronized boolean teardown() {
        if (instance == null) instance = cloudOs().getInstance();
        updateState(cloudOs(), CloudOsState.destroying);
        try {
            final CsCloud cloud = getCloud();
            if (cloud != null && cloud.isRunning(instance) && cloud.teardown(instance) == 0) {
                log.error("error tearing down instance that failed to come up properly (returned false)");
                updateState(cloudOs(), CloudOsState.error);
                return false;
            } else {
                updateState(cloudOs(), CloudOsState.destroyed);
                return true;
            }
        } catch (Exception e) {
            log.error("teardown: "+e, e);
            return false;
        }
    }

    protected boolean cloudOsIsRunning() {
        if (instance == null || !result.isComplete()) return false;
        if (instance instanceof MockCsInstance) return true;
        return HttpUtil.isOk("https://" + instance.getPublicIp() + "/", getFqdn(), 3, TimeUnit.SECONDS.toMillis(10));
    }

    @Override protected void chefStart(C cloudOs) {
        result.update("{setup.cheffing}");
        updateState(cloudOs, CloudOsState.cheffing);
    }

    @Override protected void chefComplete(C cloudOs) {
        updateState(cloudOs, CloudOsState.cheffed);
        log.info("chef-solo succeeded");
    }

    @Override protected void chefError(C cloudOs, CommandResult commandResult, Exception e) {
        result.error("{setup.error.cheffing.serverError}", "Error running chef-solo");
        log.error("Error running chef (" + e + "): stdout:\n" + ((commandResult == null) ? null : commandResult.getStdout()) + "\n\nstderr:\n" + ((commandResult == null) ? null : commandResult.getStderr()));
        updateState(cloudOs, CloudOsState.error);
    }

    protected boolean resetCloudOs() {

        CsInstance instance;
        if (!cloudOs().getAdminUuid().equals(admin().getUuid())) {
            result.error("{setup.error.notOwner}", "Another user owns this cloud");
            return false;
        }

        // if the instance is running, stop it and relaunch
        result.update("{setup.instanceLookup}");
        instance = cloudOs().getInstance();

        if (instance != null) {
            try {
                result.update("{setup.teardownPreviousInstance}");
                updateState(cloudOs(), CloudOsState.destroying);
                getCloud().teardown(instance);
                updateState(cloudOs(), CloudOsState.destroyed);

            } catch (Exception e) {
                log.error("Error tearing down instance prior to relaunch (marching bravely forward!): " + e, e);
                result.update("{setup.teardownPreviousInstance.nonFatalError}");
                updateState(cloudOs(), CloudOsState.error);
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
        result.update("{setup.startingMasterInstance}");
        final CsInstanceRequest instanceRequest = new CsInstanceRequest().setHost(hostname);
        try {
            updateState(cloudOs(), CloudOsState.starting);
            instance = getCloud().newInstance(instanceRequest);

            cloudOs().setInstanceJson(toJson(instance));
            result.setCloudOs(cloudOsDAO.update(cloudOs()));
            updateState(cloudOs(), CloudOsState.started);

        } catch (Exception e) {
            if (e.getMessage().contains("Size is not available in this region")) {
                result.error("{setup.error.startingMasterInstance.sizeUnavailableInRegion}", "The size requested is not currently available in the region requested");
            } else {
                result.error("{setup.error.startingMasterInstance.serverError}", "Error booting compute instance in cloud: " + e);
            }
            updateState(cloudOs(), CloudOsState.error);
            return;
        }

        if (!setupDns()) return;

        // notify app store of new cloud; set appstore connection info
        final String ucid = cloudOs().getUcid();
        if (!addAppStoreAccount(getFqdn(), ucid)) return;

        if (!setupMailCreds()) return;

        if (!chefDeploy(cloudOs(), hostname, instance.getPublicIp(), instance.getKey())) return;

        log.info("launch completed OK: "+instance.getHost());
        updateState(cloudOs(), CloudOsState.setup_complete);
        result.success("{setup.success}");
    }
}
