package cloudos.appstore.model.support;

import cloudos.appstore.model.AppMutableData;
import cloudos.appstore.model.app.AppLayout;
import cloudos.appstore.model.app.AppManifest;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.cobbzilla.util.http.HttpUtil;
import org.cobbzilla.util.io.StreamUtil;
import org.cobbzilla.util.io.Tarball;
import org.cobbzilla.util.string.Base64;
import org.cobbzilla.wizard.validation.ConstraintViolationBean;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.cobbzilla.util.daemon.ZillaRuntime.die;
import static org.cobbzilla.util.daemon.ZillaRuntime.empty;
import static org.cobbzilla.util.security.ShaUtil.sha256_file;

@Accessors(chain=true)
public class AppBundle {

    @Getter @Setter private File bundleDir;
    @Getter @Setter private AppManifest manifest;

    public void cleanup() { deleteQuietly(bundleDir); }

    public AppBundle (String url,
                      String urlSha,
                      AppAssetUrlGenerator assetUrlGenerator,
                      List<ConstraintViolationBean> violations) {

        final File tempTarball = assembleTarball(url, violations);
        if (tempTarball == null) return;

        // Validate shasum
        if (!empty(urlSha) && !sha256_file(tempTarball).equals(urlSha)) {
            final String msg = "{appBundle.error.shaMismatch}";
            violations.add(new ConstraintViolationBean(msg));
            cleanup(); die(msg); return;
        }

        // Unroll it...
        try {
            bundleDir = Tarball.unroll(tempTarball);

        } catch (Exception e) {
            final String msg = "{appBundle.error.unpackingTarball}";
            violations.add(new ConstraintViolationBean(msg));
            cleanup(); die(msg, e); return;

        } finally {
            deleteQuietly(tempTarball);
        }

        // load the manifest
        try {
            manifest = AppManifest.load(bundleDir);
        } catch (Exception e) {
            final String msg = "{appBundle.error.readingManifest}";
            violations.add(new ConstraintViolationBean(msg));
            cleanup(); die(msg, e); return;
        }

        // creates appDir and versionDir within the tempRepository
        final AppLayout layout = new AppLayout(manifest.getScrubbedName(), bundleDir);

        // validate assets (download remote assets so they can be hosted locally, rewrite manifest as needed)
        final String assetUrlBase = assetUrlGenerator.generateBaseUrl(manifest.getScrubbedName(), manifest.getScrubbedVersion());
        try {
            AppMutableData.downloadAssetsAndUpdateManifest(manifest, layout, assetUrlBase);
        } catch (Exception e) {
            final String msg = "{appBundle.error.processingAssets}";
            violations.add(new ConstraintViolationBean(msg));
            cleanup(); die(msg, e); return;
        }

        if (!empty(violations)) {
            cleanup(); die("{appBundle.error.validation}"); return;
        }
    }

    public static final String BASE64_PREFIX = "base64://";

    protected File assembleTarball(String url, List<ConstraintViolationBean> violations) {

        final String suffix;
        File tempTarball = null;

        try {
            if (url.startsWith(BASE64_PREFIX)) {
                // URL *is* the tarball
                // todo: use streaming APIs, this is incredibly memory-intensive
                suffix = ".tar.gz";
                final byte[] bytes = Base64.decode(url.substring(BASE64_PREFIX.length()), Base64.DONT_GUNZIP);
                tempTarball = File.createTempFile("app-tempTarball-", suffix);
                try (FileOutputStream out = new FileOutputStream(tempTarball)) {
                    StreamUtil.copyLarge(new ByteArrayInputStream(bytes), out);
                }

            } else {
                // Download the tarball
                suffix = url.substring(url.lastIndexOf('.'));
                tempTarball = File.createTempFile("app-tempTarball-", suffix);
                HttpUtil.url2file(url, tempTarball);
            }
        } catch (Exception e) {
            final String msg = "{appBundle.error.downloadingTarball}";
            violations.add(new ConstraintViolationBean(msg));
            deleteQuietly(tempTarball);
            cleanup();
            die(msg, e);
            return null;
        }
        return tempTarball;
    }

    public void writeManifest() {
        new AppLayout(manifest.getScrubbedName(), bundleDir).writeManifest(manifest);
    }
}
