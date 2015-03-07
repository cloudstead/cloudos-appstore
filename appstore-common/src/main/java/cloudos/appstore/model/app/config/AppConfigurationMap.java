package cloudos.appstore.model.app.config;

import cloudos.appstore.model.app.AppManifest;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import rooty.toots.chef.ChefSolo;

import java.io.File;
import java.util.*;

@Slf4j
public class AppConfigurationMap {

    // Map of appname->config
    @Getter @Setter private Map<String, AppConfiguration> appConfigs = new LinkedHashMap<>();

    @JsonIgnore public Collection<String> getApps() { return appConfigs.keySet(); }

    public void addAll(List<String> apps, File chefDir, String locale) {
        for (String app : apps) add(app, chefDir, locale);
    }

    public void add(String app, File chefDir, String locale) {
        final File cookbookDir = new File(chefDir, ChefSolo.COOKBOOKS_DIR);
        final File databagDir = new File(chefDir, ChefSolo.DATABAGS_DIR);

        final File appCookbook = new File(cookbookDir, app);
        final File appDatabags = new File(databagDir, app);

        if (!appCookbook.exists()) throw new IllegalArgumentException("No cookbook found for app: "+app);
        if (!appDatabags.exists()) {
            log.warn("No databags for app (not adding any config): "+app);
            return;
        }

        final File manifestFile = new File(appDatabags, AppManifest.CLOUDOS_MANIFEST_JSON);
        if (!manifestFile.exists()) {
            log.warn("No manifest for app (not adding any config): "+app);
            return;
        }

        final AppManifest manifest = AppManifest.load(manifestFile);
        if (manifest.hasDatabags()) {
            appConfigs.put(app, AppConfiguration.getAppConfiguration(manifest, databagDir, locale));
        }
    }

}
