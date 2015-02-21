package cloudos.appstore.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static org.cobbzilla.util.string.StringUtil.empty;

@NoArgsConstructor @AllArgsConstructor
public class AppRuntimeDetails {

    @Getter @Setter private String name;

    @Getter @Setter private String path;
    public boolean hasPath() { return !empty(path); }

    @Getter @Setter private String hostname;
    public boolean hasHostname() { return !empty(hostname); }

    @Getter @Setter private boolean interactive;

    @Getter @Setter private AppMutableData assets;

    @JsonIgnore public AppRuntimeDetails getDetails(String uriBase) {
        return new ConcreteAppRuntimeDetails(this, uriBase);
    }

    public String getPath(String uriBase) {

        if (!isInteractive()) throw new IllegalArgumentException("app '"+name+"' is not interactive");

        final StringBuilder sb = new StringBuilder();
        if (hasHostname()) {
            sb.append(uriBase.replace("://", "://" + getHostname() + "-"));
        } else {
            sb.append(uriBase);
        }
        if (hasPath()) {
            // ensure a slash exists between the hostname and the path
            if (!uriBase.endsWith("/") && !getPath().startsWith("/")) sb.append("/");
            sb.append(getPath());
        }


        final String path = sb.toString();
        return path.endsWith("/") ? path : path+"/";
    }

    public void mergeParent(AppRuntimeDetails parent) {
        if (parent.hasPath()) path = parent.getPath() + (empty(path) ? "" : path);
        setHostname(parent.getHostname());
        interactive = parent.isInteractive();
    }
}
