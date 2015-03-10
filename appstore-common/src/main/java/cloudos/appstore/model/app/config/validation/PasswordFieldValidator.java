package cloudos.appstore.model.app.config.validation;

import cloudos.appstore.model.app.config.AppConfigMetadataDatabagField;

public class PasswordFieldValidator extends BasicFieldValidator {

    /**
     * @return true, since empty passwords will be generated automatically by Chef
     * @param meta
     */
    protected boolean autofillAtChefTime(AppConfigMetadataDatabagField meta) { return meta.hasLogin(); }

}
