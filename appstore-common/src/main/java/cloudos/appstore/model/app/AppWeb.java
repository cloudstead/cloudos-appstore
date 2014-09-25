package cloudos.appstore.model.app;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

public class AppWeb {

    @Getter @Setter private AppWebType type;
    @JsonIgnore public String getChefType () { return StringUtils.capitalize(type.name()); }

    @Getter @Setter private String doc_root;
    @Getter @Setter private AppWebApache apache;
    @Getter @Setter private AppWebMode mode;
    @Getter @Setter private String mount;
    @Getter @Setter private String ssl_cert_name;

    @JsonIgnore public boolean hasVhost() { return apache != null && apache.hasVhost(); }

}
