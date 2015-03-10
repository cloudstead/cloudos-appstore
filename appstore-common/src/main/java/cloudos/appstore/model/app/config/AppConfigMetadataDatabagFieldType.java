package cloudos.appstore.model.app.config;

import cloudos.appstore.model.app.config.validation.*;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import org.cobbzilla.wizard.validation.ConstraintViolationBean;

import java.util.List;

import static org.cobbzilla.wizard.validation.ValidationRegexes.*;

@AllArgsConstructor
public enum AppConfigMetadataDatabagFieldType {

    // simple types
    field    (new BasicFieldValidator()),
    hash     (new BasicFieldValidator()),
    password (new PasswordFieldValidator()),

    // regex-based types
    email          (new RegexFieldValidator(EMAIL_PATTERN)),
    login          (new RegexFieldValidator(LOGIN_PATTERN)),
    ipv4           (new RegexFieldValidator(IPv4_PATTERN)),
    ipv6           (new RegexFieldValidator(IPv6_PATTERN)),
    hostname_part  (new RegexFieldValidator(HOST_PART_PATTERN)),
    url            (new RegexFieldValidator(URL_PATTERN)),
    http           (new RegexFieldValidator(HTTP_PATTERN)),
    https          (new RegexFieldValidator(HTTPS_PATTERN)),

    // custom types
    hostname   (new HostnameFieldValidator()),
    filename   (new FilenameFieldValidator()),
    port       (new PortFieldValidator()),
    yesno      (new YesnoFieldValidator()),
    pick_one   (new PickOneFieldValidator()),
    pick_many  (new PickManyFieldValidator()),
    cron       (new CronFieldValidator()),

    // types that require entity-resolver
    cloudos_group (new CloudOsGroupFieldValidator()),
    member_list (new CloudOsMemberListFieldValidator());

    private final AppConfigFieldValidator validator;

    @JsonCreator public static AppConfigMetadataDatabagFieldType create (String name) { return valueOf(name.toLowerCase()); }

    public List<ConstraintViolationBean> validate(String catName, String item, String value,
                                                  AppConfigMetadataDatabagField meta,
                                                  List<ConstraintViolationBean> violations,
                                                  AppConfigValidationResolver entityResolver) {
        return validator.validate(catName, item, value, meta, violations, entityResolver);
    }

    @JsonIgnore public boolean getIs_password() { return this == password; }

}
