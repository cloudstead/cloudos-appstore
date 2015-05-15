package cloudos.appstore.model.app.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

import static org.cobbzilla.util.string.StringUtil.empty;

public class AppConfigMetadataDatabag {

    @Getter @Setter private boolean advanced;
    @Getter @Setter private Map<String, AppConfigMetadataDatabagField> fields;

    public AppConfigMetadataDatabagField get(String item) { return fields == null ? null : fields.get(item); }

    // called by bundler when deciding whether to auto-generate any passwords for this databag
    @JsonIgnore public boolean getHas_passwords() {
        if (empty(fields)) return false;
        for (AppConfigMetadataDatabagField f : fields.values()) {
            if (f.getIs_password()) return true;
        }
        return false;
    }

    @JsonIgnore public boolean getHas_locales() {
        if (empty(fields)) return false;
        for (AppConfigMetadataDatabagField f : fields.values()) {
            if (f.getType() == AppConfigMetadataDatabagFieldType.locale) return true;
        }
        return false;
    }
}
