package cloudos.appstore.model.app;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.cobbzilla.util.security.HashType;

import java.util.ArrayList;
import java.util.List;

import static org.cobbzilla.util.daemon.ZillaRuntime.empty;

public class AppUserManagement {

    @JsonIgnore @Getter(lazy=true) private final List<String> templates = initTemplates();
    private List<String> initTemplates () {
        final List<String> templates = new ArrayList<>();
        if (!empty(exists)) templates.add(exists);
        if (!empty(create)) templates.add(create);
        if (!empty(delete)) templates.add(delete);
        if (!empty(change_password)) templates.add(change_password);
        return templates;
    }

    @Getter @Setter private String exists;
    @Getter @Setter private String create;
    @Getter @Setter private String delete;
    @Getter @Setter private String change_password;
    @Getter @Setter private HashType password_hash = HashType.sha256;

    @JsonIgnore public boolean hasUserExists() { return !empty(exists); }
    @JsonIgnore public boolean hasUserCreate() { return !empty(create); }
    @JsonIgnore public boolean hasUserDelete() { return !empty(delete); }
    @JsonIgnore public boolean hasChangePassword() { return !empty(change_password); }

}
