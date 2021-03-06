package cloudos.appstore.model.support;

import cloudos.appstore.model.AppStoreAccount;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import cloudos.appstore.ValidationConstants;
import lombok.experimental.Accessors;
import org.cobbzilla.util.daemon.ZillaRuntime;
import org.cobbzilla.wizard.validation.HasValue;
import org.cobbzilla.wizard.validation.IsUnique;

import static org.cobbzilla.util.daemon.ZillaRuntime.empty;

@IsUnique(id="uuid", unique="email", daoBean="appStoreAccountDAO", message=ValidationConstants.ERR_EMAIL_NOT_UNIQUE)
@Accessors(chain=true)
public class AppStoreAccountRegistration extends AppStoreAccount {

    @HasValue(message="err.password.empty")
    @Getter @Setter private String password;
    @JsonIgnore public boolean hasPassword() { return !empty(password); }

    @HasValue(message="err.tos.empty")
    @Getter @Setter private Boolean tos;
    public boolean hasTos () { return tos != null && tos; }

}
