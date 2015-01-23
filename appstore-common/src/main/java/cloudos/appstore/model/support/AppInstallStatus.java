package cloudos.appstore.model.support;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum AppInstallStatus {

    unavailable, // not available from anywhere (may have been removed from app store and no local copy downloaded)
    available_appstore,               // available from app store (not downloaded/installed yet)
    available_local,                  // available locally (downloaded to app repository) but not installed
    installed,                        // installed
    upgrade_available_installed,      // installed locally but upgrade available from app store
    upgrade_available_not_installed;  // downloaded (but not installed) and upgrade available from app store

    @JsonCreator public static AppInstallStatus create(String value) { return valueOf(value.toLowerCase()); }

}
