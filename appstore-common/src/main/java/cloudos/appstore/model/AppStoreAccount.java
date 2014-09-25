package cloudos.appstore.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.jmkgreen.morphia.annotations.Embedded;
import lombok.Getter;
import lombok.Setter;
import org.cobbzilla.wizard.filters.auth.TokenPrincipal;
import org.cobbzilla.wizard.model.HashedPassword;
import org.cobbzilla.wizard.model.IdentifiableBase;
import org.cobbzilla.wizard.validation.HasValue;
import org.hibernate.validator.constraints.Email;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Transient;
import javax.validation.constraints.Size;

import static cloudos.appstore.ValidationConstants.*;

@Entity
public class AppStoreAccount extends IdentifiableBase implements TokenPrincipal {

    @Email(message=ERR_EMAIL_INVALID)
    @HasValue(message=ERR_EMAIL_EMPTY)
    @Size(max=EMAIL_MAXLEN, message=ERR_EMAIL_LENGTH)
    @Column(unique=true, nullable=false, length=EMAIL_MAXLEN)
    @Getter @Setter private String email;

    @Embedded @JsonIgnore
    @Getter @Setter private HashedPassword password = new HashedPassword();

    // For Principal interface. Use other things (firstName/lastName/etc) if/when we want to add the user's real name.
    @JsonIgnore @Transient
    @Override public String getName() { return getEmail(); }

    // no need for this to ever appear in JSON. safer that way.
    // it can only be changed at the DAO layer (as the tests do) or lower (SQL to the DB)
    @JsonIgnore @Getter @Setter private boolean admin = false;

    // Set by ApiAuthFilter
    @JsonIgnore @Transient
    @Getter @Setter private String apiToken;

    @Getter @Setter private Integer publisherTos;
    @JsonIgnore @Transient public boolean isPublisher () { return publisherTos != null && publisherTos > 0; }

    @Getter @Setter private Integer consumerTos;
    @JsonIgnore @Transient public boolean isConsumer () { return consumerTos != null && consumerTos > 0; }

}
