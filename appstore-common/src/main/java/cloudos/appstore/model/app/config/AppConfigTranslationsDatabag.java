package cloudos.appstore.model.app.config;

import cloudos.appstore.model.AppMutableData;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.cobbzilla.util.json.JsonUtil;
import org.cobbzilla.util.string.LocaleUtil;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.cobbzilla.util.daemon.ZillaRuntime.empty;
import static org.cobbzilla.util.io.FileUtil.*;
import static org.cobbzilla.util.json.JsonUtil.fromJsonOrDie;
import static org.cobbzilla.util.json.JsonUtil.toJsonOrDie;

@Slf4j
public class AppConfigTranslationsDatabag {

    public static final String ID = "translations";
    public static final String TRANSLATIONS_JSON = ID + ".json";

    public static boolean isTranslationFile(File f) {
        return f.getName().startsWith(AppConfigTranslationsDatabag.ID) && f.getName().endsWith(".json");
    }
    public static boolean isDefaultTranslationFile(File f) {
        return f.getName().equals(AppConfigTranslationsDatabag.TRANSLATIONS_JSON);
    }

    public String getId() { return ID; }
    public void setId (String id) { /*noop*/ }

    @Getter @Setter private AppMutableData assets = new AppMutableData();

    @Getter @Setter private Map<String, AppConfigTranslationCategory> categories = new HashMap<>();

    public boolean hasCategories() { return !empty(categories); }

    public AppConfigTranslationCategory getTranslations (String category) { return categories.get(category); }

    public static AppConfigTranslationsDatabag load(File databagsDir, String locale) {
        final File translationsFile = LocaleUtil.findLocaleFile(new File(databagsDir, TRANSLATIONS_JSON), locale);
        try {
            return (translationsFile != null && translationsFile.exists())
                    ? JsonUtil.fromJson(translationsFile, AppConfigTranslationsDatabag.class)
                    : null;
        } catch (Exception e) {
            log.error("Error reading "+abs(translationsFile)+" (returning null): "+e, e);
            return null;
        }
    }

    public static AppConfigTranslationsDatabag loadOrDie(File f) { return fromJsonOrDie(toStringOrDie(f), AppConfigTranslationsDatabag.class); }

    public void save(File file) { toFileOrDie(file, toJsonOrDie(this)); }

}
