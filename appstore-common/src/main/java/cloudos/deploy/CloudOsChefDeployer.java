package cloudos.deploy;

import cloudos.appstore.model.app.AppLevel;
import cloudos.appstore.model.app.AppManifest;
import cloudos.databag.BaseDatabag;
import cloudos.databag.CloudOsDatabag;
import cloudos.model.instance.CloudOsBase;
import cloudos.model.instance.CloudOsEvent;
import cloudos.model.instance.CloudOsTaskResultBase;
import edu.emory.mathcs.backport.java.util.Arrays;
import lombok.Cleanup;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.io.FileUtils;
import org.cobbzilla.util.io.DeleteOnExit;
import org.cobbzilla.util.io.Tarball;
import org.cobbzilla.util.io.TempDir;
import org.cobbzilla.util.string.StringUtil;
import org.cobbzilla.util.system.Command;
import org.cobbzilla.util.system.CommandProgressFilter;
import org.cobbzilla.util.system.CommandResult;
import org.cobbzilla.util.system.CommandShell;
import org.cobbzilla.wizard.dao.DAO;
import org.cobbzilla.wizard.model.Identifiable;
import org.cobbzilla.wizard.task.TaskBase;
import org.cobbzilla.wizard.validation.SimpleViolationException;
import rooty.toots.chef.ChefHandler;
import rooty.toots.chef.ChefSolo;
import rooty.toots.chef.ChefSoloEntry;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.cobbzilla.util.daemon.ZillaRuntime.die;
import static org.cobbzilla.util.io.FileUtil.*;
import static org.cobbzilla.util.json.JsonUtil.fromJson;
import static org.cobbzilla.util.system.CommandShell.chmod;
import static rooty.toots.chef.ChefSolo.SOLO_JSON;

