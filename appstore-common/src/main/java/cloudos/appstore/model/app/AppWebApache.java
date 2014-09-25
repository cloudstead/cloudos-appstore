package cloudos.appstore.model.app;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

public class AppWebApache {

    @Getter @Setter private String[] mods;
    @Getter @Setter private AppWebApachePhpSet[] php_set;

    @Getter @Setter private String vhost;
    public boolean hasVhost () { return !StringUtils.isEmpty(vhost); }

    @Getter @Setter private String[] dir;
    public boolean hasDir() { return dir != null && dir.length > 0; }

    @Getter @Setter private String[] location;
    public boolean hasLocation() { return location != null && location.length > 0; }

    @Getter @Setter private String[] htaccess;
    public boolean hasHtaccess() { return htaccess != null && htaccess.length > 0; }

    public static class AppWebApachePhpSet {
        @Getter @Setter private String name;
        @Getter @Setter private String value;
    }
}
