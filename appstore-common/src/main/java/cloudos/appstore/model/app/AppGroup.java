package cloudos.appstore.model.app;

import lombok.Getter;
import lombok.Setter;

import static org.cobbzilla.util.daemon.ZillaRuntime.empty;

public class AppGroup {

    @Setter private String group;
    public String getGroup () { return empty(group) ? label : group; }

    @Setter private String label;
    public String getLabel () { return empty(label) ? group : label; }

    @Getter @Setter private String[] members;

}
