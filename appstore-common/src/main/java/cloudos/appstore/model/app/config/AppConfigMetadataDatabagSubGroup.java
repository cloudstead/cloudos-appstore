package cloudos.appstore.model.app.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
public class AppConfigMetadataDatabagSubGroup {

    @Getter @Setter private String label;
    @Getter @Setter private String[] required;

}
