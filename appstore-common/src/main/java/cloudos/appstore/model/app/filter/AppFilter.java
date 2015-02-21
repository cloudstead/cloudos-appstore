package cloudos.appstore.model.app.filter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class AppFilter {

    @Getter @Setter AppFilterType type;
    @Getter @Setter Map<String, String> config;

    @JsonIgnore @Getter(lazy=true) private final AppFilterHandler handler = initHandler();
    public AppFilterHandler initHandler () {
        return type.newHandler().configure(config);
    }
}
