package cloudos.appstore.model.app.filter;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum AppFilterType {

    field   (FieldFilterHandler.class),
    element (ElementFilterHandler.class),
    regex   (RegexFilterHandler.class),
    header  (HeaderFilterHandler.class),
    footer  (FooterFilterHandler.class),
    blocker (BlockerFilterHandler.class),
    submit  (SubmitFilterHandler.class),
    radio   (RadioGroupFilterHandler.class),
    plugin  (PluginFilterHandler.class);

    private Class<? extends AppFilterHandler> handlerClass;

    @JsonCreator public static AppFilterType fromString (String value) { return AppFilterType.valueOf(value.toLowerCase()); }

    public AppFilterHandler newHandler() {
        try { return handlerClass.newInstance(); } catch (Exception e) {
            throw new IllegalStateException("Error creating handler: "+e, e);
        }
    }
}
