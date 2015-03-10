package cloudos.appstore.model.app.config;

import cloudos.appstore.model.app.AppDatabagDef;
import cloudos.appstore.model.app.AppLayout;
import cloudos.appstore.model.app.AppManifest;
import cloudos.appstore.model.app.config.validation.AppConfigFieldValidatorBase;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.cobbzilla.util.io.FileUtil;
import org.cobbzilla.util.json.JsonEdit;
import org.cobbzilla.util.json.JsonEditOperation;
import org.cobbzilla.util.json.JsonEditOperationType;
import org.cobbzilla.util.json.JsonUtil;
import org.cobbzilla.util.mustache.MustacheUtil;
import org.cobbzilla.util.reflect.ReflectionUtil;
import org.cobbzilla.util.system.CommandShell;
import org.cobbzilla.wizard.validation.ConstraintViolationBean;
import rooty.toots.vendor.VendorDatabag;
import rooty.toots.vendor.VendorSettingHandler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.cobbzilla.util.daemon.ZillaRuntime.die;
import static org.cobbzilla.util.json.JsonUtil.FULL_MAPPER;
import static org.cobbzilla.util.security.ShaUtil.sha256_hex;
import static org.cobbzilla.util.string.StringUtil.empty;

/**
 * Represents the configuration for a given app+version
 */
@Slf4j
public class AppConfiguration {

    @Getter @Setter private List<AppConfigurationCategory> categories = new ArrayList<>();
    public void add(AppConfigurationCategory category) { categories.add(category); }

    @Getter @Setter private AppConfigMetadata metadata;
    @Getter @Setter private AppConfigTranslationsDatabag translations;
    @Getter @Setter private List<ConstraintViolationBean> violations;

    public AppConfigurationCategory getCategory(String name) {
        for (AppConfigurationCategory cat : categories) {
            if (cat.getName().equals(name)) return cat;
        }
        return null;
    }

    public static AppConfiguration fromLayout(AppLayout layout, String locale) {
        if (!layout.exists()) return null;
        final AppManifest manifest = AppManifest.load(layout.getManifest());
        final File databagsDir = layout.getDatabagsDir();
        return readAppConfiguration(manifest, databagsDir, locale);
    }

    public static String getShasum(Object databag, String config) {
        return sha256_hex(String.valueOf(ReflectionUtil.get(databag, config)));
    }

    /**
     * Load an AppConfiguration from a manifest and databag.
     * @param manifest The AppManifest. The "config" section is inspected to determine app config options
     * @param databagsDir The databags directory for the app (should contain cloudos-manifest.json and all other databags)
     * @param locale The locale for translations (we'll attach the best-match translations.json file for the locale to the AppConfiguration)
     * @return The AppConfiguration for the app
     */
    public static AppConfiguration readAppConfiguration(AppManifest manifest, File databagsDir, String locale) {
        final AppConfiguration config = new AppConfiguration();
        if (manifest.hasDatabags()) {
            databagsDir = new File(databagsDir, manifest.getName());
            for (AppDatabagDef databag : manifest.getConfig()) {

                final String databagName = databag.getName();
                final File databagFile = new File(databagsDir, databagName+".json");
                final JsonNode node = databagFile.exists() ? JsonUtil.toNode(databagFile) : null;

                final AppConfigurationCategory category = new AppConfigurationCategory(databagName);
                config.add(category);

                // does this databag define vendor-defaults that should be hidden?
                final VendorDatabag vendor = getVendorDatabag(node);

                // add items found in the databag to the AppConfigurationCategory
                for (String item : databag.getItems()) {
                    category.add(item);
                    if (node != null) {
                        try {
                            final JsonNode itemNode = JsonUtil.findNode(node, item);
                            if (itemNode != null) {
                                String value = itemNode.asText();
                                if (vendor != null && vendor.isDefault(item, value)) {
                                    value = VendorSettingHandler.VENDOR_DEFAULT;
                                }

                                final Map<String, Object> scope = new HashMap<>();
                                scope.put("hostname", CommandShell.hostname());
                                category.set(item, MustacheUtil.render(value, scope));

                            } else {
                                category.set(item, VendorSettingHandler.VALUE_NOT_SET);
                            }

                        } catch (IOException e) {
                            log.warn("Error loading databag item (" + databagName + "/" + item + "): " + e);
                        }
                    }
                }
            }
        }

        // attach config-metadata if found
        config.setMetadata(AppConfigMetadata.load(databagsDir));

        // attach translations if found
        config.setTranslations(AppConfigTranslationsDatabag.load(databagsDir, locale));

        return config;
    }

