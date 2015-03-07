package cloudos.appstore.model.app.config.validation;

import cloudos.appstore.model.app.config.AppConfigMetadataDatabagField;
import cloudos.appstore.model.app.config.AppConfigValidationResolver;
import org.cobbzilla.wizard.validation.ConstraintViolationBean;

import java.util.List;

public interface AppConfigFieldValidator {

    public List<ConstraintViolationBean> validate(String catName, String item, String value,
                                                  AppConfigMetadataDatabagField meta,
                                                  List<ConstraintViolationBean> violations,
                                                  AppConfigValidationResolver entityResolver);

}
