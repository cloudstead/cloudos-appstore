package cloudos.appstore.bundler;

import cloudos.appstore.model.app.AppManifest;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.cobbzilla.util.system.CommandShell;
import org.kohsuke.args4j.CmdLineParser;

import java.io.File;

import static cloudos.appstore.model.app.AppManifest.CLOUDOS_MANIFEST_JSON;
import static org.cobbzilla.util.daemon.ZillaRuntime.die;
import static org.cobbzilla.util.io.FileUtil.abs;

@Slf4j
public class BundlerMain {

    public static final String PRE_BUNDLE_SH = "pre_bundle.sh";

    @Getter @Setter protected String[] args;

    protected final BundlerOptions options = new BundlerOptions();
    protected final CmdLineParser parser = new CmdLineParser(options);

    public BundlerMain(String[] args) { this.args = args; }

    public static boolean isAppDirectory(File dir) {
        return new File(dir, CLOUDOS_MANIFEST_JSON).exists() || new File(dir, PRE_BUNDLE_SH).exists();
    }

    public static void main (String[] args) throws Exception {
        final BundlerMain main = new BundlerMain(args);
        main.run();
    }

    public void run () throws Exception {
        parser.parseArgument(args);

        File manifestFile = options.getManifest();

        if (manifestFile.isDirectory() && prebundleScript(manifestFile).exists()) {
            CommandShell.exec(abs(prebundleScript(manifestFile)));

        } else if (manifestFile.isFile() && prebundleScript(manifestFile.getParentFile()).exists()) {
            CommandShell.exec(abs(prebundleScript(manifestFile.getParentFile())));
        }

        final AppManifest manifest = AppManifest.load(manifestFile);
        final AppBundler bundler = new DefaultAppBundler();
        bundler.bundle(options, manifest);
    }

    protected File prebundleScript(File dir) { return new File(dir, PRE_BUNDLE_SH); }

    public void runOrDie () { try { run(); } catch (Exception e) { die("runOrDie: "+e, e); } }

}
