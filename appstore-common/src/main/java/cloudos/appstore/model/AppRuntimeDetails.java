package cloudos.appstore.model;

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

    public String getPath(String uriBase) {

        if (!isInteractive()) throw new IllegalArgumentException("app '"+name+"' is not interactive");

        StringBuilder sb = new StringBuilder();
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
        return sb.toString();
    }

}
