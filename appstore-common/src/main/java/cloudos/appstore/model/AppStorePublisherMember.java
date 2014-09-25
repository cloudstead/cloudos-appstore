package cloudos.appstore.model;

import lombok.Getter;
import lombok.Setter;
import org.cobbzilla.wizard.model.IdentifiableBase;
import org.cobbzilla.wizard.validation.HasValue;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Size;

import static cloudos.appstore.ValidationConstants.*;
import static org.cobbzilla.wizard.model.BasicConstraintConstants.UUID_MAXLEN;

@Entity
@Table(uniqueConstraints=@UniqueConstraint(columnNames = {"publisher", "account"}))
public class AppStorePublisherMember extends IdentifiableBase {

    @HasValue(message=ERR_MEMBER_PUBLISHER_UUID_EMPTY)
    @Size(max=UUID_MAXLEN, message=ERR_MEMBER_PUBLISHER_UUID_LENGTH)
    @Column(unique=true, nullable=false, length=UUID_MAXLEN)
    @Getter @Setter private String publisher;

    @HasValue(message=ERR_MEMBER_ACCOUNT_UUID_EMPTY)
    @Size(max=UUID_MAXLEN, message=ERR_MEMBER_ACCOUNT_UUID_LENGTH)
    @Column(unique=true, nullable=false, length=UUID_MAXLEN)
    @Getter @Setter private String account;

}
