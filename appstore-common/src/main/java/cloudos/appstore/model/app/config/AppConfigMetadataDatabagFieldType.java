package cloudos.appstore.model.app.config;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum AppConfigMetadataDatabagFieldType {

    field, login, password, yesno, cloudos_group, member_list;

    @JsonCreator public static AppConfigMetadataDatabagFieldType create (String name) { return valueOf(name.toLowerCase()); }

}
