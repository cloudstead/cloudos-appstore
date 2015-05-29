package cloudos.appstore.model.app.config.validation;

import java.util.regex.Pattern;

import static org.cobbzilla.wizard.validation.ValidationRegexes.LOCALE_PATTERNS;

public class LocaleFieldValidator extends PickOneFieldValidator {

    @Override protected boolean isValid(String value) {
        // todo: we should load default-locale-names.json and validate that the value exactly matches something there
        for (Pattern p : LOCALE_PATTERNS) {
            if (p.matcher(value).matches()) return true;
        }
        return false;
    }

}
