package cloudos.appstore.model.app;

import cloudos.appstore.model.app.filter.AppFilterConfig;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import static org.cobbzilla.util.daemon.ZillaRuntime.empty;

public class AppWeb {

    @Getter @Setter private AppWebType type;
    @JsonIgnore public String getChefType () { return StringUtils.capitalize(type.name()); }

    @Getter @Setter private String doc_root;
    @Getter @Setter private String hostname;
    @Getter @Setter private AppWebApache apache;
    @Getter @Setter private AppWebMode mode;

    @Getter @Setter private String mount;
    @JsonIgnore public boolean hasMount() { return !empty(mount); }

    @Getter @Setter private String cookie;
    @JsonIgnore public boolean hasCookie() { return !empty(cookie); }

    @Getter @Setter private String local_mount;
    @Getter @Setter private String ssl_cert_name;

    @JsonIgnore public boolean getIs_separate_hostname() { return mode.isSeparateHostname(); }

    @Getter @Setter private AppFilterConfig[] filters;
    public boolean hasFilters () { return filters != null && filters.length > 0; }

    public AppFilterConfig getFilterConfig(String uri) {
        if (!hasFilters()) return null;
        for (AppFilterConfig f : filters) {
            if (f.isUriMatch(uri)) return f;
        }
        return null;
    }

}
