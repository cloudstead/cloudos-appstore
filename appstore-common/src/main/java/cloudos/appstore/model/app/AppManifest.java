package cloudos.appstore.model.app;

import cloudos.appstore.model.AppRuntimeDetails;
import cloudos.appstore.model.ConfigurableAppRuntime;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang.StringUtils;
import org.cobbzilla.util.collection.ArrayUtil;

import java.util.*;

@ToString
public class AppManifest {

    @Getter @Setter private String name;
    @JsonIgnore public String getChefName () { return StringUtils.capitalize(getId()); }

    @JsonIgnore @Getter(lazy=true) private final String id = initId();
    private String initId () { return getName().replace('-', '_').replaceAll("\\W", ""); }

    @Getter @Setter private String version;
    @Getter @Setter private AppStyle style;
    @Getter @Setter private String parent;
    @Getter @Setter private AppUser run_as;
    @Getter @Setter private AppPublisher publisher;

    @Getter @Setter private AppDatabagDef[] databags;

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

    // the chef cookbooks/recipes to install, backup and restore
    @Getter @Setter private AppChefConfig chef = new AppChefConfig();

    @JsonIgnore
    public List<String> getChefInstallRunlist () {
        if (!chef.getInstall().isEmpty()) return chef.getInstall();
        final List<String> defaultRunlist = new ArrayList<>();
        defaultRunlist.add("recipe["+name+"]");
        return defaultRunlist;
    }

    @JsonIgnore
    public AppRuntimeDetails getInstalledAppDetails () {
        return new AppRuntimeDetails(name, getPath(), getHostname());
    }

    @JsonIgnore
    public String getPath() {
        switch (style) {
            case php: return web.hasVhost() ? null : name;
            case rails: return null;
            case java_webapp: return null;
            default: throw new IllegalStateException("getPath: invalid style: "+style);
        }
    }

    @JsonIgnore
    public String getHostname() {
        if (web.getMode() == AppWebMode.proxy_root) return "_root_";
        switch (style) {
            case rails: return name;
            case php: return web.hasVhost() ? name : null;
            case java_webapp: return web.getMode().isSeparateHostname() ? name : null;
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
