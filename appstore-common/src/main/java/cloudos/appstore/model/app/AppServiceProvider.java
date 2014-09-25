package cloudos.appstore.model.app;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum AppServiceProvider {

    init, upstart;

    @JsonCreator public static AppServiceProvider create(String value) { return valueOf(value.toLowerCase()); }

}
