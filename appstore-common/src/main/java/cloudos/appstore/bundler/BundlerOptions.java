package cloudos.appstore.bundler;

import cloudos.appstore.client.AppStoreApiClient;
import cloudos.appstore.model.AppVisibility;
import cloudos.appstore.model.app.AppManifest;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.cobbzilla.util.http.ApiConnectionInfo;
import org.cobbzilla.util.json.JsonUtil;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.cobbzilla.util.daemon.ZillaRuntime.die;
import static org.cobbzilla.util.daemon.ZillaRuntime.empty;
import static org.cobbzilla.util.io.FileUtil.abs;
import static org.cobbzilla.util.io.FileUtil.mkdirOrDie;
import static org.cobbzilla.util.reflect.ReflectionUtil.copy;

@NoArgsConstructor
public class BundlerOptions {

    public BundlerOptions(BundlerOptions options) { copy(this, options); }

    public static final File defaultManifest = new File(System.getProperty("user.dir"), AppManifest.CLOUDOS_MANIFEST_JSON);

    public static final String ENV_CREDENTIALS_FILE = "CBUNDLER_APPSTORE_CREDENTIALS";
    public static final String DEFAULT_CREDENTIALS_FILE = ".cbundler.json";
    public static final String DEFAULT_OUTPUT_DIR = "dist";

    public static final String USAGE_MANIFEST = "The manifest file for the app. Default is a cloudos-manifest.json file in the current directory.";
    public static final String OPT_MANIFEST = "-m";
    public static final String LONGOPT_MANIFEST = "--manifest";
    @Option(name=OPT_MANIFEST, aliases=LONGOPT_MANIFEST, usage=USAGE_MANIFEST)
    @Getter @Setter private File manifest = defaultManifest;

    public File getManifestParent() { return manifest.isDirectory() ? manifest : manifest.getParentFile(); }

    public static final String USAGE_OUTPUT_DIR = "The output directory to build the bundle. Default is a directory named 'dist' within same directory as the manifest file.";
    public static final String OPT_OUTPUT_DIR = "-o";
    public static final String LONGOPT_OUTPUT_DIR = "--output";
    @Option(name=OPT_OUTPUT_DIR, aliases=LONGOPT_OUTPUT_DIR, usage=USAGE_OUTPUT_DIR)
    @Setter private File outputDir = null;
    public File getOutputDir () { return !empty(outputDir) ? outputDir : new File(getManifest().getParentFile(), DEFAULT_OUTPUT_DIR); }

    public static final String USAGE_UPLOAD = "Upload the bundle to the app store. Place credentials and API endpoint in  ~/" + DEFAULT_CREDENTIALS_FILE + ", or define the " + ENV_CREDENTIALS_FILE + " environment variable as the path to the credentials file";
    public static final String OPT_UPLOAD = "-u";
    public static final String LONGOPT_UPLOAD = "--upload";
    @Option(name=OPT_UPLOAD, aliases=LONGOPT_UPLOAD, usage=USAGE_UPLOAD)
    @Getter @Setter private boolean upload;

    public static final String USAGE_PUBLISH = "Publish the bundle in the app store (implies "+OPT_UPLOAD+"/"+LONGOPT_UPLOAD+")";
    public static final String OPT_PUBLISH = "-p";
    public static final String LONGOPT_PUBLISH = "--publish";
    @Option(name=OPT_PUBLISH, aliases=LONGOPT_PUBLISH, usage=USAGE_PUBLISH)
    @Getter @Setter private boolean publish;

    public static final String USAGE_PUBLISHER = "When uploading/publishing, use this app store Publisher. Default is the same as the 'user' found in the credentials file.";
    public static final String OPT_PUBLISHER = "-P";
    public static final String LONGOPT_PUBLISHER = "--publisher";
    @Option(name=OPT_PUBLISHER, aliases=LONGOPT_PUBLISHER, usage=USAGE_PUBLISHER)
    @Getter @Setter private String publisher;

    public String getPublisherName () {
        return !requiresAppstoreCredentials() ? null : empty(publisher) ? getCredentials().getUser() : publisher;
    }

    public static final String USAGE_VISIBILITY = "When uploading/publishing a new app, use this visibility level. Changing the visibility level will affect all versions of the app.";
    public static final String OPT_VISIBILITY = "-V";
    public static final String LONGOPT_VISIBILITY = "--visibility";
    @Option(name=OPT_VISIBILITY, aliases=LONGOPT_VISIBILITY, usage=USAGE_VISIBILITY)
    @Getter @Setter private AppVisibility visibility = null;
    public boolean hasVisibility () { return !empty(visibility); }

    public String getAppSourceDir() {
        return (getManifest().isDirectory() ? abs(getManifest()) : abs(getManifest().getParentFile())) + "/";
    }

    public static final String BUILD_DIR = "build";
    public File getBuildDir() { return mkdirOrDie(new File(getOutputDir(), BUILD_DIR)); }
    public String getBuildBase() { return abs(getBuildDir()) + "/"; }

    public AppStoreApiClient getAppStoreClient() { return new AppStoreApiClient(getCredentials()); }

    private ApiConnectionInfo info;
    protected ApiConnectionInfo getCredentials() {
        if (!requiresAppstoreCredentials()) return null; // no need for these if not interacting with app store
        if (info == null) {
            String credsPath = System.getenv(ENV_CREDENTIALS_FILE);
            if (credsPath == null) credsPath = System.getProperty("user.home") + "/" + DEFAULT_CREDENTIALS_FILE;
            File credsFile = new File(credsPath);
            if (!credsFile.exists() || !credsFile.canRead()) die("Credentials file is not readable: " + abs(credsFile));
            try {
                info = JsonUtil.fromJson(credsFile, ApiConnectionInfo.class);
            } catch (Exception e) {
                die("Error reading credentials file (" + abs(credsFile) + "): " + e, e);
            }
        }
        return info;
    }

    public boolean requiresAppstoreCredentials() { return upload || publish; }
    public boolean shouldUpload() { return upload || publish; }

    public String[] toArgs() {
        final List<String> args = new ArrayList<>();
        args.add(OPT_MANIFEST);
        args.add(abs(manifest));
        if (!empty(outputDir)) {
            args.add(OPT_OUTPUT_DIR);
            args.add(abs(outputDir));
        }
        if (!empty(publisher)) {
            args.add(OPT_PUBLISHER);
            args.add(publisher);
        }
        if (upload) args.add(OPT_UPLOAD);
        if (publish) args.add(OPT_PUBLISH);
        return args.toArray(new String[args.size()]);
    }
}
