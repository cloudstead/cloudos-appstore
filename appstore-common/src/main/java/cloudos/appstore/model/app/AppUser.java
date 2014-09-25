package cloudos.appstore.model.app;

import lombok.Getter;
import lombok.Setter;

import static org.cobbzilla.util.string.StringUtil.empty;

public class AppUser {

    @Setter private String home;
    public String getHome () { return empty(home) ? "/home/"+getUser(): home; }

    @Setter private String user;
    public String getUser () { return empty(user) ? label : user; }

    @Setter private String label;
    public String getLabel () { return empty(label) ? user : label; }

    @Setter private String group;
    public String getGroup () { return empty(group) ? user : group; }

    @Getter @Setter private boolean can_login = false;
    @Getter @Setter private boolean system = true;
    @Getter @Setter private boolean kerberos = false;

}
