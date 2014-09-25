package cloudos.appstore.model.app;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

public enum AppDatabaseType {

    mysql ("mysql"), postgresql ("pgsql");

    AppDatabaseType(String shortName) { this.shortName = shortName; }

    @Getter private final String shortName;

    @JsonCreator public static AppDatabaseType create(String value) { return valueOf(value.toLowerCase()); }

}
