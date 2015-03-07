package cloudos.appstore.model.app.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

public class AppConfigMetadataDatabagField {

    @Getter @Setter private AppConfigMetadataDatabagFieldType type = AppConfigMetadataDatabagFieldType.field;
    @Getter @Setter private String group;
    @Getter @Setter private boolean required = true;
    @Getter @Setter private Integer min = null;
    @Getter @Setter private Integer max = null;
    @Getter @Setter private String[] choices;

    @JsonIgnore public boolean hasMin () { return min != null; }
    @JsonIgnore public boolean hasMax () { return max != null; }

}
