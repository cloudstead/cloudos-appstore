package cloudos.appstore.model.app.config.validation;

import static org.cobbzilla.util.string.ValidationRegexes.*;

public class EmailFieldValidator extends AppConfigFieldValidatorBase {

    @Override protected boolean isValid(String value) {
        value = value.replace("@hostname", "example.example.com").replace("@domain", "example.com");
        return EMAIL_PATTERN.matcher(value).matches();
    }

}