@Slf4j
public abstract class CloudOsChefDeployer<A extends Identifiable,
        C extends CloudOsBase,
        R extends CloudOsTaskResultBase<A, C>>
        extends TaskBase<R, CloudOsEvent> {

    protected abstract DAO<C> getCloudOsDAO();

    protected A admin () { return result.getAdmin(); }
    protected C cloudOs () { return result.getCloudOs(); }

    protected abstract void chefStart(C cloudOs);
    protected abstract void chefComplete(C cloudOs);
    protected abstract void chefError(C cloudOs, CommandResult commandResult, Exception e);

    public static final List<String> DATA_FILES = Arrays.asList(new String[]{"geoip"});

    @Override protected CloudOsEvent newEvent(String messageKey) {
        return super.newEvent(messageKey).setCloudOsUuid(cloudOs().getUuid());
    }

    @Getter(lazy=true) private final BaseDatabag baseDatabag = BaseDatabag.fromChefRepo(getInitFilesDir());
    @Getter(lazy=true) private final CloudOsDatabag cloudOsDatabag = CloudOsDatabag.fromChefRepo(getInitFilesDir());

    private File initFilesDir;
    protected File getInitFilesDir() {
        if (initFilesDir == null) {
            // decrypt and unroll the zipfile
            initFilesDir = createInitFilesDir();
            DeleteOnExit.add(initFilesDir);
        }
        return initFilesDir;
    }

    protected File getStagingDir() {
        final File stagingDir;
        if (cloudOs().hasStagingDir()) {
            stagingDir = cloudOs().getStagingDirFile();
        } else {
            stagingDir = createChefDir();
            cloudOs().setStagingDir(abs(stagingDir));
            getCloudOsDAO().update(cloudOs());
        }
        DeleteOnExit.add(stagingDir);
        return stagingDir;
    }

    protected File createInitFilesDir() { return new TempDir(); }
    protected File createChefDir() { return new TempDir(); }

    public static boolean prepChefRepo(File stagingDir,
                                       File chefMaster,
                                       List<String> appList,
                                       AppBundleResolver appResolver) {
        try {
            final Map<AppLevel, List<String>> appsByLevel = new HashMap<>();
            for (String app : appList) {
                final File bundleTarball = appResolver.getAppBundle(app);
                if (bundleTarball == null) {
                    throw new SimpleViolationException("err.cloudos.app.invalid", "no bundle could be located for app: "+app, app);
                }

                // unroll it, we'll rsync it to the target host later
                @Cleanup final TempDir tempDir = Tarball.unroll(bundleTarball);

                // for now, rsync it to our staging directory
                CommandShell.execScript("rsync -avc " + abs(tempDir) + "/chef/* " + abs(stagingDir) + "/");

                // peek in the manifest to see what precedence this app should take in the run list
                final AppManifest manifest = AppManifest.load(tempDir);
                List<String> apps = appsByLevel.get(manifest.getLevel());
                if (apps == null) {
                    apps = new ArrayList<>();
                    appsByLevel.put(manifest.getLevel(), apps);
                }
                apps.add(app);
            }

            // Copy master files
            for (File f : listFiles(chefMaster)) {
                FileUtils.copyFileToDirectory(f, stagingDir);
                if (f.canExecute()) chmod(new File(stagingDir, f.getName()), "a+rx");
            }

            // create data_files dir and copy files, if any
            final File masterDatafiles  = new File(chefMaster, ChefSolo.DATAFILES_DIR);
            final File stagingDatafiles = new File(stagingDir, ChefSolo.DATAFILES_DIR);
            for (String path : DATA_FILES) {
                CommandShell.exec("rsync -avzc "+abs(masterDatafiles)+"/"+path+" "+abs(stagingDatafiles));
            }

            // Order apps by level
            final List<String> allApps = new ArrayList<>(appsByLevel.size());
            for (AppLevel level : AppLevel.values()) {
                final List<String> apps = appsByLevel.get(level);
                if (apps != null) allApps.addAll(apps);
            }

            // Build chef solo.json using cookbooks in order of priority
            final ChefSolo soloJson = new ChefSolo();
            for (String app : allApps) {
                if (ChefSolo.recipeExists(stagingDir, app, "lib")) soloJson.add("recipe["+app+"::lib]");
            }
            for (String app : allApps) {
                if (ChefSolo.recipeExists(stagingDir, app, "default")) {
                    soloJson.add("recipe[" + app + "]");
                } else {
                    log.warn("No default recipe found for app: "+app);
                }
            }
            for (String app : allApps) {
                if (ChefSolo.recipeExists(stagingDir, app, "validate")) soloJson.add("recipe["+app+"::validate]");
            }
            soloJson.write(stagingDir);

        } catch (Exception e) {
            log.error("prepChefRepo: Error preparing chef staging dir: "+e);
            return false;
        }
        return true;
    }

    public boolean chefDeploy(C cloudOs, String hostname, String publicIp, String privateKey) {

        // run chef-solo to configure the box
        chefStart(cloudOs);

        final File stagingDir = getStagingDir();
        CommandResult commandResult = null;
        try {
            final File deploy = new File(stagingDir, "deploy.sh");
            chmod(deploy, "u+x");

            final File deployLib = new File(stagingDir, "deploy_lib.sh");
            chmod(deployLib, "u+x");

            final CommandLine chefSolo = new CommandLine(deploy)
                    .addArgument(hostname + "@" + publicIp)
                    .addArgument(SOLO_JSON)
                    .addArgument(getMode().name());

            // setup system env for deploy.sh script
            final Map<String, String> chefSoloEnv = new HashMap<>();
            chefSoloEnv.put("INIT_FILES", abs(getInitFilesDir()));

            final String jsonEdit = getJsonEdit();
            if (jsonEdit != null) chefSoloEnv.put("JSON_EDIT", jsonEdit);

            final List<File> requiredFiles = getRequiredFiles();
            if (requiredFiles != null) chefSoloEnv.put("REQUIRED", StringUtil.toString(requiredFiles, " "));

            final List<File> cookbookSources = getCookbookSources();
            if (cookbookSources != null) chefSoloEnv.put("COOKBOOK_SOURCES", StringUtil.toString(cookbookSources, " "));

            // decrypt the private key and put it on disk somewhere, so that deploy.sh works without asking for a passphrase
            // we can make this a lot easier... what a PITA
            if (privateKey == null) {
                log.warn("instance had no private key (mock instance?), skipping chef setup");

            } else {
                @Cleanup("delete") final File keyFile = File.createTempFile("cloudos", ".key");
                chmod(keyFile, "600");
                toFile(abs(keyFile), privateKey);
                chefSoloEnv.put("SSH_KEY", abs(keyFile)); // add key to env

                final CommandProgressFilter filter = getLaunchProgressFilter(stagingDir);
                final Command command = new Command(chefSolo)
                        .setDir(stagingDir)
                        .setEnv(chefSoloEnv)
                        .setOut(filter)
                        .setCopyToStandard(true);
                commandResult = CommandShell.exec(command);

                if (!commandResult.isZeroExitStatus() || commandResult.getStdout().contains("FATAL")) {
                    die("Error running chef-solo: " + commandResult);
                }
                chefComplete(cloudOs);
            }

        } catch (Exception e) {
            chefError(cloudOs, commandResult, e);
            return false;
        }

        return true;
    }

    public String getJsonEdit() { return null; }
    public List<File> getRequiredFiles() { return null; }
    public List<File> getCookbookSources() { return null; }

    public enum Mode { tempdir, inline }

    public Mode getMode() { return Mode.tempdir; }

    public static final String[] CHEF_BOOTSTRAP_INDICATORS = {
            "Reading package lists", "Preconfiguring packages", "Current default time zone",
            "Running depmod.", "Updating certificates in /etc/ssl/certs",
            "INFO: Chef-client pid"
    };

    protected CommandProgressFilter getLaunchProgressFilter(File chefDir) throws Exception {

        final CommandProgressFilter filter = new CommandProgressFilter()
                .setCallback(new CloudOsLaunchProgressCallback(getResult()));

        final float chefBootstrapPct = 30.0f;
        final float chefRunPct = 100.0f - chefBootstrapPct;
        float pct = 1.0f;
        float delta = chefBootstrapPct / ((float) CHEF_BOOTSTRAP_INDICATORS.length);

        for (String indicator : CHEF_BOOTSTRAP_INDICATORS) {
            filter.addIndicator(indicator, (int) pct);
            pct += delta;
        }

        final ChefSolo solo = fromJson(new File(chefDir, SOLO_JSON), ChefSolo.class);
        int numEntries = 0;
        for (ChefSoloEntry entry : solo.getEntries()) {
            if (entry.isRecipe("default") || entry.isRecipe("validate")) {
                numEntries++;
            }
        }

        delta = chefRunPct / ((float) numEntries);
        pct = chefBootstrapPct;
        for (ChefSoloEntry entry : solo.getEntries()) {
            if (entry.isRecipe("default") || entry.isRecipe("validate")) {
                filter.addIndicator(ChefHandler.getChefProgressPattern(entry), (int) pct);
                pct += delta;
            }
        }
        return filter;
    }

}
