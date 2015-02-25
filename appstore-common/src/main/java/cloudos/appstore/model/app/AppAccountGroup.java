package cloudos.appstore.model.app;

import lombok.Getter;
import lombok.Setter;

public class AppAccountGroup {

    @Getter @Setter private String description;
    @Getter @Setter private String quota;

    @Getter @Setter private String members; // comma-separated list of members (accounts and/or groups)
    @Getter @Setter private String mirror;  // a single group to mirror members from

}
