package cloudos.appstore.model.app;

import cloudos.appstore.model.AppMutableData;
import cloudos.databag.PortsDatabag;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.cobbzilla.util.io.FileUtil;
import org.cobbzilla.util.json.JsonUtil;
import org.cobbzilla.wizard.model.SemanticVersion;
import rooty.toots.chef.ChefSolo;

import java.io.File;
import java.io.FilenameFilter;
import java.util.*;

import static org.cobbzilla.util.io.FileUtil.abs;
import static org.cobbzilla.util.io.FileUtil.toFileOrDie;
import static org.cobbzilla.util.json.JsonUtil.toJsonOrDie;

@ToString @Slf4j
public class AppLayout {

    public static final String BUNDLE_TARBALL = "bundle.tar.gz";
    public static final String CHEF_DIR = "chef";
    public static final String[] ASSET_IMAGE_EXTS = new String[]{"png", "jpg", "jpeg", "gif"};

    public static final FilenameFilter VERSION_DIRNAME_FILTER = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
            try { return SemanticVersion.isValid(name); } catch (Exception ignored) {}
            return false;
        }
    };

    @Getter @Setter private File appDir;
    @Getter @Setter private String appName;
    @Getter @Setter private File versionDir;

    public boolean exists() { return versionDir != null && versionDir.exists() && versionDir.isDirectory(); }

    public AppLayout (String name, File bundleDir) {
        appName = name;
        versionDir = bundleDir;
    }

    public AppLayout (File appRepository, String name) {
        appDir = new File(appRepository, AppManifest.scrubDirname(name));
        appName = appDir.getName();
        versionDir = getAppActiveVersionDir();
    }

    public AppLayout (File appRepository, String name, String version) {
        appDir = new File(appRepository, AppManifest.scrubDirname(name));
        appName = appDir.getName();
        versionDir = getAppVersionDir(version);
    }

    public AppLayout (File appRepository, AppManifest manifest) {
        appDir = new File(appRepository, manifest.getScrubbedName());
        appName = appDir.getName();
        versionDir = getAppVersionDir(manifest.getScrubbedVersion());
    }

    /**
     * @return a list of all versions, in descending order
     */
    public List<SemanticVersion> getVersions () {
        if (appDir == null || !appDir.exists()) {
            log.warn("getVersions: appDir does not exist: "+abs(appDir));
            return Collections.emptyList();
        }
        final File[] dirs;
        try {
            dirs = FileUtil.listDirs(appDir);
        } catch (Exception e) {
            log.warn("getVersions: Error listing appDir ("+abs(appDir)+"): "+e);
            return Collections.emptyList();
        }
        // list most recent versions first
        final SortedSet<SemanticVersion> versions = new TreeSet<>(SemanticVersion.COMPARE_LATEST_FIRST);
        for (File dir : dirs) {
            if (SemanticVersion.VERSION_PATTERN.matcher(dir.getName()).matches()) {
                versions.add(new SemanticVersion(dir.getName()));
            }
        }
        return new ArrayList<>(versions);
    }

    public File getAppActiveVersionDir () {
        final AppMetadata metadata = getAppMetadata();
        if (metadata.isActive()) {
            final File versionDir = new File(appDir, AppManifest.scrubDirname(metadata.getActive_version()));
            return versionDir.exists() && versionDir.isDirectory() ? versionDir : null;
        }
        return null;
    }

    public AppMetadata getAppMetadata() { return AppMetadata.fromJson(appDir); }

    public File getLatestVersionDir () {
        final SortedSet<SemanticVersion> versions = new TreeSet<>();
        final String[] versionDirs = appDir.list(VERSION_DIRNAME_FILTER);
        if (versionDirs != null) {
            for (String versionDir : versionDirs) versions.add(new SemanticVersion(versionDir));
            return new File(appDir, versions.last().toString());
        }
        return null;
    }

    public File getAppActiveVersionDir(AppManifest manifest) {
        return getAppVersionDir(manifest.getVersion());
    }

    public File getAppVersionDir(String version) {
        return new File(appDir, AppManifest.scrubDirname(version));
    }

    public File getBundleFile  () { return new File(versionDir, AppLayout.BUNDLE_TARBALL); }
    public File getChefDir     () { return new File(versionDir, AppLayout.CHEF_DIR); }
    public File getManifest    () { return new File(versionDir, AppManifest.CLOUDOS_MANIFEST_JSON);  }

    public File getDatabagsDir() { return new File(getChefDir(), ChefSolo.DATABAGS_DIR); }

    public File getDatabagDirForApp(String appName) { return new File(getDatabagsDir(), appName); }

    public File getDatabagFile(String databagName) {
        return new File(getDatabagDirForApp(appName), databagName+".json");
    }

    // versionDir/chef/cookbooks
    public File getChefCookbooksDir() { return new File(getChefDir(), ChefSolo.COOKBOOKS_DIR); }

    // versionDir/chef/cookbooks/app
    public File getChefAppCookbookDir() { return new File(getChefCookbooksDir(), appName); }

    // versionDir/chef/cookbooks/app/files/default
    public File getChefFilesDir() { return new File(new File(getChefAppCookbookDir(), "files"), "default"); }

    public File getPluginJar () { return new File(getChefFilesDir(), AppManifest.PLUGIN_JAR); }

    public JsonNode getDatabag(String databagName) {
        final File databagFile = getDatabagFile(databagName);
        if (!databagFile.exists()) return null;
        return getDatabagNode(databagFile);
    }

    public static JsonNode getDatabagNode(File databagFile) {
        return JsonUtil.fromJsonOrDie(FileUtil.toStringOrDie(databagFile), JsonNode.class);
    }

    public PortsDatabag getPortsDatabag() {
        final File databagFile = getDatabagFile(PortsDatabag.ID);
        if (!databagFile.exists()) return null;
        return JsonUtil.fromJsonOrDie(FileUtil.toStringOrDie(databagFile), PortsDatabag.class);
    }

    public File findDefaultAsset(String asset) {
        // strip file extension if given
        int lastDot = asset.lastIndexOf('.');
        if (lastDot != -1) asset = asset.substring(0, lastDot);

        for (String ext : ASSET_IMAGE_EXTS) {
            final File f = new File(getChefFilesDir(), asset + "." + ext);
            if (f.exists()) return f;
        }
        return null;
    }

    public void writeManifest(AppManifest manifest) {
        final String manifestJson = toJsonOrDie(manifest);
        toFileOrDie(getManifest(), manifestJson);
        toFileOrDie(getDatabagFile(AppManifest.CLOUDOS_MANIFEST_JSON), manifestJson);
    }

    public boolean copyAssets (AppLayout destLayout) {
        try {
            for (String asset : AppMutableData.APP_ASSETS) {
                final File f = findDefaultAsset(asset);
                if (f != null) {
                    final File destDir = destLayout.getChefFilesDir();
                    if (!destDir.exists() && !destDir.mkdirs()) {
                        log.error("copyAssets: Error creating dir: " + abs(destDir));
                        return false;
                    }
                    final File destFile = new File(destDir, f.getName());
                    if (!f.renameTo(destFile)) {
                        log.error("copyAssets: Error renaming file " + abs(f) + " -> " + abs(destFile));
                        return false;
                    }
                }
            }
        } catch (Exception e) {
            log.error("copyAssets: "+e, e);
            return false;
        }
        return true;
    }

}
