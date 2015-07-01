package cloudos.appstore.model.app.config;

import lombok.*;
import lombok.experimental.Accessors;
import org.cobbzilla.util.collection.ArrayUtil;

import static org.cobbzilla.util.daemon.ZillaRuntime.empty;

@NoArgsConstructor @AllArgsConstructor
@Accessors(chain=true) @ToString
public class AppConfigTranslation {

    @Getter @Setter private String label;
    @Getter @Setter private String info;
    @Getter @Setter private String help;
    @Getter @Setter private AppConfigTranslation[] choices;

    public AppConfigTranslation(String label) {
        this.label = label;
    }

    public AppConfigTranslation(String label, String info) {
        this(label);
        this.info = info;
    }

    public boolean hasChoice(String choice) {
        if (empty(choices)) return false;
        for (AppConfigTranslation t : choices) {
            if (t.getLabel().equals(choice)) return true;
        }
        return false;
    }

    public void addChoice(AppConfigTranslation translation) {
        choices = ArrayUtil.append(choices, translation);
    }
}
