package cloudos.appstore.model.app.config.validation;

import cloudos.appstore.model.app.config.AppConfigMetadataDatabagField;
import cloudos.appstore.model.app.config.AppConfigValidationResolver;
import org.cobbzilla.wizard.validation.ConstraintViolationBean;

import java.util.List;

import static org.cobbzilla.util.string.StringUtil.empty;

public class AppConfigFieldValidatorBase implements AppConfigFieldValidator {

    public static ConstraintViolationBean err(String catName, String item, String problem, String value) {
        return new ConstraintViolationBean("{err." + catName + "." + item + "." + problem + "}", null, value);
    }

    protected boolean isValid(String value) { return true; }

    @Override public List<ConstraintViolationBean> validate(String catName, String item, String value,
                                                            AppConfigMetadataDatabagField meta,
                                                            List<ConstraintViolationBean> violations,
                                                            AppConfigValidationResolver entityResolver) {

        final boolean required = meta.isRequired();
        if (!required && empty(value)) return violations;

        if (required && empty(value)) {
            violations.add(err(catName, item, "empty", value));

        } else if (!isValid(value)) {
            violations.add(err(catName, item, "invalid", value));
        }

        return violations;
    }

}
