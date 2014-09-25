package cloudos.appstore.model.app;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

public class AppRepository {

    @Getter @Setter private String url;
    @Getter @Setter private String branch = "master";
    @Getter @Setter private String dir;

    @JsonIgnore public String getName () {
        // todo: generify this, currently it is git-specific
        return url.substring(url.lastIndexOf("/"), url.lastIndexOf(".git"));
    }

}
