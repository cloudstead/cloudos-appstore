package cloudos.appstore.model.app.filter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.util.regex.Pattern;

public class AppFilterConfig {

    public static final String REGEX_PREFIX = "regex:";

    @Getter @Setter private String uri;
    @Getter @Setter private AppFilter[] filters;

    public boolean isUriMatch(String requestUri) {
        return isPattern() ? getPattern().matcher(requestUri).matches() : uri.equals(requestUri);
    }

    @JsonIgnore @Getter(lazy=true) private final Pattern pattern = initPattern();
    public Pattern initPattern() {
        return uri.startsWith(REGEX_PREFIX) ? Pattern.compile(uri.substring(REGEX_PREFIX.length())) : null;
    }

    @JsonIgnore public boolean isPattern () { return getPattern() != null; }
    @JsonIgnore public boolean hasFilters() { return filters != null && filters.length > 0; }
}
