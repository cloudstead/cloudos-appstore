package cloudos.appstore.model.app.config.validation;

import org.quartz.CronExpression;

public class CronFieldValidator extends AppConfigFieldValidatorBase {

    @Override protected boolean isValid(String value) {
        return super.isValid(value) && CronExpression.isValidExpression(value);
    }

}
