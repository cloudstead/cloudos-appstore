package cloudos.appstore.model.app.config.validation;

import org.cobbzilla.util.string.ValidationRegexes;

public class IntegerFieldValidator extends RegexFieldValidator {

    public IntegerFieldValidator() { super(ValidationRegexes.INTEGER_PATTERN); }

    @Override public boolean isTooSmall(String value, int min) { return Integer.valueOf(value) < min; }
    @Override public boolean isTooLarge(String value, int max) { return Integer.valueOf(value) > max; }

}
