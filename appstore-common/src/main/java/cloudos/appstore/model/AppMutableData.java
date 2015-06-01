package cloudos.appstore.model;

import cloudos.appstore.model.app.AppLayout;
import cloudos.appstore.model.app.AppManifest;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.cobbzilla.util.http.HttpUtil;
import org.cobbzilla.util.http.URIUtil;
import org.cobbzilla.util.reflect.ReflectionUtil;
import org.cobbzilla.util.security.ShaUtil;
import org.cobbzilla.wizard.validation.HasValue;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.Size;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static cloudos.appstore.ValidationConstants.*;
import static org.cobbzilla.util.daemon.ZillaRuntime.die;
import static org.cobbzilla.util.daemon.ZillaRuntime.empty;
import static org.cobbzilla.util.io.FileUtil.abs;
import static org.cobbzilla.util.io.FileUtil.mkdirOrDie;
import static org.cobbzilla.wizard.model.BasicConstraintConstants.HASHEDPASSWORD_MAXLEN;
import static org.cobbzilla.wizard.model.BasicConstraintConstants.URL_MAXLEN;

/**
 * Data that can change within a CloudAppVersion that is marked LIVE
 * Changes are limited because PublishedApps may refer to these versions
 */
@Embeddable @Accessors(chain=true)
public class AppMutableData {

    public static final String[] APP_ASSETS = {"taskbarIcon", "smallIcon", "largeIcon"};

    @HasValue(message=ERR_APP_BLURB_EMPTY)
    @Size(max=APP_BLURB_MAXLEN, message=ERR_APP_BLURB_LENGTH)
    @Column(nullable=false)
    @Getter @Setter private String blurb;

    @HasValue(message=ERR_APP_DESCRIPTION_EMPTY)
    @Size(max=APP_DESCRIPTION_MAXLEN, message=ERR_APP_DESCRIPTION_LENGTH)
    @Column(nullable=false)
    @Getter @Setter private String description;

    @Size(max=APP_METADATA_MAXLEN, message=ERR_APP_METADATA_LENGTH)
    @Getter @Setter private String metadata;

    @Size(max=APP_TB_ICON_ALT_TEXT_LENGTH, message=ERR_APP_TB_ICON_ALT_TEXT_LENGTH)
    @Column(length=APP_TB_ICON_ALT_TEXT_LENGTH)
    @Getter @Setter private String taskbarIconAltText;

    @Size(max=URL_MAXLEN, message=ERR_APP_TB_ICON_URL_LENGTH)
    @Column(length=URL_MAXLEN)
    @Getter @Setter private String taskbarIconUrl;
    public boolean hasTaskbarIconUrl () { return !empty(taskbarIconUrl); }

    @Size(max=HASHEDPASSWORD_MAXLEN, message=ERR_APP_TB_ICON_SHA_LENGTH)
    @Column(length=HASHEDPASSWORD_MAXLEN)
    @Getter @Setter private String taskbarIconUrlSha;

    @HasValue(message=ERR_APP_SM_ICON_URL_EMPTY)
    @Size(max=URL_MAXLEN, message=ERR_APP_SM_ICON_URL_LENGTH)
    @Column(nullable=false, length=URL_MAXLEN)
    @Getter @Setter private String smallIconUrl;
    public boolean hasSmallIconUrl () { return !empty(smallIconUrl); }

    @HasValue(message=ERR_APP_SM_ICON_SHA_EMPTY)
    @Size(max=HASHEDPASSWORD_MAXLEN, message=ERR_APP_SM_ICON_SHA_LENGTH)
    @Column(nullable=false, length=HASHEDPASSWORD_MAXLEN)
    @Getter @Setter private String smallIconUrlSha;

    @HasValue(message=ERR_APP_LG_ICON_URL_EMPTY)
    @Size(max=URL_MAXLEN, message=ERR_APP_LG_ICON_URL_LENGTH)
    @Column(nullable=false, length=URL_MAXLEN)
    @Getter @Setter private String largeIconUrl;
    public boolean hasLargeIconUrl () { return !empty(largeIconUrl); }

    @HasValue(message=ERR_APP_LG_ICON_SHA_EMPTY)
    @Size(max=HASHEDPASSWORD_MAXLEN, message=ERR_APP_LG_ICON_SHA_LENGTH)
    @Column(nullable=false, length=HASHEDPASSWORD_MAXLEN)
    @Getter @Setter private String largeIconUrlSha;

    public static boolean isValidImageExtention(String ext) {
        for (String validExt : AppLayout.ASSET_IMAGE_EXTS) {
            if (ext.equals(validExt)) return true;
        }
        return false;
    }

    public static void downloadAssetsAndUpdateManifest(AppManifest manifest, AppLayout layout, String urlBase) throws Exception {
        boolean assetChanged = false;
        for (String asset : APP_ASSETS) {
            assetChanged = downloadAssetAndUpdateManifest(manifest, asset, layout, urlBase) || assetChanged;
        }
        if (assetChanged) {
            // rewrite manifest with new asset URLs
            layout.writeManifest(manifest);
        }
    }

    public static boolean downloadAssetAndUpdateManifest(AppManifest manifest, String asset, AppLayout layout, String urlBase) {
        // does the manifest define this asset?
        File assetFile = null;
        String sha = null;

        AppMutableData assets = manifest.getAssets();
        if (assets == null) {
            assets = new AppMutableData();
            manifest.setAssets(assets);
        }

        final Object value = ReflectionUtil.get(assets, asset + "Url");
        final Object shaValue = ReflectionUtil.get(assets, asset + "UrlSha");
        if (value != null) {
            final String assetUrl = value.toString();
            final String ext = URIUtil.getFileExt(assetUrl);
            if (!isValidImageExtention(ext)) {
                die("Invalid file extension for asset (must be one of: " + Arrays.toString(AppLayout.ASSET_IMAGE_EXTS) + "): " + assetUrl);
            }
            assetFile = new File(layout.getChefFilesDir(), asset + "." + ext);
            mkdirOrDie(assetFile.getParentFile());

            try {
                HttpUtil.url2file(assetUrl, assetFile);
            } catch (IOException e) {
                die("Asset (" + asset + ") could not be loaded from: " + assetUrl, e);
            }
            if (!empty(shaValue)) sha = shaValue.toString();
        }

        // no asset URL defined, check the app cookbook's "files/default" directory for a default asset
        if (assetFile == null) assetFile = layout.findDefaultAsset(asset);

        if (assetFile == null) return false;

        // calculate sha, validate if manifest specified one
        final String fileSha = ShaUtil.sha256_file(assetFile);
        if (!empty(sha) && !fileSha.equals(sha)) die("Asset (" + abs(assetFile) + " had an invalid SHA sum");

        if (!urlBase.endsWith("/")) urlBase += "/";
        ReflectionUtil.set(assets, asset + "Url", urlBase + manifest.getScrubbedName() + "/" + assetFile.getName());
        ReflectionUtil.set(assets, asset + "UrlSha", fileSha);
        return true;
    }

}
