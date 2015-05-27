package cloudos.appstore.model.support;

import cloudos.appstore.model.AppVisibility;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain=true)
public class DefineCloudAppRequest {

    @Getter @Setter private String bundleUrl;
    @Getter @Setter private String bundleUrlSha;
    @Getter @Setter private AppVisibility visibility = AppVisibility.publisher;

}
