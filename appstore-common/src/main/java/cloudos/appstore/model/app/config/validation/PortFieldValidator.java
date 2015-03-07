package cloudos.appstore.model.app.config.validation;

import org.cobbzilla.wizard.validation.ValidationRegexes;

public class PortFieldValidator extends RegexFieldValidator {

    public PortFieldValidator () { super(ValidationRegexes.PORT_PATTERN); }

    @Override protected boolean isValid(String value) {
        if (!super.isValid(value)) return false;
        try {
            int port = Integer.parseInt(value);
            return port > 0 && port < 65536;
        } catch (Exception e) {
            return false;
        }
    }

}
