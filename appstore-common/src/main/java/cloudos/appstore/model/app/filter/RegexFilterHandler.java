package cloudos.appstore.model.app.filter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexFilterHandler extends AppFilterHandlerBase {

    @Getter @Setter private String regex;
    @Getter @Setter private int replace_group = 1;
    @Getter @Setter private String value;

    public String renderValue(Map<String, Object> scope) { return render(getValue(), scope); }

    @Getter(lazy=true) private final Pattern pattern = initPattern();
    public Pattern initPattern() { return Pattern.compile(getPatternString(), getRegexFlags()); }

    @JsonIgnore protected String getPatternString() {
        String regexString = getRegex().replace(".", "\\.");
        if (!regexString.contains("(") && !regexString.contains(")")) {
            regexString = "("+regexString+")";
        }
        return regexString;
    }

    public String apply(String document, Map<String, Object> scope) {
        final Matcher matcher = Pattern.compile(getPatternString(), getRegexFlags()).matcher(document);
        while (matcher.find()) {
            document = replaceGroup(document, matcher, getReplace_group(), renderValue(scope));
        }
        return document;
    }
}
