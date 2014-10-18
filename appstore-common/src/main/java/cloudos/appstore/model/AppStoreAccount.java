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
    @JsonIgnore @Transient
    @Getter private String apiToken;
    public void setApiToken(String apiToken) { this.apiToken = apiToken; }

    @Getter @Setter private Integer publisherTosVersion;
    @JsonIgnore @Transient public boolean isPublisher () { return publisherTosVersion != null && publisherTosVersion > 0; }

    @Getter @Setter private Integer consumerTosVersion;
    @JsonIgnore @Transient public boolean isConsumer () { return consumerTosVersion != null && consumerTosVersion> 0; }

    public AppStoreAccount populate (AppStoreAccountRegistration reg) {
        super.populate(reg);
        setPublisherTosVersion(reg.getPublisherTosVersion());
        setConsumerTosVersion(reg.getConsumerTosVersion());
        return this;
    }
}
