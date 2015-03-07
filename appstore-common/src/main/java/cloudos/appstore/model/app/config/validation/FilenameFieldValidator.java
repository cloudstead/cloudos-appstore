package cloudos.appstore.model.app.config.validation;

import static org.cobbzilla.wizard.validation.ValidationRegexes.FILENAME_PATTERN;

public class FilenameFieldValidator extends RegexFieldValidator {

    public FilenameFieldValidator() { super(FILENAME_PATTERN); }

    @Override protected boolean isValid(String value) {
        return super.isValid(value) && (value.equals(".") || value.equals("..") || value.contains("/") || value.contains("\\"));
    }
}
