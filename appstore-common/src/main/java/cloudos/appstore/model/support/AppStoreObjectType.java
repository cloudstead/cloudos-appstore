package cloudos.appstore.model.support;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum AppStoreObjectType {

    account, publisher, cloud;

    @JsonCreator public static AppStoreObjectType create(String value) { return valueOf(value.toLowerCase()); }

}
