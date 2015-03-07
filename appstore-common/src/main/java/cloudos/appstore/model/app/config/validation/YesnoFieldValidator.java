package cloudos.appstore.model.app.config.validation;

public class YesnoFieldValidator extends AppConfigFieldValidatorBase {

    @Override protected boolean isValid(String value) {
        return super.isValid(value) && (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false"));
    }

}
