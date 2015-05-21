package cloudos.appstore.test;

import cloudos.appstore.model.CloudApp;
import cloudos.appstore.model.support.AppBundle;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain=true)
public class MockCloudApp extends CloudApp {

    @Getter @Setter private String version;
    @Getter @Setter private AppBundle bundle;

}
