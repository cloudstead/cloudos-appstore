package cloudos.appstore.model.app.config.validation;

import cloudos.appstore.model.app.config.AppConfigMetadataDatabagField;
import cloudos.appstore.model.app.config.AppConfigValidationResolver;
import org.cobbzilla.util.string.StringUtil;
import org.cobbzilla.wizard.validation.ConstraintViolationBean;
import rooty.toots.vendor.VendorSettingHandler;

import java.util.List;

public class AppConfigFieldValidatorBase implements AppConfigFieldValidator {

    public static ConstraintViolationBean err(String catName, String item, String problem, String value) {
        return new ConstraintViolationBean("{err." + catName + "." + item + "." + problem + "}", null, value);
    }

    public boolean empty (Object o) {
        return StringUtil.empty(o) || o.toString().equals(VendorSettingHandler.VALUE_NOT_SET);
    }

    protected boolean isValid(String value) { return true; }

    /**
     *  @return true if it is OK for this otherwise required field to be empty, because Chef will generate a value for it automatically.
     * @param meta
     */
    protected boolean autofillAtChefTime(AppConfigMetadataDatabagField meta) { return false; }

    @Override public List<ConstraintViolationBean> validate(String catName, String item, String value,
                                                            AppConfigMetadataDatabagField meta,
                                                            List<ConstraintViolationBean> violations,
                                                            AppConfigValidationResolver entityResolver) {

        final boolean required = meta.isRequired();
        if (!required && empty(value)) return violations;

        if (required && empty(value) && !autofillAtChefTime(meta)) {
            violations.add(err(catName, item, "empty", value));

        } else if (!isValid(value)) {
            violations.add(err(catName, item, "invalid", value));
        }

        return violations;
    }

}
