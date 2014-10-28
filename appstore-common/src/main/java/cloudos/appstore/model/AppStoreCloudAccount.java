package cloudos.appstore.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.cobbzilla.wizard.model.IdentifiableBase;
import org.cobbzilla.wizard.validation.HasValue;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.Size;

import static org.cobbzilla.wizard.model.BasicConstraintConstants.UUID_MAXLEN;

@Entity
@Accessors(chain=true)
public class AppStoreCloudAccount extends IdentifiableBase {

    @HasValue(message="err.ucid.empty")
    @Size(max=UUID_MAXLEN, message="err.ucid.length")
    @Column(length=UUID_MAXLEN, nullable=false, updatable=false)
    @Getter @Setter private String ucid;

    @HasValue(message="err.uri.empty")
    @Size(max=255, message="err.uri.length")
    @Column(length=255, nullable=false, updatable=false)
    @Getter @Setter private String uri;

}
