package cloudos.appstore.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang3.RandomStringUtils;
import org.cobbzilla.util.collection.FieldTransfomer;
import org.cobbzilla.wizard.model.IdentifiableBase;
import org.cobbzilla.wizard.validation.HasValue;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import static cloudos.appstore.ValidationConstants.*;

@Entity @Accessors(chain=true)
@Table(uniqueConstraints=@UniqueConstraint(columnNames = {"publisher", "account"}))
public class AppStorePublisherMember extends IdentifiableBase {

    public static final long INVITATION_VALID_DURATION = TimeUnit.DAYS.toMillis(30);

    public static final FieldTransfomer TO_PUBLISHER = new FieldTransfomer("publisher");
    public static final Transformer TO_ACCOUNT = new FieldTransfomer("account");

    public static Collection<String> toPublisher (Collection<AppStorePublisherMember> c) { return CollectionUtils.collect(c, TO_PUBLISHER); }
    public static Collection<String> toAccount (Collection<AppStorePublisherMember> c) { return CollectionUtils.collect(c, TO_ACCOUNT); }

    @HasValue(message=ERR_MEMBER_PUBLISHER_UUID_EMPTY)
    @Size(max=UUID_MAXLEN, message=ERR_MEMBER_PUBLISHER_UUID_LENGTH)
    @Column(nullable=false, length=UUID_MAXLEN)
    @Getter @Setter private String publisher;

    @HasValue(message=ERR_MEMBER_ACCOUNT_UUID_EMPTY)
    @Size(max=UUID_MAXLEN, message=ERR_MEMBER_ACCOUNT_UUID_LENGTH)
    @Column(nullable=false, length=UUID_MAXLEN)
    @Getter @Setter private String account;

    @Getter @Setter private boolean active = false;
    public boolean inactive() { return !isActive(); }

    @Size(max=UUID_MAXLEN, message=ERR_MEMBER_ACCOUNT_ACTIVATION_LENGTH)
    @Column(unique=true, length=UUID_MAXLEN)
    @Getter @Setter @JsonIgnore private String activation;

    @Getter @Setter private Long activationExpiration = null;

    public AppStorePublisherMember initNew() {
        initUuid();
        newActivation();
        return this;
    }

    public void newActivation() {
        activation = RandomStringUtils.randomAlphanumeric(20);
        activationExpiration = System.currentTimeMillis() + INVITATION_VALID_DURATION;
    }

}
