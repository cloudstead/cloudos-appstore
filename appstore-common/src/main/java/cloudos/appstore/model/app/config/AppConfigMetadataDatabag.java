package cloudos.appstore.model.app.config;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

public class AppConfigMetadataDatabag {

    @Getter @Setter private boolean advanced;
    @Getter @Setter private Map<String, AppConfigMetadataDatabagField> fields;

    public AppConfigMetadataDatabagField get(String item) { return fields == null ? null : fields.get(item); }

}
