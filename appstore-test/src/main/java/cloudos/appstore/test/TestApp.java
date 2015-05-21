package cloudos.appstore.test;

import cloudos.appstore.model.app.AppManifest;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.File;

@Accessors(chain=true)
public class TestApp {

    @Getter @Setter private AppManifest manifest;
    @Getter @Setter private String bundleUrl;
    @Getter @Setter private String bundleUrlSha;
    @Getter @Setter private File tarball;
    @Getter @Setter private File iconFile;

    @JsonIgnore public String getNameAndVersion() {
        return manifest.getName()+"/"+manifest.getVersion();
    }
}
