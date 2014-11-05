package cloudos.appstore.model.app;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.cobbzilla.util.security.HashType;

public class AppUserManagement {

    @JsonIgnore public String[] getTemplates () { return new String[] {exists, create, delete, change_password};  }

    @Getter @Setter private String exists;
    @Getter @Setter private String create;
    @Getter @Setter private String delete;
    @Getter @Setter private String change_password;
    @Getter @Setter private HashType password_hash = HashType.sha256;

}
