package cloudos.appstore.model.app.config;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.cobbzilla.util.io.FileUtil;
import org.cobbzilla.util.json.JsonUtil;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class AppConfigMetadata {

    private static final String CONFIG_METADATA_JSON = "config-metadata.json";

    @Getter @Setter private Map<String, AppConfigMetadataDatabag> metadataMap = new HashMap<>();

    public static AppConfigMetadata load(File databagsDir) {
        final File metadataFile = new File(databagsDir, CONFIG_METADATA_JSON);
        if (metadataFile.exists()) {
            try {
                return JsonUtil.fromJson(FileUtil.toString(metadataFile), AppConfigMetadata.class);
            } catch (Exception e) {
                log.error("load: Error reading " + CONFIG_METADATA_JSON + " (returning null): " + e, e);
            }
        }
        return null;
    }

}
