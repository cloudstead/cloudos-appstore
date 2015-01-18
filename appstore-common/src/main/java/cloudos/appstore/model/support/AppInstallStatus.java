package cloudos.appstore.model.support;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum AppInstallStatus {

    unavailable, available, installing, installed, upgrade_available;

    @JsonCreator public static AppInstallStatus create(String value) { return valueOf(value.toLowerCase()); }

}
