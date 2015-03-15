package cloudos.appstore.bundler;

import cloudos.appstore.model.app.AppManifest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.io.FileUtils;
import org.cobbzilla.util.io.FileUtil;
import org.cobbzilla.util.json.JsonUtil;
import org.cobbzilla.util.system.CommandShell;

import java.io.File;
import java.util.*;

import static cloudos.appstore.model.app.AppManifest.CLOUDOS_MANIFEST_JSON;
import static org.cobbzilla.util.daemon.ZillaRuntime.die;
import static org.cobbzilla.util.io.FileUtil.abs;

@Slf4j @AllArgsConstructor
public class MultiBundlerMain {

    public static final String TARGET_DIR = "target";
    public static final String PRE_BUNDLE_SH = "pre_bundle.sh";
    @Getter @Setter private String[] args;

    private final Map<String, Object> results = new HashMap<>();

    public static void main (String[] args) throws Exception {
        final MultiBundlerMain main = new MultiBundlerMain(args);
        main.run();
    }

    private void run() throws Exception {
        final File dir = new File(args[0]);
        if (!dir.exists() || !dir.isDirectory()) {
            throw new IllegalArgumentException("Not a directory: "+abs(dir));
        }
        try {
            processDir(dir);
        } finally {
            StringBuilder b = new StringBuilder("\n----- RESULTS -----\n");
            Set<String> apps = new TreeSet<>(results.keySet());
            for (String app : apps) {
                final Object result = results.get(app);
                if (result instanceof String) b.append(app).append(" ==> ").append(result.toString()).append("\n");
            }
            for (String app: apps) {
                final Object result = results.get(app);
                if (!(result instanceof String)) b.append(app).append(" ==> ").append(result.toString()).append("\n");
            }
            b.append("----- END RESULTS -----\n");
            log.info(b.toString());
        }
    }

    private void processDir(File dir) throws Exception {

        final Set<String> processed = new HashSet<>();
        final File[] files = FileUtil.list(dir);

        for (File f : files) {
            final String fpath = abs(f);

            if (f.isDirectory()) {
                if (f.getName().equals(TARGET_DIR)) continue;
                processDir(f);
            }

            if (!processed.contains(fpath)
                    && f.isFile()
                    && (f.getName().equals(CLOUDOS_MANIFEST_JSON) || f.getName().equals(PRE_BUNDLE_SH))) {
                final String appName = f.getParentFile().getName();
                try {
                    bundleApp(f);
                    results.put(appName, "success!");
                } catch (Exception e) {
                    results.put(appName, e);
                } finally {
                    processed.add(fpath);
                }
            }
        }
    }

    private void bundleApp(File manifestFile) throws Exception {

        // If the "manifest" is actually a pre_bundle.sh script, run it, then look for the manifest
        if (manifestFile.getName().equals(PRE_BUNDLE_SH)) {
            CommandShell.exec(new CommandLine(manifestFile));
            final File realManifest = new File(manifestFile.getParentFile(), CLOUDOS_MANIFEST_JSON);
            if (!realManifest.exists()) die(CLOUDOS_MANIFEST_JSON + " does not exist, even after running: " + abs(manifestFile));
            manifestFile = realManifest;
        }

        final AppManifest manifest = JsonUtil.fromJson(FileUtil.toString(manifestFile), AppManifest.class);
        final String appName = manifestFile.getParentFile().getName();
        final AppBundlerFactory factory = new DefaultAppBundlerFactory();
        final AppBundler bundler = factory.getBundler(manifest);

        // Recreate output dir
        final String appBundleDir = appName + "-bundle";
        final File targetDir = new File(manifestFile.getParentFile(), TARGET_DIR);
        final File outputDir = new File(targetDir, appBundleDir);
        if (outputDir.exists()) FileUtils.deleteDirectory(outputDir);
        FileUtil.mkdirOrDie(outputDir);

        final BundlerOptions options = new BundlerOptions();
        options.setManifest(manifestFile);
        options.setOutputDir(outputDir);

        bundler.bundle(options, manifest);
        log.info("tarball("+appBundleDir+"): "+CommandShell.execScript("cd " + abs(outputDir) + " && tar cvzf ../"+appBundleDir+".tar.gz . 2>&1"));
    }

}
