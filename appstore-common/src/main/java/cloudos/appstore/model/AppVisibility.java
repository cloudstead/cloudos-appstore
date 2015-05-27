package cloudos.appstore.model;

import com.fasterxml.jackson.annotation.JsonCreator;

import static org.cobbzilla.util.daemon.ZillaRuntime.empty;

public enum AppVisibility {

    everyone, members, publisher;

    public static final AppVisibility[] EVERYONE_OR_MEMBER = {AppVisibility.everyone, AppVisibility.members};

    @JsonCreator public static AppVisibility create (String v) { return empty(v) ? null : valueOf(v.toLowerCase()); }

}
