package cloudos.appstore.model;

import cloudos.appstore.model.app.AppManifest;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.cobbzilla.wizard.model.SemanticVersion;

import javax.persistence.Transient;
import java.util.Comparator;

@NoArgsConstructor @Accessors(chain=true)
@EqualsAndHashCode(of={"publisher", "appName", "version"})
public class PublishedApp {

    public static final Comparator<PublishedApp> COMPARATOR_NAME = new Comparator<PublishedApp>() {
        @Override public int compare(PublishedApp p1, PublishedApp p2) {
            return p1.getAppName().compareTo(p2.getAppName());
        }
    };

    @Getter @Setter private String author;
    @Getter @Setter private String publisher;

    @Getter @Setter private String appUuid;
    @Getter @Setter private String appName;
    @Getter @Setter private String version;

    @JsonIgnore
    public SemanticVersion getSemanticVersion() { return new SemanticVersion(version); }

    @Getter @Setter private AppVisibility visibility;
    @Getter @Setter private CloudAppStatus status;

    @Getter @Setter private boolean interactive;

    @Getter @Setter private AppMutableData data;

    @Getter @Setter private String bundleUrl;
    @Getter @Setter private String bundleUrlSha;

    @Getter @Setter private String approvedBy;

    // needed for ember
    @Transient public String getId() { return "published-app-"+appName+"-"+version; }
    public void setId(String id) { /*noop*/ }

    public PublishedApp (AppManifest manifest) {
        appName = manifest.getName();
        version = manifest.getVersion();
        interactive = manifest.isInteractive();
        data = manifest.getAssets();
    }
}
