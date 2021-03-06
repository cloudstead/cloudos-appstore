package cloudos.appstore.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;

import static org.cobbzilla.util.daemon.ZillaRuntime.empty;

public enum CloudAppStatus {

    created, pending, published, retired;

    @JsonCreator public static CloudAppStatus create (String name) { return empty(name) ? null : valueOf(name.toLowerCase()); }

    @JsonIgnore public boolean isPublished() { return this == published; }

}
