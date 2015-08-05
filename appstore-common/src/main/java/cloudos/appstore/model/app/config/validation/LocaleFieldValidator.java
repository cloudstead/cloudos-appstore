package cloudos.appstore.model.app.config.validation;

import java.util.regex.Pattern;

import static org.cobbzilla.util.string.ValidationRegexes.LOCALE_PATTERNS;

public class LocaleFieldValidator extends PickOneFieldValidator {

    @Override protected boolean isValid(String value) {

        // allow value of "@lang", chef installer will substitute at install-time
        if ("@lang".equals(value)) return true;

        // todo: we should load default-locale-names.json and validate that the value exactly matches something there
        for (Pattern p : LOCALE_PATTERNS) {
            if (p.matcher(value).matches()) return true;
        }
        return false;
    }

}
