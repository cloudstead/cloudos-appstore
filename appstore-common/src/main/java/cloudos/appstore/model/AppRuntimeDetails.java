package cloudos.appstore.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.cobbzilla.util.string.StringUtil;

@NoArgsConstructor @AllArgsConstructor
public class AppRuntimeDetails {

    @Getter @Setter private String name;

    @Getter @Setter private String path;
    public boolean hasPath () { return !StringUtil.empty(path); }

    @Getter @Setter private String hostname;
    public boolean hasHostname () { return !StringUtil.empty(hostname); }

}
