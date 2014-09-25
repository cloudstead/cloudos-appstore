package cloudos.appstore.bundler;

import cloudos.appstore.model.app.AppManifest;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.cobbzilla.util.io.FileUtil;
import org.cobbzilla.util.json.JsonUtil;
import org.kohsuke.args4j.CmdLineParser;

@Slf4j
public class BundlerMain {

    @Getter @Setter private String[] args;

    private final BundlerOptions options = new BundlerOptions();
    private final CmdLineParser parser = new CmdLineParser(options);

    public BundlerMain(String[] args) { this.args = args; }

    public static void main (String[] args) throws Exception {
        final BundlerMain main = new BundlerMain(args);
        main.run();
    }

    public void run () throws Exception {
        parser.parseArgument(args);

        final AppManifest manifest = JsonUtil.fromJson(FileUtil.toString(options.getManifest()), AppManifest.class);
        final AppBundlerFactory factory = new DefaultAppBundlerFactory();
        final AppBundler bundler = factory.getBundler(manifest);
        bundler.bundle(options, manifest);
    }

}
