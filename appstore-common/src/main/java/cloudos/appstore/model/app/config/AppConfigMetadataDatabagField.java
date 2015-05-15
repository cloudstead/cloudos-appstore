package cloudos.appstore.model.app.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import static org.cobbzilla.util.string.StringUtil.empty;

@ToString
public class AppConfigMetadataDatabagField {

    @Getter @Setter private AppConfigMetadataDatabagFieldType type = AppConfigMetadataDatabagFieldType.field;
    @Getter @Setter private String group;
    @Getter @Setter private boolean required = true;
    @Getter @Setter private Integer min = null;
    @Getter @Setter private Integer max = null;
    @Getter @Setter private String[] choices;
    @Getter @Setter private String login; // if type==password, this is the name of the field for the login

    @JsonIgnore public boolean hasMin () { return min != null; }
    @JsonIgnore public boolean hasMax () { return max != null; }
    @JsonIgnore public boolean hasLogin() { return !empty(login); }

    @JsonIgnore public boolean getIs_password () { return type != null && type.getIs_password(); }
    @JsonIgnore public boolean getIs_locale () { return type != null && type.getIs_locale(); }

}
