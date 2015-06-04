package cloudos.appstore.test;

import cloudos.appstore.bundler.BundlerMain;
import cloudos.appstore.bundler.BundlerOptions;
import cloudos.appstore.model.AppMutableData;
import cloudos.appstore.model.app.AppLevel;
import cloudos.appstore.model.app.AppManifest;
import cloudos.appstore.model.app.AppStyle;
import cloudos.appstore.model.app.config.AppConfigMetadata;
import org.apache.commons.io.FileUtils;
import org.cobbzilla.util.io.FileUtil;
import org.cobbzilla.util.io.StreamUtil;
import org.cobbzilla.util.io.Tarball;
import org.cobbzilla.util.io.TempDir;
import org.cobbzilla.util.json.JsonUtil;
import org.cobbzilla.util.security.ShaUtil;
import org.cobbzilla.util.system.PortPicker;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ResourceHandler;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.cobbzilla.util.daemon.ZillaRuntime.die;
import static org.cobbzilla.util.daemon.ZillaRuntime.empty;
import static org.cobbzilla.util.io.FileUtil.abs;
import static org.cobbzilla.util.io.StreamUtil.loadResourceAsString;

public class AssetWebServer {

    protected Server server;
    protected int port;
    protected File rootDir;

    protected Map<String, TestApp> apps = new HashMap<>();

    public void start () throws Exception {
        port = PortPicker.pick();
        rootDir = FileUtil.createTempDir(getClass().getName());

        // Set up jetty server to serve tarball and icon png
        server = new Server(port);

        final ResourceHandler handler = new ResourceHandler();
        handler.setResourceBase(abs(rootDir));
        server.setHandler(handler);

        server.start();
    }

    public void stop () throws Exception {
        server.stop();
        FileUtils.deleteDirectory(rootDir);
    }

    public TestApp buildSimpleApp(String name, String version, AppLevel level) {

        final AppManifest manifest = new AppManifest();
        manifest.setName(name);
        manifest.setVersion(version);
        manifest.setLevel(level);
        manifest.setStyle(AppStyle.chef);

        TempDir tempDir = null;
        try {
            tempDir = new TempDir();
            final File manifestFile = new File(tempDir, AppManifest.CLOUDOS_MANIFEST_JSON);
            FileUtil.toFile(manifestFile, JsonUtil.toJson(manifest));

            return buildAppTarball(manifestFile);

        } catch (Exception e) {
            return die("Error building app: "+e, e);

        } finally {
            FileUtils.deleteQuietly(tempDir);
        }
    }

    public TestApp buildAppTarball(File manifestFile) throws Exception {
        return buildAppTarball(manifestFile, null, null);
    }

    public TestApp buildAppTarball(String manifestResourcePath,
                                   String appConfigMetadataPath,
                                   String iconResourcePath) throws Exception {

        // Write manifest for test app to a temp dir
        final File appTemp = FileUtil.createTempDir("appTemp");
        final File manifestFile = new File(appTemp, AppManifest.CLOUDOS_MANIFEST_JSON);
        final String manifestData = loadResourceAsString(manifestResourcePath).replace("@@PORT@@", String.valueOf(port));
        FileUtil.toFile(manifestFile, manifestData);

        return buildAppTarball(manifestFile, appConfigMetadataPath, iconResourcePath);
    }

    public TestApp buildAppTarball(File manifestFile,
                                   String appConfigMetadataPath,
                                   String iconResourcePath) throws Exception {

        final File appTemp = manifestFile.getParentFile();
        final AppManifest appManifest = AppManifest.load(manifestFile);
        if (!empty(appConfigMetadataPath)) {
            final String configMetadata = loadResourceAsString(appConfigMetadataPath);
            final File configDir = new File(appTemp, "config");
            FileUtil.toFile(new File(configDir, AppConfigMetadata.CONFIG_METADATA_JSON), configMetadata);
        }

        File iconFile = null;
        if (!empty(iconResourcePath)) {
            // Copy icon file to doc root
            final String iconBaseName = new File(iconResourcePath).getName();
            iconFile = new File(rootDir, iconBaseName);
            FileUtils.copyFile(StreamUtil.loadResourceAsFile(iconResourcePath), iconFile);

            // update manifest
            if (!appManifest.hasAssets()) appManifest.setAssets(new AppMutableData());
            final AppMutableData assets = appManifest.getAssets();
            final String url = getUrl(iconBaseName);
            final String sha = ShaUtil.sha256_file(iconFile);
            assets.setTaskbarIconUrl(url);
            assets.setTaskbarIconUrlSha(sha);
            assets.setSmallIconUrl(url);
            assets.setSmallIconUrlSha(sha);
            assets.setLargeIconUrl(url);
            assets.setLargeIconUrlSha(sha);
            FileUtil.toFile(manifestFile, JsonUtil.toJson(appManifest));
        }

        final File bundleDir = FileUtil.createTempDir("bundleDir");

        // Run the bundler on our test manifest
        final BundlerMain main = new BundlerMain(new String[] {
                BundlerOptions.OPT_MANIFEST, abs(manifestFile),
                BundlerOptions.OPT_OUTPUT_DIR, abs(bundleDir)
        });
        main.runOrDie();

        // Roll the tarball into its place under the doc root
        final String tarballName = getBundleFilename(appManifest);
        final File tarball = new File(rootDir, tarballName);
        Tarball.roll(tarball, bundleDir);

        // Save the URL and shasum
        final String bundleUrl = getUrl(tarballName);
        final String bundleUrlSha = ShaUtil.sha256_file(tarball);

        final TestApp testApp = new TestApp()
                .setManifest(appManifest)
                .setBundleUrl(bundleUrl)
                .setBundleUrlSha(bundleUrlSha)
                .setIconFile(iconFile)
                .setTarball(tarball);
        apps.put(testApp.getNameAndVersion(), testApp);
        return testApp;
    }

    public String getBundleFilename(AppManifest manifest) {
        return manifest.getName() + "-" + manifest.getVersion() + "-bundle.tar.gz";
    }

    public String getBaseUrl() { return "http://127.0.0.1:" + port; }

    public String getUrl(String path) { return getBaseUrl() + "/" + path; }

    public TestApp getApp(AppManifest manifest) {
        return apps.get(manifest.getName() + "/" + manifest.getVersion());
    }

    public String getBundleUrl (AppManifest manifest) { return getApp(manifest).getBundleUrl(); }

    public String getBundleSha (AppManifest manifest) { return getApp(manifest).getBundleUrlSha(); }

}
