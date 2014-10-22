package cloudos.appstore.bundler;

import cloudos.appstore.model.app.AppManifest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.cobbzilla.util.io.FileUtil;
import org.cobbzilla.util.json.JsonUtil;
import org.cobbzilla.util.system.CommandShell;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

@Slf4j @AllArgsConstructor
public class MultiBundlerMain {

    public static final String TARGET_DIR = "target";
    @Getter @Setter private String[] args;

    private final Map<String, Object> results = new HashMap<>();

    public static void main (String[] args) throws Exception {
        final MultiBundlerMain main = new MultiBundlerMain(args);
        main.run();
    }

    private void run() throws Exception {
        final File dir = new File(args[0]);
        if (!dir.exists() || !dir.isDirectory()) {
            throw new IllegalArgumentException("Not a directory: "+dir.getAbsolutePath());
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
        for (File f : dir.listFiles()) {
            if (f.isDirectory()) {
                if (f.getName().equals(TARGET_DIR)) continue;
                processDir(f);
            }
            if (f.isFile() && f.getName().equals("cloudos-manifest.json")) {
                final String appName = f.getParentFile().getName();
                try {
                    bundleApp(f);
                    results.put(appName, "success!");
                } catch (Exception e) {
                    results.put(appName, e);
                }
            }
        }
    }

    private void bundleApp(File manifestFile) throws Exception {

        final AppManifest manifest = JsonUtil.fromJson(FileUtil.toString(manifestFile), AppManifest.class);
        final String appName = manifestFile.getParentFile().getName();
        final AppBundlerFactory factory = new DefaultAppBundlerFactory();
        final AppBundler bundler = factory.getBundler(manifest);

        // If a src dir exists and a pom file exists, run maven
        final File srcDir = new File(manifestFile.getParentFile(), "src");
        if (srcDir.exists() && new File(manifestFile.getParentFile(), "pom.xml").exists()) {
            CommandShell.execScript("cd "+srcDir.getAbsolutePath()+" && mvn clean package");
        }

        // Recreate output dir
        final String appBundleDir = appName + "-bundle";
        final File outputDir = new File(manifestFile.getParentFile() + "/" + TARGET_DIR + "/" + appBundleDir);
        if (outputDir.exists()) FileUtils.deleteDirectory(outputDir);
        if (!outputDir.mkdirs()) throw new IllegalStateException("Error creating output dir:"+outputDir.getAbsolutePath());

        final BundlerOptions options = new BundlerOptions();
        options.setManifest(manifestFile);
        options.setOutputDir(outputDir);

        bundler.bundle(options, manifest);
        log.info("tarball("+appBundleDir+"): "+CommandShell.execScript("cd " + outputDir.getAbsolutePath() + "/.. && tar cvzf "+appBundleDir+".tar.gz "+appBundleDir + " 2>&1"));
    }

}
