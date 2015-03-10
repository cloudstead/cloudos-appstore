package cloudos.appstore.model.app.config;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.cobbzilla.util.io.FileUtil;
import org.cobbzilla.util.json.JsonUtil;
import org.cobbzilla.util.string.LocaleUtil;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.cobbzilla.util.io.FileUtil.abs;
import static org.cobbzilla.util.io.FileUtil.toStringOrDie;
import static org.cobbzilla.util.json.JsonUtil.fromJsonOrDie;

@Slf4j
public class AppConfigTranslationsDatabag {

    private static final String TRANSLATIONS_JSON = "translations.json";
    public static final String ID = "translations";

    public String getId() { return ID; }
    public void setId (String id) { /*noop*/ }

    @Getter @Setter private Map<String, AppConfigTranslationCategory> categories = new HashMap<>();

    public AppConfigTranslationCategory getTranslations (String category) { return categories.get(category); }

    public static AppConfigTranslationsDatabag load(File databagsDir, String locale) {
        final File translationsFile = LocaleUtil.findLocaleFile(new File(databagsDir, TRANSLATIONS_JSON), locale);
        try {
            return (translationsFile != null && translationsFile.exists())
                    ? JsonUtil.fromJson(FileUtil.toString(translationsFile), AppConfigTranslationsDatabag.class)
                    : null;
        } catch (Exception e) {
            log.error("Error reading "+abs(translationsFile)+" (returning null): "+e, e);
            return null;
        }
    }

    public static AppConfigTranslationsDatabag loadOrDie(File f) { return fromJsonOrDie(toStringOrDie(f), AppConfigTranslationsDatabag.class); }

}
