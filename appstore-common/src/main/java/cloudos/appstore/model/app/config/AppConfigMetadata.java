package cloudos.appstore.model.app.config;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.cobbzilla.util.io.FileUtil;
import org.cobbzilla.util.json.JsonUtil;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.cobbzilla.util.daemon.ZillaRuntime.die;
import static org.cobbzilla.util.daemon.ZillaRuntime.empty;

@Slf4j
public class AppConfigMetadata {

    public static final String CONFIG_METADATA_JSON = "config-metadata.json";
    public static final String ID = "config-metadata";

    public String getId() { return ID; }
    public void setId (String id) { /*noop*/ }

    @Getter @Setter private Map<String, AppConfigMetadataDatabag> categories = new HashMap<>();

    public static AppConfigMetadata load(File path) {
        final File metadataFile = path.isFile() && path.getName().equals(CONFIG_METADATA_JSON)
                ? path
                : path.isDirectory()
                    ? new File(path, CONFIG_METADATA_JSON)
                    : (File) die("not a "+CONFIG_METADATA_JSON+" file nor a directory containing one");
        if (metadataFile.exists()) {
            try {
                return loadOrDie(metadataFile);
            } catch (Exception e) {
                log.error("load: Error reading " + CONFIG_METADATA_JSON + " (returning null): " + e, e);
            }
        }
        return null;
    }

    public static AppConfigMetadata loadOrDie(File metadataFile) throws Exception {
        return JsonUtil.fromJson(FileUtil.toString(metadataFile), AppConfigMetadata.class);
    }

    public boolean hasPasswords() {
        for (AppConfigMetadataDatabag bag : categories.values()) {
            if (bag.getHas_passwords()) return true;
        }
        return false;
    }

    public Map<String, Map<String, AppConfigMetadataDatabagField>> getLocaleFields() {

        final Map<String, Map<String, AppConfigMetadataDatabagField>> localeFields = new HashMap<>();

        for (Map.Entry<String, AppConfigMetadataDatabag> bag : categories.entrySet()) {

            for (Map.Entry<String, AppConfigMetadataDatabagField> field : bag.getValue().getFields().entrySet()) {

                if (field.getValue().getIs_locale()) {
                    Map<String, AppConfigMetadataDatabagField> fields = localeFields.get(bag.getKey());
                    if (fields == null) {
                        fields = new HashMap<>();
                        localeFields.put(bag.getKey(), fields);
                    }
                    fields.put(field.getKey(), field.getValue());
                }
            }
        }

        return localeFields;
    }

    public boolean hasLocaleFields() { return !empty(getLocaleFields()); }

}
