package cloudos.appstore.model.app.config.validation;

import cloudos.appstore.model.app.config.AppConfigMetadataDatabagField;
import cloudos.appstore.model.app.config.AppConfigValidationResolver;
import org.cobbzilla.wizard.validation.ConstraintViolationBean;

import java.util.List;

public class BasicFieldValidator extends AppConfigFieldValidatorBase {

    public boolean isTooSmall(String value, int min) { return value.length() < min; }
    public boolean isTooLarge(String value, int max) { return value.length() > max; }

    @Override public List<ConstraintViolationBean> validate(String catName, String item, String value,
                                                            AppConfigMetadataDatabagField meta,
                                                            List<ConstraintViolationBean> violations,
                                                            AppConfigValidationResolver entityResolver) {

        violations = super.validate(catName, item, value, meta, violations, entityResolver);

        if (!empty(value)) {
            if (meta.hasMin() && isTooSmall(value, meta.getMin())) {
                violations.add(err(catName, item, "tooSmall", value));

            } else if (meta.hasMax() && isTooLarge(value, meta.getMax())) {
                violations.add(err(catName, item, "tooLong", value));
            }
        }

        return violations;
    }

}
