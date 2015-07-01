package cloudos.appstore.model.app.config;

import java.util.HashMap;

public class AppConfigTranslationCategory extends HashMap<String, AppConfigTranslation> {

    public boolean hasChoice(String field, String choice) {
        return containsKey(field) && get(field).hasChoice(choice);
    }

}
