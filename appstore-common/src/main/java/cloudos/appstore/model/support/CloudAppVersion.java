package cloudos.appstore.model.support;

import lombok.*;
import lombok.experimental.Accessors;
import org.cobbzilla.wizard.model.SemanticVersion;

import static org.cobbzilla.util.daemon.ZillaRuntime.die;
import static org.cobbzilla.util.string.StringUtil.empty;

@NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
@Accessors(chain=true)
public class CloudAppVersion {

    @Getter @Setter private String app;
    @Getter @Setter private String version;

    public CloudAppVersion(String nameAndVersion) {
        if (empty(nameAndVersion) || !nameAndVersion.contains("/")) die("Invalid nameAndVersion: "+nameAndVersion);
        final int slashPos = nameAndVersion.indexOf('/');
        if (slashPos == 0 || slashPos == nameAndVersion.length()-1) die("Invalid nameAndVersion: "+nameAndVersion);
        app = nameAndVersion.substring(0, slashPos);
        version = nameAndVersion.substring(slashPos+1);
    }

    public CloudAppVersion(String name, SemanticVersion version) {
        this.app = name;
        this.version = version.toString();
    }

    public String toString () { return app + "/" + version; }

}
