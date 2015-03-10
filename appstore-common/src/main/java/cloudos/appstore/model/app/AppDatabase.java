package cloudos.appstore.model.app;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.cobbzilla.util.string.StringUtil.empty;

public class AppDatabase {

    @Getter @Setter private AppDatabaseType type;
    @JsonIgnore public String getChefType () { return capitalize(type.name()); }
    @JsonIgnore public String getShortName () { return type.getShortName(); }

    @Getter @Setter private String dbname;
    @Getter @Setter private String dbuser;
    @Getter @Setter private String dbpass;
    @Getter @Setter private boolean allow_create_db = false;
    @Getter @Setter private Map<String, JsonNode> options;

    @Getter @Setter private String schema;

    @Getter @Setter private Boolean auto_migration;
    @Getter @Setter private String schema_version;
    @JsonIgnore public boolean hasSchemaVersion () { return !empty(schema_version); }

    // only applies to schema or here_schema, not init_command
    @Getter @Setter private String unless;

    @Getter @Setter private AppShellCommand init_command;
    @Getter @Setter private AppShellCommand upgrade_command;
    @Getter @Setter private AppDatabaseInsert[] inserts;
    @Getter @Setter private AppDatabaseSetConfig[] set_config;

    public static class AppDatabaseInsert {
        @Getter @Setter private String sql;
        @Getter @Setter private String dbname;
        @Getter @Setter private String unless;
    }

    public static class AppDatabaseSetConfig {
        @Getter @Setter private String group;
        @Getter @Setter private String name;
        @Getter @Setter private String value;
    }

}
