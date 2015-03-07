package cloudos.appstore.model.app.config.validation;

import cloudos.appstore.model.app.config.AppConfigMetadataDatabagField;
import cloudos.appstore.model.app.config.AppConfigValidationResolver;
import org.cobbzilla.wizard.validation.ConstraintViolationBean;

import java.util.List;

public class CloudOsGroupFieldValidator extends AppConfigFieldValidatorBase {

    @Override public List<ConstraintViolationBean> validate(String catName, String item, String value,
                                                            AppConfigMetadataDatabagField meta,
                                                            List<ConstraintViolationBean> violations,
                                                            AppConfigValidationResolver entityResolver) {

        violations = super.validate(catName, item, value, meta, violations, entityResolver);
        if (!entityResolver.isValidGroup(value)) {
            violations.add(err(catName, item, "invalid", value));
        }
        return violations;
    }
}
