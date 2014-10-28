package cloudos.appstore.model;

import cloudos.appstore.model.support.AppStoreAccountRegistration;
import cloudos.model.AccountBase;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.cobbzilla.wizard.filters.auth.TokenPrincipal;

import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity @Accessors(chain=true)
public class AppStoreAccount extends AccountBase implements TokenPrincipal {

    // Set by ApiAuthFilter
    @JsonIgnore @Transient @Getter private String apiToken;
    public void setApiToken(String apiToken) { this.apiToken = apiToken; }

    @Getter @Setter private Integer tosVersion;

    public AppStoreAccount populate (AppStoreAccountRegistration reg) {
        super.populate(reg);
        setTosVersion(reg.getTos() ? 1 : null); // todo: get TOS version from TOS service/dao. for now default to version 1
        return this;
    }
}
