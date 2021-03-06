package cloudos.appstore.model.app.filter;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.beanutils.BeanUtils;
import org.cobbzilla.util.mustache.MustacheUtil;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.cobbzilla.util.mustache.MustacheUtil.renderBoolean;

public abstract class AppFilterHandlerBase implements AppFilterHandler {

    public static final int REGEX_FLAGS = Pattern.MULTILINE | Pattern.DOTALL | Pattern.CASE_INSENSITIVE;

    @Getter @Setter private String only_if = "true";
    @Getter @Setter private String not_if = "false";

    public int getRegexFlags() { return REGEX_FLAGS; }

    @Override public AppFilterHandler configure(Map<String, String> config) {
        try {
            BeanUtils.populate(this, config);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid config ("+config+"): "+e, e);
        }
        return this;
    }

    public String render(String string, Map<String, Object> scope) {
        return renderBoolean(only_if, scope) && !renderBoolean(not_if, scope)
                ? MustacheUtil.render(string, scope)
                : string;
    }


    public Pattern elementPattern(String element, String attrName, String attrValue) {
        return Pattern.compile("(<\\s*" + element + "\\s+[^>]*?" + attrName + "\\s*=\\s*\"" + attrValue + "\"[^>]*>)", getRegexFlags());
    }

    public Pattern attributePattern(final String attrName) {
        return Pattern.compile("\\s+(" + attrName + "\\s*=\\s*\".+?\"){1}?\\s*", getRegexFlags());
    }

    public static String replaceGroup (String document, Matcher matcher, int group, String value) {
        return document.substring(0, matcher.start(group)) + value + document.substring(matcher.end(group));
    }

}
