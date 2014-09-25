package cloudos.appstore.model;

import lombok.Getter;
import lombok.Setter;
import org.cobbzilla.wizard.model.IdentifiableBase;
import org.cobbzilla.wizard.validation.HasValue;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.Size;

import static cloudos.appstore.ValidationConstants.*;
import static org.cobbzilla.wizard.model.BasicConstraintConstants.UUID_MAXLEN;

@Entity
public class CloudApp extends IdentifiableBase {

    @HasValue(message=ERR_APP_PUBLISHER_UUID_EMPTY)
    @Size(max=UUID_MAXLEN, message=ERR_APP_PUBLISHER_UUID_LENGTH)
    @Column(nullable=false, updatable=false, length=UUID_MAXLEN)
    @Getter @Setter private String publisher;

    @Column(nullable=false, length=UUID_MAXLEN)
    @Getter @Setter private String author;

    @HasValue(message=ERR_APP_NAME_EMPTY)
    @Size(max=APP_NAME_MAXLEN, message=ERR_APP_NAME_LENGTH)
    @Column(unique=true, nullable=false)
    @Getter @Setter private String name;

    @Column(length=UUID_MAXLEN)
    @Getter @Setter private String activeVersion;
}