    public void writeAppConfiguration(AppManifest manifest, File databagsDir) {
        if (manifest.hasDatabags()) {
            for (AppDatabagDef databag : manifest.getConfig()) {

                final String databagName = databag.getName();

                final List<JsonEditOperation> operations = new ArrayList<>();
                operations.add(new JsonEditOperation()
                        .setType(JsonEditOperationType.write)
                        .setPath("id")
                        .setJson("\"" + databagName + "\""));

                // Did the caller provide config for this category?
                final AppConfigurationCategory category = getCategory(databagName);
                if (category == null) {
                    log.warn("No configuration provided for category (" + databagName + "), skipping");
                    continue;
                }

                // Does this category exist as a databag? If not create a new JsonNode to represent it
                final File databagFile = new File(databagsDir, databagName+".json");
                final JsonNode node = databagFile.exists()
                        ? AppLayout.getDatabagNode(databagFile)
                        : new ObjectNode(FULL_MAPPER.getNodeFactory());

                // Update config settings via JSON
                for (String item : databag.getItems()) {
                    // Did the caller provide a value for this config item?
                    final String value = category.getValues().get(item);
                    if (value != null) {
                        // If the value is the special 'default' value, skip this and do not edit anything
                        if (value.equals(VendorSettingHandler.VENDOR_DEFAULT)) {
                            log.info("skipping value (not changed from default): " + item);
                            continue;
                        }
                        if (value.equals(VendorSettingHandler.VALUE_NOT_SET)) {
                            log.info("skipping empty value (nothing set): "+item);
                            continue;
                        }

                        // If the setting already exists in the data bag, determine the type from there
                        final JsonEditOperation op = new JsonEditOperation()
                                .setType(JsonEditOperationType.write)
                                .setPath(item);

                        try {
                            final JsonNode existing = JsonUtil.findNode(node, item);
                            final String json;
                            if (existing != null) {
                                json = JsonUtil.toJson(JsonUtil.getValueNode(existing, item, value));
                            } else {
                                json = "\"" + value + "\""; // assume String
                            }
                            op.setJson(json);
                            operations.add(op);

                        } catch (Exception e) {
                            die("Error preparing to write " + databagName + "/" + item + ": " + e, e);
                        }
                    }
                }

                final String updatedJson;
                try {
                    updatedJson = new JsonEdit().setJsonData(node).setOperations(operations).edit();
                    FileUtil.toFile(databagFile, updatedJson);

                } catch (Exception e) {
                    die("Error generating updated json: " + e);
                }

            }
        }
    }

    public static VendorDatabag getVendorDatabag(JsonNode node) {
        try {
            return JsonUtil.fromJson(node, "vendor", VendorDatabag.class);
        } catch (Exception e) {
            log.warn("Error finding 'vendor' section in databag: " + e);
            return null;
        }
    }

    @JsonIgnore public Map<String, Map<String, String>> getDatabagMap() {

        final Map<String, Map<String, String>> databags = new HashMap<>();

        for (AppConfigurationCategory cat : getCategories()) {
            final Map<String, String> values = new HashMap<>();
            if (cat.hasValues()) {
                final Map<String, String> databagValues = cat.getValues();
                for (String key : databagValues.keySet()) {
                    final String value = databagValues.get(key);
                    if (key.contains(".")) values.put(key.replace(".", "_"), value);
                    values.put(key, value);
                }
            }
            databags.put(cat.getName(), values);
        }
        return databags;
    }

    public List<ConstraintViolationBean> validate(AppConfigValidationResolver entityResolver) {
        return validate(null, entityResolver);
    }

    public List<ConstraintViolationBean> validate(List<ConstraintViolationBean> violations,
                                                  AppConfigValidationResolver entityResolver) {
        if (violations == null) violations = new ArrayList<>();
        for (AppConfigurationCategory cat : getCategories()) {
            for (String item : cat.getItems()) {
                final String catName = cat.getName();
                final String value = cat.get(item);

                final AppConfigMetadataDatabagField fieldMetadata = getFieldMetadata(cat, item);

                if (fieldMetadata == null) {
                    // assume field is required but otherwise do not validate
                    if (empty(value)) {
                        violations.add(AppConfigFieldValidatorBase.err(catName, item, "empty", value));
                    }
                } else {
                    fieldMetadata.getType().validate(catName, item, value, fieldMetadata, violations, entityResolver);
                }
            }
        }
        setViolations(violations);
        return violations;
    }

    public AppConfigMetadataDatabagField getFieldMetadata(AppConfigurationCategory cat, String item) {
        if (metadata == null) return null;
        final AppConfigMetadataDatabag databagMetadata = metadata.getCategories().get(cat.getName());
        if (databagMetadata == null) return null;
        return databagMetadata.get(item);
    }
}
