package cloudos.appstore.model.app;

import lombok.Getter;
import lombok.Setter;

public class AppPermission {

    @Getter @Setter private String chown;
    @Getter @Setter private String chgrp;
    @Getter @Setter private String perms;
    @Getter @Setter private boolean recursive = false;

}
