package cloudos.appstore.model.app.filter;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.cobbzilla.util.daemon.ZillaRuntime.die;

@Slf4j
public class ElementFilterHandler extends AppFilterHandlerBase {

    @Getter @Setter private String element;
    @Getter @Setter private String match_attr_name;
    @Getter @Setter private String match_attr_value;
    @Getter @Setter private String replace_attr;
    @Getter @Setter private String value;

    @Override public String apply(String document, Map<String, Object> scope) {

        final Pattern pattern = elementPattern(getElement(), getMatch_attr_name(), getMatch_attr_value());
        final String renderedValue = render(getValue(), scope);
        final String val = getReplacement(renderedValue);
        int start = 0;
        while (true) {
            final Matcher matcher = pattern.matcher(document);
            if (!matcher.find(start)) break;

            String element = matcher.group(1);
            if (element.contains(val)) break;

            boolean anyFound = false;
            while (true) {
                final Matcher attrMatcher = attributePattern(getReplace_attr()).matcher(element);
                if (!attrMatcher.find() || attrMatcher.group(1).contains(val)) break;
                element = replaceGroup(element, attrMatcher, 1, val);
                anyFound = true;
            }

            // If none were found, add one at the end
            if (!anyFound) {
                if (!element.endsWith(">")) die("No terminating angle-bracket: " + element);
                final String suffix = element.endsWith("/>") ? "/>" : ">";
                element = element.substring(0, element.length() - suffix.length()) + " " + val + suffix;
            }
            document = replaceGroup(document, matcher, 1, element);
            start = matcher.start(1) + element.length();
        }

        return document;
    }

        public String getReplacement(String renderedValue) {
                return getReplace_attr() + "=\"" + renderedValue + "\"";
        }

}
