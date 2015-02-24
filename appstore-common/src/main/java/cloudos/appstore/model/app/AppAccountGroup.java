package cloudos.appstore.model.app;

import lombok.Getter;
import lombok.Setter;

public class AppAccountGroup {

    @Getter @Setter private String name;
    @Getter @Setter private String description;
    @Getter @Setter private String quota;
    @Getter @Setter private String[] members;
    @Getter @Setter private String link_group;

}
