package cloudos.appstore.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.experimental.Accessors;
import org.cobbzilla.wizard.model.IdentifiableBase;
import org.cobbzilla.wizard.model.SemanticVersion;
import org.cobbzilla.wizard.validation.HasValue;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.Comparator;

import static org.cobbzilla.util.daemon.ZillaRuntime.die;
import static org.cobbzilla.util.daemon.ZillaRuntime.empty;
import static org.cobbzilla.wizard.model.UniquelyNamedEntity.NAME_MAXLEN;

@Entity
@NoArgsConstructor @AllArgsConstructor
@Accessors(chain=true)
@EqualsAndHashCode(of={"app", "version"}, callSuper=false)
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"app", "version"}))
public class CloudAppVersion extends IdentifiableBase {

    public static Comparator<CloudAppVersion> LATEST_VERSION_FIRST = new Comparator<CloudAppVersion>() {
        @Override public int compare(CloudAppVersion v1, CloudAppVersion v2) {
            return v1.getSemanticVersion().compareTo(v2.getSemanticVersion());
        }
    };

    @Size(max=NAME_MAXLEN)
    @Column(length=NAME_MAXLEN, nullable=false, updatable=false)
    @Getter @Setter private String app;

    @HasValue(message="err.app.version.empty")
    @Size(max=100)
    @Column(length=100, nullable=false, updatable=false)
    @Getter @Setter private String version;

    @Transient @JsonIgnore
    public SemanticVersion getSemanticVersion () { return new SemanticVersion(version); }

    @Size(max=80)
    @Column(length=80, nullable=false, updatable=false)
    @Getter @Setter private String bundleSha;

    @Column(length=20, nullable=false)
    @Enumerated(EnumType.STRING)
    @Getter @Setter private CloudAppStatus status = CloudAppStatus.created;

    @Size(max=UUID_MAXLEN)
    @Getter @Setter private String approvedBy; // uuid of account that moved this to "published" statue

    @JsonIgnore public boolean isPublished() { return status.isPublished(); }

    public CloudAppVersion(String nameAndVersion) {
        if (empty(nameAndVersion) || !nameAndVersion.contains("/")) die("Invalid nameAndVersion: "+nameAndVersion);
        final int slashPos = nameAndVersion.indexOf('/');
        if (slashPos == 0 || slashPos == nameAndVersion.length()-1) die("Invalid nameAndVersion: "+nameAndVersion);
        app = nameAndVersion.substring(0, slashPos);
        version = nameAndVersion.substring(slashPos+1);
    }

    public CloudAppVersion(String name, String version) {
        this.app = name;
        this.version = version;
    }

    public CloudAppVersion(String name, SemanticVersion version) {
        this(name, version.toString());
    }

    public String toString () { return app + "/" + version; }

}
