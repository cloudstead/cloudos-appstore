package cloudos.appstore.model.app;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;
import org.cobbzilla.util.io.FileUtil;
import org.cobbzilla.util.json.JsonUtil;
import org.cobbzilla.wizard.model.SemanticVersion;

import java.io.File;
import java.io.FilenameFilter;
import java.util.*;

public class AppLayout {

    public static final String BUNDLE_TARBALL = "bundle.tar.gz";
    public static final String CHEF_DIR = "chef";
    public static final String DATABAGS_DIR = "data_bags";
    public static final String ASSET_PLUGIN_JAR = "plugin.jar";
    public static final String[] ASSET_IMAGE_EXTS = new String[]{"png", "jpg", "jpeg", "gif"};

    public static final FilenameFilter VERSION_DIRNAME_FILTER = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
            try { SemanticVersion.fromString(name); return true; } catch (Exception ignored) {}
            return false;
        }
    };

    @Getter @Setter private File appDir;
    @Getter @Setter private File versionDir;

    public boolean exists() { return versionDir != null && versionDir.exists() && versionDir.isDirectory(); }

    public AppLayout (File appRepository, String name) {
        appDir = new File(appRepository, AppManifest.scrubDirname(name));
        versionDir = getAppActiveVersionDir();
    }

    public AppLayout (File appRepository, String name, String version) {
        appDir = new File(appRepository, AppManifest.scrubDirname(name));
        versionDir = getAppVersionDir(version);
    }

    public AppLayout (File appRepository, AppManifest manifest) {
        appDir = new File(appRepository, manifest.getScrubbedName());
        versionDir = getAppVersionDir(manifest.getScrubbedVersion());
    }

    public File getAppActiveVersionDir () {
        final AppMetadata metadata = AppMetadata.fromJson(appDir);
        if (metadata.isActive()) {
            final File versionDir = new File(appDir, AppManifest.scrubDirname(metadata.getActive_version()));
            return versionDir.exists() && versionDir.isDirectory() ? versionDir : null;
        }
        return null;
    }

    public File getLatestVersionDir () {
        final SortedSet<SemanticVersion> versions = new TreeSet<>();
        final String[] versionDirs = appDir.list(VERSION_DIRNAME_FILTER);
        if (versionDirs != null) {
            for (String versionDir : versionDirs) versions.add(SemanticVersion.fromString(versionDir));
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
    public File getPluginJar   () { return new File(versionDir, AppLayout.ASSET_PLUGIN_JAR); }

    public File getDatabagsDir() { return new File(getChefDir(), AppLayout.DATABAGS_DIR); }

    public File getDatabagFile(String databagName) {
        return new File(new File(getDatabagsDir(), versionDir.getParentFile().getName()), databagName+".json");
    }

    // versionDir/chef/cookbooks
    public File getChefCookbooksDir() { return new File(getChefDir(), "cookbooks"); }

    // versionDir/chef/cookbooks/app
    public File getChefAppCookbookDir() { return new File(getChefCookbooksDir(), appDir.getName()); }

    // versionDir/chef/cookbooks/app/files/default
    public File getChefFilesDir() { return new File(new File(getChefAppCookbookDir(), "files"), "default"); }

    public JsonNode getDatabag(String databagName) {
        final File databagFile = getDatabagFile(databagName);
        if (!databagFile.exists()) return null;
        return JsonUtil.fromJsonOrDie(FileUtil.toStringOrDie(databagFile), JsonNode.class);
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

}
