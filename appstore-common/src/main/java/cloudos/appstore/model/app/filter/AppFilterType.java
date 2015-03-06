package cloudos.appstore.model.app.filter;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;

import static org.cobbzilla.util.daemon.ZillaRuntime.die;

@AllArgsConstructor
public enum AppFilterType {

    field   (FieldFilterHandler.class),
    element (ElementFilterHandler.class),
    regex   (RegexFilterHandler.class),
    header  (HeaderFilterHandler.class),
    footer  (FooterFilterHandler.class),
    overlay (OverlayFilterHandler.class),
    submit  (SubmitFilterHandler.class),
    radio   (RadioGroupFilterHandler.class),
    plugin  (PluginFilterHandler.class);

    private Class<? extends AppFilterHandler> handlerClass;

    @JsonCreator public static AppFilterType create(String value) { return AppFilterType.valueOf(value.toLowerCase()); }

    public AppFilterHandler newHandler() {
        try { return handlerClass.newInstance(); } catch (Exception e) {
            return (AppFilterHandler) die("Error creating handler: " + e, e);
        }
    }
}
