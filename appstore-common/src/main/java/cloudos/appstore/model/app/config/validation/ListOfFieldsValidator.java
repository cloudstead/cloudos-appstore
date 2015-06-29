package cloudos.appstore.model.app.config.validation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.cobbzilla.util.string.StringUtil;

@NoArgsConstructor @AllArgsConstructor
public class ListOfFieldsValidator extends AppConfigFieldValidatorBase {

    private static final String DEFAULT_DELIMS = ", \t\r\n";

    @Getter @Setter protected IpAddressFieldValidator fieldValidator;
    @Getter @Setter protected String delimiters = DEFAULT_DELIMS;

    public ListOfFieldsValidator(IpAddressFieldValidator fieldValidator) {
        this.fieldValidator = fieldValidator;
    }

    @Override protected boolean isValid(String value) {
        for (String val : StringUtil.split(value, delimiters)) {
            if (!isElementValid(val)) return false;
        }
        return true;
    }

    protected boolean isElementValid(String val) { return fieldValidator.isValid(val); }

}
