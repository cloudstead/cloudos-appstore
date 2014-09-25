package cloudos.appstore.model;

import lombok.Getter;
import lombok.Setter;
import org.cobbzilla.wizard.model.BasicConstraintConstants;
import org.cobbzilla.wizard.model.IdentifiableBase;
import org.cobbzilla.wizard.validation.HasValue;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.Size;

import static cloudos.appstore.ValidationConstants.*;

@Entity
public class AppStorePublisher extends IdentifiableBase {

    @HasValue(message=ERR_PUBLISHER_NAME_EMPTY)
    @Size(max=PUBLISHER_NAME_MAXLEN, message=ERR_PUBLISHER_NAME_LENGTH)
    @Column(unique=true, nullable=false, length=PUBLISHER_NAME_MAXLEN)
    @Getter @Setter private String name;

    @HasValue(message=ERR_OWNER_UUID_EMPTY)
    @Size(max=BasicConstraintConstants.UUID_MAXLEN, message=ERR_OWNER_UUID_LENGTH)
    @Column(unique=true, nullable=false)
    @Getter @Setter private String owner;

    public void beforeCreate() {
        if (getUuid() == null) throw new IllegalStateException("uuid not initialized");
    }
}
