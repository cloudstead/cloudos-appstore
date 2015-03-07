package cloudos.appstore.model.app;

import cloudos.appstore.model.AppMutableData;
import cloudos.appstore.model.AppRuntimeDetails;
import cloudos.appstore.model.ConfigurableAppRuntime;
import cloudos.appstore.model.app.filter.AppFilterConfig;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.cobbzilla.util.collection.ArrayUtil;
import org.cobbzilla.util.io.FileUtil;
import org.cobbzilla.util.json.JsonUtil;
import org.cobbzilla.wizard.model.SemanticVersion;

import java.io.File;
import java.util.*;

import static org.cobbzilla.util.string.StringUtil.empty;

@ToString
public class AppManifest {

    public static final String CLOUDOS_MANIFEST_JSON = "cloudos-manifest.json";
    public static final String PLUGIN_JAR = "plugin.jar";
    public static final String ROOT_HOSTNAME = "_root_";

    public static AppManifest load(File file) {
        final File manifestFile = file.isDirectory() ? new File(file, AppManifest.CLOUDOS_MANIFEST_JSON) : file;
        return JsonUtil.fromJsonOrDie(FileUtil.toStringOrDie(manifestFile), AppManifest.class);
    }

    @Getter @Setter private String name;
    @JsonIgnore public String getChefName () { return StringUtils.capitalize(getId()); }

    @Getter @Setter private boolean validation_script = false;

    @JsonIgnore @Getter(lazy=true) private final String id = initId();
    private String initId () { return scrub(getName()); }

    public static String scrub(String string) { return string == null ? null : string.replace('-', '_').replaceAll("\\W", ""); }

    public static String scrubDirname(String string) { return string == null ? null : string.replaceAll("[^a-zA-Z_0-9\\.\\-]", ""); }

    @Getter @Setter private String version;

    @JsonIgnore public String getScrubbedName () { return scrubDirname(getName()); }
    @JsonIgnore public String getScrubbedVersion () { return scrubDirname(getVersion()); }
    @JsonIgnore public SemanticVersion getSemanticVersion () { return SemanticVersion.fromString(version); }

    @Getter @Setter private AppStyle style;
    @Getter @Setter private String parent;
    @JsonIgnore public boolean hasParent () { return !empty(parent); }

    @Getter @Setter private AppUser run_as;
    @Getter @Setter private AppPublisher publisher;
    @Getter @Setter private boolean interactive = false;

    @Getter @Setter private AppMutableData assets;
    @JsonIgnore public boolean hasAssets () { return assets != null; }

    @Getter @Setter private AppDatabagDef[] config;
    public boolean hasDatabags () { return config != null && config.length > 0; }

    @Getter @Setter private AppShellCommand[] pre_package;
    @Getter @Setter private String[] packages;
    @Getter @Setter private AppShellCommand[] post_package;

    @Getter @Setter private String[] passwords;
    @Getter @Setter private AppGroup[] groups;
    @Getter @Setter private AppUser[] users;
    @Getter @Setter private Map<String, AppCloudOsGroup> cloudos_groups = new LinkedHashMap<>();

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

    @JsonIgnore public String getLocalMount () {
        if (web == null || empty(web.getLocal_mount())) return null;
        return web.getLocal_mount().replaceAll("@name", name);
    }

    @JsonIgnore public String getNormalizedLocalMount () {
        String m = getLocalMount();
        if (m == null) m = "/";
        if (!m.startsWith("/")) m = "/" + m;
        if (!m.endsWith("/")) m += "/";
        return m;
    }

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
    public void addSysinit (String spec) { sysinit = ArrayUtil.append(sysinit, spec); }

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
        if (!isInteractive() || !hasWeb()) return null;
        if (web.hasMount()) return web.getMount();
        if (web.getMode().isRoot()) return null;
        return name;
    }

    @JsonIgnore
    public String getHostname() {
        if (!isInteractive() || !hasWeb()) return null;
        if (web.getMode().isRoot()) return ROOT_HOSTNAME;
        if (web.getMode().isSeparateHostname()) return name;
        return null;
    }
    public boolean hasHostname () { return getHostname() != null; }

    @JsonIgnore public boolean hasFilters() { return hasWeb() && web.hasFilters(); }
    public AppFilterConfig getFilterConfig(String uri) { return hasWeb() ? web.getFilterConfig(uri) : null; }

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
