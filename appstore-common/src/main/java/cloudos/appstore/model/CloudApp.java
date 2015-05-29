package cloudos.appstore.model;

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

    @Transient
    @Getter @Setter private List<CloudAppVersion> versions;

}
