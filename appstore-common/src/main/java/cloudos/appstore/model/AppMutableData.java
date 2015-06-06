package cloudos.appstore.model;

import cloudos.appstore.model.app.AppLayout;
import cloudos.appstore.model.app.AppManifest;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.cobbzilla.util.collection.FailedOperationCounter;
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
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.cobbzilla.util.daemon.ZillaRuntime.die;
import static org.cobbzilla.util.daemon.ZillaRuntime.empty;
import static org.cobbzilla.util.io.FileUtil.abs;
import static org.cobbzilla.util.io.FileUtil.isReadableNonEmptyFile;
import static org.cobbzilla.util.reflect.ReflectionUtil.copy;
import static org.cobbzilla.wizard.model.BasicConstraintConstants.HASHEDPASSWORD_MAXLEN;
import static org.cobbzilla.wizard.model.BasicConstraintConstants.URL_MAXLEN;

/**
 * Data that can change within a CloudAppVersion that is marked LIVE
 * Changes are limited because PublishedApps may refer to these versions
 */
@Embeddable @NoArgsConstructor @Accessors(chain=true) @Slf4j
public class AppMutableData {

    public static final String[] APP_ASSETS = {"taskbarIcon", "smallIcon", "largeIcon"};

    public AppMutableData (AppMutableData other) { copy(this, other); }

    @HasValue(message=ERR_APP_BLURB_EMPTY)
    @Size(max=APP_BLURB_MAXLEN, message=ERR_APP_BLURB_LENGTH)
    @Column(nullable=false)
    @Getter @Setter private String blurb;

    @HasValue(message=ERR_APP_DESCRIPTION_EMPTY)
    @Size(max=APP_DESCRIPTION_MAXLEN, message=ERR_APP_DESCRIPTION_LENGTH)
    @Column(nullable=false)
    @Getter @Setter private String description;

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

    private static FailedOperationCounter<String> assetLoadFailures = new FailedOperationCounter<>();

    // does the manifest define this asset?
    public static boolean downloadAssetAndUpdateManifest(AppManifest manifest, String asset, AppLayout layout, String urlBase) {

        // ensure assets exists in the manifest, even as an empty object
        AppMutableData assets = manifest.getAssets();
        if (assets == null) {
            assets = new AppMutableData();
            manifest.setAssets(assets);
        }

        // ensure urlBase ends with a slash
        if (!urlBase.endsWith("/")) urlBase += "/";

        // what's in the manifest?
        Object urlValue = ReflectionUtil.get(assets, asset + "Url");
        Object shaValue = ReflectionUtil.get(assets, asset + "UrlSha");

        // check local stuff first to see what we have...
        String localUrl = urlBase + asset;
        File localFile = layout.findLocalAsset(asset);

        // if the local file is valid, simply ensure that the url and sha in the manifest match this
        if (localFile != null && isReadableNonEmptyFile(localFile)) {

            // check the SHA
            if (shaValue != null && !shaValue.equals(ShaUtil.sha256_file(localFile))) {
                // bad SHA. remove asset and clear manifest.
                ReflectionUtil.setNull(assets, asset + "Url", String.class);
                ReflectionUtil.setNull(assets, asset + "UrlSha", String.class);
                deleteQuietly(localFile);
                return true;
            }

            if (urlValue != null && (urlValue.equals(localUrl) || urlValue.toString().startsWith(localUrl+"."))) {
                // URL is local, sha matches, nothing changes
                return false;

            } else {
                // we have a local file, sha is OK, but URL is different. update URL.
                localUrl = urlBase + localFile.getName();
                ReflectionUtil.set(assets, asset+"Url", localUrl);
                return true;
            }
        }

        // Nothing local, try to grab the remote, validate sha, copy to local and update manifest
        if (urlValue != null) {
            final String assetUrl = urlValue.toString();
            final String ext = URIUtil.getFileExt(assetUrl);
            if (!isValidImageExtention(ext)) die("Invalid file extension for asset (must be one of: " + Arrays.toString(AppLayout.ASSET_IMAGE_EXTS) + "): " + assetUrl);

            File tempAssetFile = null;
            try {
                if (assetLoadFailures.tooManyFailures(assetUrl)) {
                    log.warn("Asset (" + asset + ") with URL=" + assetUrl + " has failed too many times (>="+assetLoadFailures.getMaxFailures()+"), retry interval "+assetLoadFailures.getExpiration()+"ms)");
                    return false;

                } else {
                    try {
                        tempAssetFile = HttpUtil.url2file(assetUrl);
                    } catch (Exception e) {
                        // error loading URL. leave it be for now, may be a temporary issue with the URL
                        log.warn("Asset (" + asset + ") could not be loaded from: " + assetUrl + ": " + e);
                        assetLoadFailures.fail(assetUrl);
                        return false;
                    }
                }

                final String tempAssetSha = ShaUtil.sha256_file(tempAssetFile);
                if (!empty(shaValue) && !tempAssetSha.equals(shaValue)) {
                    log.warn("Asset " + asset + " (" + abs(tempAssetFile) + ") failed SHA validation, not using it");
                    ReflectionUtil.setNull(assets, asset + "Url", String.class);
                    ReflectionUtil.setNull(assets, asset + "UrlSha", String.class);
                    return true;

                } else if (empty(shaValue)) {
                    // sha wasn't set, so set it now
                    shaValue = tempAssetSha;
                }

                localFile = new File(layout.getChefFilesDir(), asset + "." + ext);
                try {
                    FileUtils.copyFile(tempAssetFile, localFile);
                } catch (IOException e) {
                    die("Error copying " + abs(tempAssetFile) + " -> " + abs(localFile) + ": " + e);
                }
            } finally {
                deleteQuietly(tempAssetFile);
            }

            localUrl = urlBase + manifest.getScrubbedName() + "/" + localFile.getName();

            ReflectionUtil.set(assets, asset + "Url", localUrl);
            ReflectionUtil.set(assets, asset + "UrlSha", shaValue);
            return true;

        } else {
            // no local file and no URL in the manifest, nothing to do, nothing changes
            return false;
        }
    }

}
