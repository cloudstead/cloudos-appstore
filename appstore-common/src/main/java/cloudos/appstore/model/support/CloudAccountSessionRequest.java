package cloudos.appstore.model.support;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.RandomStringUtils;

@NoArgsConstructor
public class CloudAccountSessionRequest {

    @Getter @Setter private String ucid;
    @Getter @Setter private String data;

    public CloudAccountSessionRequest (String ucid) {
        this.ucid = ucid;
        this.data = RandomStringUtils.randomAlphanumeric(32);
    }

}
