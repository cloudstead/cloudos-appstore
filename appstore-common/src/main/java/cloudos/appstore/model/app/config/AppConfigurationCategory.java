package cloudos.appstore.model.app.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rooty.toots.vendor.VendorSettingHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.cobbzilla.util.daemon.ZillaRuntime.empty;

/**
 * Corresponds to a single databag in the app manifest.
 */
@NoArgsConstructor
public class AppConfigurationCategory {

    @Getter @Setter private String name;
    @Getter @Setter private List<String> items = new ArrayList<>();
    @Getter @Setter private Map<String, String> values = new HashMap<>();

    public AppConfigurationCategory(String name) { this.name = name; }

    public boolean hasItem(String item) { return items.contains(item); }

    public boolean hasValue(String item) {
        final String val = values.get(item);
        return !empty(val) && !val.equals(VendorSettingHandler.VALUE_NOT_SET);
    }

    public void add (String item) { if (!hasItem(item)) items.add(item); }

    public void set (String item, String value) {
        if (!items.contains(item)) throw new IllegalArgumentException("Invalid config item: "+item);
        values.put(item, value);
    }

    public String get (String item) { return values.get(item); }

    public boolean hasValues() { return !values.isEmpty(); }

}
