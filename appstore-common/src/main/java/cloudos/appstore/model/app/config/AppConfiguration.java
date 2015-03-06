package cloudos.appstore.model.app.config;

import cloudos.appstore.model.app.AppDatabagDef;
import cloudos.appstore.model.app.AppLayout;
import cloudos.appstore.model.app.AppManifest;
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
import org.cobbzilla.wizard.validation.ConstraintViolationBean;
import rooty.toots.vendor.VendorDatabag;
import rooty.toots.vendor.VendorSettingHandler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.cobbzilla.util.json.JsonUtil.FULL_MAPPER;
import static org.cobbzilla.util.string.StringUtil.empty;

/**
 * Represents the configuration for a given app+version
 */
@Slf4j
public class AppConfiguration {

    @Getter @Setter private List<AppConfigurationCategory> categories = new ArrayList<>();
    public void add(AppConfigurationCategory category) { categories.add(category); }

    @Getter @Setter private AppConfigMetadata metadata;
    @Getter @Setter private AppConfigTranslations translations;

    public static AppConfiguration fromLayout(AppLayout layout, String locale) {
        if (!layout.exists()) return null;
        final AppManifest manifest = AppManifest.load(layout.getManifest());
        final File databagsDir = layout.getDatabagsDir();
        return getAppConfiguration(manifest, databagsDir, locale);
    }

    public static AppConfiguration getAppConfiguration(AppManifest manifest, File databagsDir, String locale) {
        final AppConfiguration config = new AppConfiguration();
        if (manifest.hasDatabags()) {
            databagsDir = new File(databagsDir, manifest.getName());
            for (AppDatabagDef databag : manifest.getDatabags()) {

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
                                category.set(item, value);
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
        config.setTranslations(AppConfigTranslations.load(databagsDir, locale));

        return config;
    }

    public static void setAppConfiguration(AppManifest manifest, File databagsDir, AppConfiguration config) {
        if (manifest.hasDatabags()) {
            databagsDir = new File(databagsDir, manifest.getName());
            for (AppDatabagDef databag : manifest.getDatabags()) {

                final String databagName = databag.getName();

                final List<JsonEditOperation> operations = new ArrayList<>();
                operations.add(new JsonEditOperation()
                        .setType(JsonEditOperationType.write)
                        .setPath("id")
                        .setJson("\"" + databagName + "\""));

                // Did the caller provide config for this category?
                final AppConfigurationCategory category = config.getCategory(databagName);
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
                            throw new IllegalStateException("Error preparing to write " + databagName + "/" + item + ": " + e);
                        }
                    }
                }

                final String updatedJson;
                try {
                    updatedJson = new JsonEdit().setJsonData(node).setOperations(operations).edit();
                    FileUtil.toFile(databagFile, updatedJson);

                } catch (Exception e) {
                    throw new IllegalStateException("Error generating updated json: " + e);
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

    public AppConfigurationCategory getCategory(String name) {
        for (AppConfigurationCategory cat : categories) {
            if (cat.getName().equals(name)) return cat;
        }
        return null;
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

    public List<ConstraintViolationBean> validate(List<ConstraintViolationBean> violations,
                                                  AppConfigValidationResolver entityResolver) {
        for (AppConfigurationCategory cat : getCategories()) {
            for (String item : cat.getItems()) {
                final String catName = cat.getName();
                final String value = cat.get(item);

                final AppConfigMetadataDatabagField fieldMetadata = getFieldMetadata(cat, item);

                if (fieldMetadata == null) {
                    // assume field is required but otherwise do not validate
                    if (empty(value)) {
                        violations.add(err(catName, item, "empty", value));
                    }
                } else {
                    validateField(catName, item, value, fieldMetadata, violations, entityResolver);
                }
            }
        }
        return violations;
    }

    private ConstraintViolationBean err(String catName, String item, String problem, String value) {
        return new ConstraintViolationBean("{err." + catName + "." + item + "." + problem + "}", null, value);
    }

    public static final Pattern LOGIN_PATTERN = Pattern.compile("[\\w\\-]+");

    private void validateField(String catName, String item, String value,
                               AppConfigMetadataDatabagField fieldMetadata,
                               List<ConstraintViolationBean> violations,
                               AppConfigValidationResolver entityResolver) {

        // quick check -- if value is empty and field is not required, we're done
        final boolean required = fieldMetadata.isRequired();
        if (!required && empty(value)) return;

        if (required && empty(value)) {
            violations.add(err(catName, item, "empty", value));
            return;
        }

        if (fieldMetadata.hasMin() && value.length() < fieldMetadata.getMin()) {
            violations.add(err(catName, item, "tooShort", value));

        } else if (fieldMetadata.hasMax() && value.length() > fieldMetadata.getMax()) {
            violations.add(err(catName, item, "tooLong", value));
        }

        switch (fieldMetadata.getType()) {
            case field:
            case password:
                // no additional validation to do here...
                break;

            case login:
                if (!LOGIN_PATTERN.matcher(value).matches()) {
                    violations.add(err(catName, item, "invalid", value));
                }
                break;

            case yesno:
                if (!(value.equals("true") || value.equals("false"))) {
                    violations.add(err(catName, item, "invalid", value));
                }
                break;

            case cloudos_group:
                if (!entityResolver.isValidGroup(value)) {
                    violations.add(err(catName, item, "invalid", value));
                }
                break;

            case member_list:
                for (String member : value.split("[,\\s]+")) {
                    if (!entityResolver.isValidAccount(value)) {
                        violations.add(err(catName, item, "invalid", value));
                    }
                }
                break;

            default:
                throw new IllegalStateException("Invalid field type: "+fieldMetadata.getType());
        }
    }

    public AppConfigMetadataDatabagField getFieldMetadata(AppConfigurationCategory cat, String item) {
        if (metadata == null) return null;
        final AppConfigMetadataDatabag databagMetadata = metadata.getMetadataMap().get(cat.getName());
        if (databagMetadata == null) return null;
        return databagMetadata.get(item);
    }
}
