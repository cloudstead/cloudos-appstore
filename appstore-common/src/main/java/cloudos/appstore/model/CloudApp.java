package cloudos.appstore.model;

import cloudos.appstore.model.app.AppLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.cobbzilla.wizard.model.IdentifiableBase;
import org.cobbzilla.wizard.validation.HasValue;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.List;

import static cloudos.appstore.ValidationConstants.ERR_APP_PUBLISHER_UUID_EMPTY;
import static cloudos.appstore.ValidationConstants.ERR_APP_PUBLISHER_UUID_LENGTH;
import static org.cobbzilla.util.daemon.ZillaRuntime.empty;
import static org.cobbzilla.util.reflect.ReflectionUtil.copy;

@Entity @Accessors(chain=true)
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"publisher", "name"}))
public class CloudApp extends IdentifiableBase {

    @HasValue(message=ERR_APP_PUBLISHER_UUID_EMPTY)
    @Size(max=UUID_MAXLEN, message=ERR_APP_PUBLISHER_UUID_LENGTH)
    @Column(nullable=false, updatable=false, length=UUID_MAXLEN)
    @Getter @Setter private String publisher;

    @HasValue(message="err.app.name.empty")
    @Column(length=100, nullable=false, updatable=false)
    @Size(min=2, max=100, message="err.app.name.length")
    protected String name;
    public boolean hasName () { return !empty(name); }

    public String getName () { return hasName() ? name.toLowerCase() : null; }
    public CloudApp setName (String name) { this.name = (name == null) ? null : name.toLowerCase(); return this; }

    @Size(max=UUID_MAXLEN)
    @Getter @Setter private String author;

    @Column(length=20, nullable=false)
    @Enumerated(EnumType.STRING)
    @Getter @Setter private AppVisibility visibility = AppVisibility.publisher;

    @HasValue(message="err.app.level.empty")
    @Column(length=20, nullable=false)
    @Enumerated(EnumType.STRING)
    @Getter @Setter private AppLevel level;

    @Transient
    @Getter @Setter private List<CloudAppVersion> versions;

    @Transient
    @Getter private AppStorePublisher publishedBy;
    public void setPublishedBy(AppStorePublisher pub) {
        publishedBy = new AppStorePublisher();
        copy(publishedBy, pub, AppStorePublisher.PUBLIC_FIELDS);
    }

    @Transient
    @Getter private AppStoreAccount authoredBy;
    public void setAuthoredBy(AppStoreAccount author) {
        authoredBy = new AppStoreAccount(null);
        copy(authoredBy, author, AppStoreAccount.AUTHOR_PUBLIC_FIELDS);
    }
}
