package cloudos.appstore.model.support;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import static org.cobbzilla.util.string.StringUtil.empty;

@Accessors(chain=true)
public class DefineCloudAppRequest {

    @Getter @Setter private String publisher;
    public boolean hasPublisher () { return !empty(publisher); }

    @Getter @Setter private String bundleUrl;
    @Getter @Setter private String bundleUrlSha;

}
