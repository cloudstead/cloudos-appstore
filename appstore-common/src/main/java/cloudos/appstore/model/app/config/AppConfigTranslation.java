package cloudos.appstore.model.app.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Accessors(chain=true) @ToString
public class AppConfigTranslation {

    @Getter @Setter private String label;
    @Getter @Setter private String info;

}
