package cloudos.appstore.model.support;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum  AppInstallStatus {

    unavailable, available, installing, installed;

    @JsonCreator public static AppInstallStatus create(String value) { return valueOf(value.toLowerCase()); }

}
