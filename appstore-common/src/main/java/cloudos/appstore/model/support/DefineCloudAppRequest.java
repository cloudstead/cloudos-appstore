package cloudos.appstore.model.support;

import cloudos.appstore.model.AppVisibility;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import static org.cobbzilla.util.daemon.ZillaRuntime.empty;

@Accessors(chain=true)
public class DefineCloudAppRequest {

    @Getter @Setter private String bundleUrl;
    @Getter @Setter private String bundleUrlSha;
    @Getter @Setter private AppVisibility visibility;
    public boolean hasVisibility () { return !empty(visibility); }

}
