package cloudos.appstore.model.app;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

public class AppService {

    @Getter @Setter private String name;
    @Getter @Setter private String pattern;
    @Getter @Setter private AppServiceProvider provider;
    @Getter @Setter private String[] ports;

    @JsonIgnore public String getChefProvider () { return provider == null ? null : "Chef::Provider::Service::"+ StringUtils.capitalize(provider.name()); }

}
