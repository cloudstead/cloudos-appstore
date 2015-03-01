package cloudos.appstore.model.app.filter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.regex.Pattern;

import static org.cobbzilla.util.mustache.MustacheUtil.renderBoolean;

public class AppFilterConfig {

    public static final String REGEX_PREFIX = "regex:";

    @Getter @Setter private String uri;
    @Getter @Setter private String only_if = "true";
    @Getter @Setter private String not_if = "false";
    @Getter @Setter private AppFilter[] filters;

    public boolean isUriMatch(String requestUri) {
        return isPattern() ? getPattern().matcher(requestUri).matches() : uri.equals(requestUri);
    }

    @JsonIgnore @Getter(lazy=true) private final Pattern pattern = initPattern();
    public Pattern initPattern() {
        return uri.startsWith(REGEX_PREFIX) ? Pattern.compile(uri.substring(REGEX_PREFIX.length())) : null;
    }

    @JsonIgnore public boolean isPattern () { return getPattern() != null; }
    @JsonIgnore public boolean hasFilters(Map<String, Object> scope) {
        if (filters == null || filters.length == 0) return false;
        return renderBoolean(only_if, scope) && !renderBoolean(not_if, scope);
    }
}
