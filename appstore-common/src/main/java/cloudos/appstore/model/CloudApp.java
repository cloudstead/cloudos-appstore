package cloudos.appstore.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.cobbzilla.wizard.model.UniquelyNamedEntity;
import org.cobbzilla.wizard.validation.HasValue;
import org.cobbzilla.wizard.validation.IsUnique;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.Size;

import static cloudos.appstore.ValidationConstants.*;

@Entity @Accessors(chain=true)
@IsUnique(unique="name", daoBean="cloudAppDAO", message="{err.name.notUnique}")
public class CloudApp extends UniquelyNamedEntity {

    @HasValue(message=ERR_APP_PUBLISHER_UUID_EMPTY)
    @Size(max=UUID_MAXLEN, message=ERR_APP_PUBLISHER_UUID_LENGTH)
    @Column(nullable=false, updatable=false, length=UUID_MAXLEN)
    @Getter @Setter private String publisher;

    @Column(nullable=false, length=UUID_MAXLEN)
    @Getter @Setter private String author;

    @Column(length=UUID_MAXLEN)
    @Getter @Setter private String activeVersion;
}
