package cloudos.appstore.model.app;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum AppWebType {

    apache;

    @JsonCreator public static AppWebType create(String value) { return valueOf(value.toLowerCase()); }

}
