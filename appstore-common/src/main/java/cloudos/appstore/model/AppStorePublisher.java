package cloudos.appstore.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.cobbzilla.wizard.model.BasicConstraintConstants;
import org.cobbzilla.wizard.model.UniquelyNamedEntity;
import org.cobbzilla.wizard.validation.HasValue;
import org.cobbzilla.wizard.validation.IsUnique;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.Size;

import static cloudos.appstore.ValidationConstants.*;
import static org.cobbzilla.util.daemon.ZillaRuntime.die;

@Entity @Accessors(chain=true)
@IsUnique(unique="name", daoBean="appStorePublisherDAO", message="{err.name.notUnique}")
public class AppStorePublisher extends UniquelyNamedEntity {

    @HasValue(message=ERR_OWNER_UUID_EMPTY)
    @Size(max=BasicConstraintConstants.UUID_MAXLEN, message=ERR_OWNER_UUID_LENGTH)
    @Column(unique=true, nullable=false)
    @Getter @Setter private String owner;

    public void beforeCreate() {
        if (getUuid() == null) die("uuid not initialized");
    }
}
