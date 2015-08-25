package cloudos.appstore.bundler;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.cobbzilla.util.io.FileUtil;
import org.cobbzilla.wizard.main.MainBase;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.cobbzilla.util.io.FileUtil.abs;
import static org.cobbzilla.util.io.FileUtil.listDirs;

@Slf4j
public class MultiBundlerMain extends MainBase<MultiBundlerOptions> {

    private final Map<String, Object> success = new HashMap<>();
    private final Map<String, Object> failure = new HashMap<>();

    public static void main (String[] args) { main(MultiBundlerMain.class, args); }

    public void run() throws Exception {

        final File dir = getOptions().getDir();
        if (!dir.exists() || !dir.isDirectory()) die("Not a directory: "+abs(dir));

        try {
            processDir(dir);
        } finally {
            Set<String> apps;
            StringBuilder b = new StringBuilder("\n----- RESULTS -----\n");

            apps = new TreeSet<>(success.keySet());
            for (String app : apps) {
                final Object result = success.get(app);
                if (result instanceof String) b.append(app).append(" ==> ").append(result.toString()).append("\n");
            }
            apps = new TreeSet<>(failure.keySet());
            for (String app : apps) {
                final Object result = failure.get(app);
                if (!(result instanceof String)) b.append(app).append(" ==> ").append(result.toString()).append("\n");
            }

            b.append("----- END RESULTS -----\n");
            out(b.toString());
        }
    }

    private void processDir(File dir) throws Exception {

        for (File d : listDirs(dir)) {
            if (d.getName().equals(BundlerOptions.DEFAULT_OUTPUT_DIR)) continue;
            if (BundlerMain.isAppDirectory(d)) {
                final String appName = d.getName();
                try {
                    bundleApp(d);
                    success.put(appName, "success!");
                } catch (Exception e) {
                    failure.put(appName, e+"\n"+ ExceptionUtils.getStackTrace(e));
                }
            } else {
                processDir(d);
            }
        }
    }

    private void bundleApp(File appDir) throws Exception {

        // Recreate output dir
        final File outputDir = new File(appDir, BundlerOptions.DEFAULT_OUTPUT_DIR);
        final File buildDir = new File(outputDir, BundlerOptions.BUILD_DIR);
        if (buildDir.exists()) FileUtils.deleteDirectory(buildDir);
        FileUtil.mkdirOrDie(buildDir);

        // copy options provided at the command line
        final BundlerOptions options = new BundlerOptions(getOptions());
        options.setManifest(appDir);
        options.setOutputDir(outputDir);
        BundlerMain.main(options.toArgs());
    }

}
