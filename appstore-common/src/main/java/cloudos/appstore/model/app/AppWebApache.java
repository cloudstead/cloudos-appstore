package cloudos.appstore.model.app;

import lombok.Getter;
import lombok.Setter;

import static org.cobbzilla.util.daemon.ZillaRuntime.empty;

public class AppWebApache {

    @Getter @Setter private String[] mods;
    @Getter @Setter private AppWebApachePhpSet[] php_set;

    @Getter @Setter private String[] dir;
    public boolean hasDir() { return !empty(dir); }

    @Getter @Setter private String[] location;
    public boolean hasLocation() { return !empty(location); }

    @Getter @Setter private String[] htaccess;
    public boolean hasHtaccess() { return !empty(htaccess); }

    @Getter @Setter private String[] mixins;
    public boolean hasMixins () { return !empty(mixins); }

    public static class AppWebApachePhpSet {
        @Getter @Setter private String name;
        @Getter @Setter private String value;
    }
}
