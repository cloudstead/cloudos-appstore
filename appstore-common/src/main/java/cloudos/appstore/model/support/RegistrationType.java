package cloudos.appstore.model.support;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum RegistrationType {

    publisher, consumer;

    @JsonCreator public static RegistrationType create(String value) { return valueOf(value.toLowerCase()); }

}
