package cloudos.appstore.model.app;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static org.cobbzilla.util.daemon.ZillaRuntime.empty;

@AllArgsConstructor
public enum AppLevel {

    init (0), system (1), cloudos (2), app (3);

    @Getter private final int level;

    @JsonCreator public static AppLevel create (String v) { return empty(v) ? null : valueOf(v.toLowerCase()); }

}
