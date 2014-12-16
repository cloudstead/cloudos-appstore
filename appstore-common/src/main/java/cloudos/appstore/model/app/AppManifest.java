package cloudos.appstore.model.app;

import cloudos.appstore.model.AppMutableData;
import cloudos.appstore.model.AppRuntimeDetails;
import cloudos.appstore.model.ConfigurableAppRuntime;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.cobbzilla.util.collection.ArrayUtil;
import org.cobbzilla.util.io.FileUtil;
import org.cobbzilla.util.json.JsonUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.cobbzilla.util.string.StringUtil.empty;

@ToString
public class AppManifest {

    public static final String CLOUDOS_MANIFEST_JSON = "cloudos-manifest.json";
    public static final String PLUGIN_JAR = "plugin.jar";

    public static AppManifest load(File file) {
        final File manifestFile = file.isDirectory() ? new File(file, AppManifest.CLOUDOS_MANIFEST_JSON) : file;
        return JsonUtil.fromJsonOrDie(FileUtil.toStringOrDie(manifestFile), AppManifest.class);
    }

    @Getter @Setter private String name;
    @JsonIgnore public String getChefName () { return StringUtils.capitalize(getId()); }

    @JsonIgnore @Getter(lazy=true) private final String id = initId();
    private String initId () { return scrub(getName()); }

    public static String scrub(String string) { return string == null ? null : string.replace('-', '_').replaceAll("\\W", ""); }

    public static String scrubDirname(String string) { return string == null ? null : string.replaceAll("[^a-zA-Z_0-9\\.\\-]", ""); }

    @Getter @Setter private String version;

    @JsonIgnore public String getScrubbedName () { return scrubDirname(getName()); }
    @JsonIgnore public String getScrubbedVersion () { return scrubDirname(getVersion()); }

    @Getter @Setter private AppStyle style;
    @Getter @Setter private String parent;
    @JsonIgnore public boolean hasParent () { return !empty(parent); }

    @Getter @Setter private AppUser run_as;
    @Getter @Setter private AppPublisher publisher;
    @Getter @Setter private boolean interactive = false;
    @Getter @Setter private AppMutableData assets;

    @Getter @Setter private AppDatabagDef[] databags;
    public boolean hasDatabags () { return databags != null && databags.length > 0; }

    @Getter @Setter private String[] packages;
    @Getter @Setter private String[] passwords;
    @Getter @Setter private AppGroup[] groups;
    @Getter @Setter private AppUser[] users;

    @Getter @Setter private AppRepository repo;
    @JsonIgnore public boolean hasRepo () { return repo != null; }

    @Getter @Setter private AppTarball tarball;
    @JsonIgnore public boolean hasTarball () { return tarball != null; }

    @Getter @Setter private AppRepository[] supporting_repos;

    @Getter @Setter private AppShellCommand[] prep_code;
    @Getter @Setter private AppShellCommand[] post_install;

    @Getter @Setter private AppDatabase database;
    @JsonIgnore public boolean hasDatabase () { return database != null; }

    @Getter @Setter private AppWeb web;
    @JsonIgnore public boolean hasWeb () { return web != null; }

    @Getter @Setter private Map<String, String> templates;
    @JsonIgnore public boolean hasTemplates () { return templates != null && !templates.isEmpty(); };

    @Getter @Setter private String[] dirs;
    @Getter @Setter private Map<String, String> symlinks;
    @Getter @Setter private Map<String, AppPermission> perms = new LinkedHashMap<>();
    @Getter @Setter private Map<String, String> copy;
    @Getter @Setter private Map<String, String> move;
    @Getter @Setter private Map<String, String> append;

    @Getter @Setter private AppMailbox[] mailboxes;

    @Getter @Setter private String[] sysinit;
    public boolean hasSysinit () { return sysinit != null && sysinit.length > 0; }

    @Getter @Setter private AppService[] services;

    @Getter @Setter private String[] logrotate;
    public void addLogrotate (String path) { logrotate = ArrayUtil.append(logrotate, path); }

    // name of java class within plugin.jar that implements AppRuntime interface
    @Getter @Setter private String plugin = ConfigurableAppRuntime.class.getName();

    @Getter @Setter private AppAuthConfig auth;
    public boolean hasAuth () { return auth != null; }
    public boolean hasUserManagement () { return auth != null && auth.getUser_management() != null; }

    // the chef cookbooks/recipes to install, backup and restore
    @Getter @Setter private AppChefConfig chef = new AppChefConfig();

    @Getter @Setter private AppBackupConfig backup = new AppBackupConfig();
    @Getter @Setter private AppRestoreConfig restore = new AppRestoreConfig();

    @JsonIgnore
    public List<String> getChefInstallRunlist () {
        if (!chef.getInstall().isEmpty()) return chef.getInstall();
        final List<String> defaultRunlist = new ArrayList<>();
        defaultRunlist.add("recipe["+name+"]");
        return defaultRunlist;
    }

    @Setter private String path;

    public String getPath() { return empty(path) ? getDefaultPath() : path; }

    @JsonIgnore
    public AppRuntimeDetails getInstalledAppDetails () {
        return new AppRuntimeDetails(name, getPath(), getHostname(), isInteractive(), getAssets());
    }

    @JsonIgnore
    public String getDefaultPath() {
        if (!isInteractive()) return null;
        switch (style) {
            case rails: return null;
            case nodejs: return null;
            case php: return web.hasVhost() ? null : name;
            case java_webapp: return null;
            case system: return null;
            default: throw new IllegalStateException("getPath: invalid style: "+style);
        }
    }

    @JsonIgnore
    public String getHostname() {
        if (!isInteractive()) return null;
        if (web.getMode() == AppWebMode.proxy_root) return "_root_";
        switch (style) {
            case rails: return name;
            case nodejs: return name;
            case php: return web.hasVhost() ? name : null;
            case java_webapp: return web.getMode().isSeparateHostname() ? name : null;
            case system: return null;
            default: throw new IllegalStateException("getHostname: invalid style: "+style);
        }
    }
    public boolean hasHostname () { return getHostname() != null; }

    public static class AppTarball {
        @Getter @Setter private String url;
        @Getter @Setter private String shasum;
        @Getter @Setter private String to;
        @Getter @Setter private AppTarballCopy[] copy;

        public static class AppTarballCopy {
            @Getter @Setter private String from;
            @Getter @Setter private String to;
        }
    }
}
