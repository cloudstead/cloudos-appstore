package cloudos.appstore.bundler;

import cloudos.appstore.model.app.AppManifest;
import lombok.extern.slf4j.Slf4j;
import org.cobbzilla.wizard.main.MainBase;

import java.io.File;

import static cloudos.appstore.model.app.AppManifest.CLOUDOS_MANIFEST_JSON;
import static org.cobbzilla.util.io.FileUtil.abs;
import static org.cobbzilla.util.system.CommandShell.exec;
import static org.cobbzilla.util.system.CommandShell.okResult;

@Slf4j
public class BundlerMain extends MainBase<BundlerOptions> {

    public static final String PRE_BUNDLE_SH = "pre_bundle.sh";

    public static boolean isAppDirectory(File dir) {
        return new File(dir, CLOUDOS_MANIFEST_JSON).exists() || new File(dir, PRE_BUNDLE_SH).exists();
    }

    public static void main (String[] args) { main(BundlerMain.class, args); }

    public void run () throws Exception {
        final File manifestFile = getOptions().getManifest();

        if (manifestFile.isDirectory() && prebundleScript(manifestFile).exists()) {
            okResult(exec(abs(prebundleScript(manifestFile))));

        } else if (manifestFile.isFile() && prebundleScript(manifestFile.getParentFile()).exists()) {
            okResult(exec(abs(prebundleScript(manifestFile.getParentFile()))));
        }

        final AppManifest manifest = AppManifest.load(manifestFile);
        final AppBundler bundler = new DefaultAppBundler();
        bundler.bundle(getOptions(), manifest);
    }

    protected File prebundleScript(File dir) { return new File(dir, PRE_BUNDLE_SH); }

}
