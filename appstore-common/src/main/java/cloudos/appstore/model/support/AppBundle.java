package cloudos.appstore.model.support;

import cloudos.appstore.model.AppMutableData;
import cloudos.appstore.model.app.AppLayout;
import cloudos.appstore.model.app.AppManifest;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.cobbzilla.util.http.HttpUtil;
import org.cobbzilla.util.io.Tarball;
import org.cobbzilla.wizard.validation.ConstraintViolationBean;

import java.io.File;
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
                      String assetUrlBase,
                      List<ConstraintViolationBean> violations) {

        // Download the tarball
        final String suffix = url.substring(url.lastIndexOf('.'));
        File tempTarball = null;
        try {
            tempTarball = File.createTempFile("app-tempTarball-", suffix);
            HttpUtil.url2file(url, tempTarball);

        } catch (Exception e) {
            final String msg = "{appBundle.error.downloadingTarball}";
            violations.add(new ConstraintViolationBean(msg));
            deleteQuietly(tempTarball);
            cleanup(); die(msg, e); return;
        }

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
        try {
            AppMutableData.downloadAssetsAndUpdateManifest(manifest, layout, assetUrlBase);
        } catch (Exception e) {
            final String msg = "{appBundle.error.processingAssets}";
            violations.add(new ConstraintViolationBean(msg));
            cleanup(); die(msg, e); return;
        }

        // disable requiring an icon or other various asset combos.
        // we can always display the app name. it's lame but it means less is mandated of apps.
        // if we do begin enforcing, it should be in common code that the app bundler can also use, to be consistent
//        final AppMutableData assets = manifest.getAssets();
//        if (manifest.isInteractive() && !assets.hasTaskbarIconUrl()) violations.add(new ConstraintViolationBean("{appBundle.error.taskbarIconUrl.empty"));

        if (!empty(violations)) {
            cleanup(); die("{appBundle.error.validation}"); return;
        }
    }

    public void writeManifest() {
        new AppLayout(manifest.getScrubbedName(), bundleDir).writeManifest(manifest);
    }
}
