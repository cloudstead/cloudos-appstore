package cloudos.appstore.model.app;

import lombok.Getter;
import lombok.Setter;

public class AppPublisher {

    @Getter @Setter private String maintainer;
    @Getter @Setter private String maintainer_email;
    @Getter @Setter private String license;
    @Getter @Setter private String code_copyright;
    @Getter @Setter private String packaging_copyright;

}
