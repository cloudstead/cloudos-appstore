package cloudos.appstore.model.support;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import cloudos.appstore.ValidationConstants;
import org.cobbzilla.wizard.validation.IsUnique;

@IsUnique(id="uuid", unique="email", daoBean="appStoreAccountDAO", message=ValidationConstants.ERR_EMAIL_NOT_UNIQUE)
public class AppStoreAccountRegistration {

    @Getter @Setter private String email;
    @Getter @Setter private String password;

    @JsonIgnore public boolean isPublisher () { return publisherTos != null && publisherTos >= 0; }
    @Getter @Setter private Integer publisherTos;
    @Getter @Setter private String publisherName;

    @JsonIgnore public boolean isConsumer () { return consumerTos != null && consumerTos >= 0; }
    @Getter @Setter private Integer consumerTos;

    public boolean hasOneTos () {
        // must be exactly one. not neither, not both
        return (isConsumer() || isPublisher()) && !(isConsumer() && isPublisher());
    }

}
