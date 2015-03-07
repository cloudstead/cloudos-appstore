package cloudos.appstore.model.app.config.validation;

import cloudos.appstore.model.app.config.AppConfigMetadataDatabagField;
import cloudos.appstore.model.app.config.AppConfigValidationResolver;
import org.cobbzilla.wizard.validation.ConstraintViolationBean;

import java.util.List;

import static org.cobbzilla.wizard.validation.ValidationRegexes.HOST_PATTERN;
import static org.cobbzilla.wizard.validation.ValidationRegexes.IPv4_PATTERN;

public class HostnameValidator extends AppConfigFieldValidatorBase {

    @Override protected boolean isValid(String value) {
        return HOST_PATTERN.matcher(value).matches() || IPv4_PATTERN.matcher(value).matches();
    }

    @Override public List<ConstraintViolationBean> validate(String catName, String item, String value,
                                                            AppConfigMetadataDatabagField meta,
                                                            List<ConstraintViolationBean> violations,
                                                            AppConfigValidationResolver entityResolver) {
        meta.setMax(255);
        return super.validate(catName, item, value, meta, violations, entityResolver);
    }
}
