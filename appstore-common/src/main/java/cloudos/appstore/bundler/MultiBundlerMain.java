package cloudos.appstore.bundler;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.cobbzilla.util.collection.ArrayUtil;
import org.cobbzilla.util.io.FileUtil;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.cobbzilla.util.io.FileUtil.abs;
import static org.cobbzilla.util.io.FileUtil.listDirs;

@Slf4j
public class MultiBundlerMain extends BundlerMain {

    private final Map<String, Object> results = new HashMap<>();

    public static void main (String[] args) throws Exception {
        main(MultiBundlerMain.class, args);
    }

    public void run() throws Exception {

        final File dir = new File(args[0]);
        if (!dir.exists() || !dir.isDirectory()) die("Not a directory: "+abs(dir));

        // populate options based on remaining args
        if (args.length > 1) parser.parseArgument(ArrayUtil.slice(args, 1, args.length));

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
            System.out.println(b.toString());
        }
    }

    private void processDir(File dir) throws Exception {

        for (File d : listDirs(dir)) {
            if (d.getName().equals(BundlerOptions.DEFAULT_OUTPUT_DIR)) continue;
            if (isAppDirectory(d)) {
                final String appName = d.getName();
                try {
                    bundleApp(d);
                    results.put(appName, "success!");
                } catch (Exception e) {
                    results.put(appName, e+"\n"+ ExceptionUtils.getStackTrace(e));
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
        final BundlerOptions options = new BundlerOptions(this.options);
        options.setManifest(appDir);
        options.setOutputDir(outputDir);
        BundlerMain.main(options.toArgs());
    }

}
