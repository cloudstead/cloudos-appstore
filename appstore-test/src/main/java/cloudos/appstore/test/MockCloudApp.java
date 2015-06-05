package cloudos.appstore.test;

import cloudos.appstore.model.CloudApp;
import cloudos.appstore.model.support.AppBundle;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain=true)
public class MockCloudApp extends CloudApp {

    @JsonIgnore @Getter @Setter private String version;
    @JsonIgnore @Getter @Setter private AppBundle bundle;

}
