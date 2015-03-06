package cloudos.appstore.model.app.config;

import lombok.extern.slf4j.Slf4j;
import org.cobbzilla.util.io.FileUtil;
import org.cobbzilla.util.json.JsonUtil;
import org.cobbzilla.util.string.LocaleUtil;

import java.io.File;
import java.util.HashMap;

@Slf4j
public class AppConfigTranslations extends HashMap<String, AppConfigTranslationsDatabag> {

    private static final String TRANSLATIONS_JSON = "translations.json";

    public static AppConfigTranslations load(File databagsDir, String locale) {
        final File translationsFile = LocaleUtil.findLocaleFile(new File(databagsDir, TRANSLATIONS_JSON), locale);
        try {
            return (translationsFile != null && translationsFile.exists())
                    ? JsonUtil.fromJson(FileUtil.toString(translationsFile), AppConfigTranslations.class)
                    : null;
        } catch (Exception e) {
            log.error("Error reading "+TRANSLATIONS_JSON+" (returning null): "+e, e);
            return null;
        }
    }

}
