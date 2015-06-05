package cloudos.appstore.model;

import cloudos.appstore.model.support.AppStoreAccountRegistration;
import cloudos.model.AccountBase;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.cobbzilla.wizard.filters.auth.TokenPrincipal;
import org.cobbzilla.wizard.validation.IsUnique;

import javax.persistence.Entity;
import javax.persistence.Transient;

import static org.cobbzilla.util.daemon.ZillaRuntime.die;

@NoArgsConstructor
@Entity @Accessors(chain=true)
@IsUnique(unique="name", daoBean="appStoreAccountDAO", message="{err.name.notUnique}")
public class AppStoreAccount extends AccountBase implements TokenPrincipal {

    public static final String[] AUTHOR_PUBLIC_FIELDS = {"name", "lastName", "firstName"};

    // Set by ApiAuthFilter
    @JsonIgnore @Transient @Getter private String apiToken;

    public AppStoreAccount(Object o) {
        if (o != null) die("pass null to create blank object");
        setAdmin(null);
        setSuspended(null);
        setTwoFactor(null);
    }

    public void setApiToken(String apiToken) { this.apiToken = apiToken; }

    @Getter @Setter private Integer tosVersion;

    public AppStoreAccount populate (AppStoreAccountRegistration reg) {
        super.populate(reg);
        setTosVersion(reg.getTos() ? 1 : null); // todo: get TOS version from TOS service/dao. for now default to version 1
        return this;
    }
}
