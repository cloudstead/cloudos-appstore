package cloudos.appstore.model.app;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import static org.cobbzilla.util.daemon.ZillaRuntime.empty;

public class AppRepository {

    @Getter @Setter private String url;
    @Getter @Setter private String dir;

    @Getter @Setter private String tag;

    @Setter private String branch = "master";

    public String getBranch () {
        if (!empty(tag)) return "tags/"+tag;
        return branch;
    }

    @JsonIgnore public String getName () {
        // todo: generify this, currently it is git-specific
        return url.substring(url.lastIndexOf("/"), url.lastIndexOf(".git"));
    }

}
