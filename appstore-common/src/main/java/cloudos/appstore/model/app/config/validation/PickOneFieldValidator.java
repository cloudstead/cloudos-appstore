package cloudos.appstore.model.app.config.validation;

import cloudos.appstore.model.app.config.AppConfigMetadataDatabagField;
import cloudos.appstore.model.app.config.AppConfigValidationResolver;
import org.apache.commons.lang3.ArrayUtils;
import org.cobbzilla.wizard.validation.ConstraintViolationBean;

import java.util.List;

import static org.cobbzilla.util.string.StringUtil.empty;

public class PickOneFieldValidator extends AppConfigFieldValidatorBase {

    @Override public List<ConstraintViolationBean> validate(String catName, String item, String value,
                                                            AppConfigMetadataDatabagField meta,
                                                            List<ConstraintViolationBean> violations,
                                                            AppConfigValidationResolver entityResolver) {

        violations = super.validate(catName, item, value, meta, violations, entityResolver);

        if (!empty(value)) {
            if (!ArrayUtils.contains(meta.getChoices(), value)) {
                violations.add(err(catName, item, "invalid", value));
            }
        }

        return violations;
    }

}
