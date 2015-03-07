package cloudos.appstore.model.app.config.validation;

import cloudos.appstore.model.app.config.AppConfigMetadataDatabagField;
import cloudos.appstore.model.app.config.AppConfigValidationResolver;
import org.cobbzilla.wizard.validation.ConstraintViolationBean;

import java.util.List;

import static org.cobbzilla.util.string.StringUtil.empty;

public class BasicFieldValidator extends AppConfigFieldValidatorBase {

    @Override public List<ConstraintViolationBean> validate(String catName, String item, String value,
                                                            AppConfigMetadataDatabagField meta,
                                                            List<ConstraintViolationBean> violations,
                                                            AppConfigValidationResolver entityResolver) {

        violations = super.validate(catName, item, value, meta, violations, entityResolver);

        if (!empty(value)) {
            if (meta.hasMin() && value.length() < meta.getMin()) {
                violations.add(err(catName, item, "tooShort", value));

            } else if (meta.hasMax() && value.length() > meta.getMax()) {
                violations.add(err(catName, item, "tooLong", value));
            }
        }

        return violations;
    }

}
