package cloudos.appstore.model.app.config;

import cloudos.appstore.model.app.AppManifest;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ArrayUtils;
import org.cobbzilla.wizard.validation.ConstraintViolationBean;
import rooty.toots.chef.ChefSolo;

import java.io.File;
import java.util.*;

import static org.cobbzilla.util.daemon.ZillaRuntime.empty;

/**
 * Aggregates AppConfiguration for multiple apps on a cloudstead.
 */
@Slf4j @Accessors(chain=true)
public class AppConfigurationMap {

    // Map of appname->config
    @Getter @Setter private Map<String, AppConfiguration> appConfigs = new LinkedHashMap<>();

    @JsonIgnore public Collection<String> getApps() { return appConfigs.keySet(); }

    @JsonIgnore public Map<String, List<ConstraintViolationBean>> getViolations () {
        final Map<String, List<ConstraintViolationBean>> violations = new HashMap<>();
        for (String app : appConfigs.keySet()) {
            final List<ConstraintViolationBean> vlist = appConfigs.get(app).getViolations();
            if (!empty(vlist)) violations.put(app, vlist);
        }
        return violations;
    }

    public AppConfiguration getConfig(String name) { return appConfigs.get(name); }

    public void removeConfig(String app) { appConfigs.remove(app); }

    public void addAll(List<String> apps, File chefDir, String locale) {
        for (String app : apps) add(app, chefDir, locale);
    }

    public void add(String app, File chefDir, String locale) {
        final File cookbookDir = new File(chefDir, ChefSolo.COOKBOOKS_DIR);
        final File databagDir = new File(chefDir, ChefSolo.DATABAGS_DIR);

        final File appCookbook = new File(cookbookDir, app);
        final File appDatabags = new File(databagDir, app);

        if (!appCookbook.exists()) {
            log.warn("No cookbook for app (not adding config): "+app);
            return;
        }
        if (!appDatabags.exists()) {
            log.warn("No databags for app (not adding config): "+app);
            return;
        }

        final File manifestFile = new File(appDatabags, AppManifest.CLOUDOS_MANIFEST_JSON);
        if (!manifestFile.exists()) {
            log.warn("No manifest for app (not adding config): "+app);
            return;
        }

        final AppManifest manifest = AppManifest.load(manifestFile);
        if (manifest.hasConfig()) {
            appConfigs.put(app, AppConfiguration.readAppConfiguration(manifest, databagDir, locale));
        }
    }

    public Map<String, List<ConstraintViolationBean>> validate(AppConfigValidationResolver resolver) {
        return validate(resolver, null);
    }

    public Map<String, List<ConstraintViolationBean>> validate(AppConfigValidationResolver resolver, String[] skipApps) {

        final Map<String, List<ConstraintViolationBean>> violations = new HashMap<>();

        for (Map.Entry<String, AppConfiguration> entry : getAppConfigs().entrySet()) {
            // skip this app?
            final String appName = entry.getKey();
            if (ArrayUtils.contains(skipApps, appName)) {
                log.info("validate: skipping app "+ appName);
                continue;
            }
            // validate this app...
            final List<ConstraintViolationBean> vlist = entry.getValue().validate(resolver);
            if (!empty(vlist)) violations.put(appName, vlist);
        }
        return violations;
    }

}
