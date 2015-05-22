package cloudos.appstore.model.app.filter;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.cobbzilla.util.daemon.ZillaRuntime.empty;

public class RadioGroupFilterHandler extends CompoundFilterHandler {

    @Getter @Setter private String checked;
    @Getter @Setter private String unchecked;
    @Getter @Setter private String swap_if;

    @Override
    protected List<AppFilterHandler> getHandlers(String document, Map<String, Object> scope) {
        final List<AppFilterHandler> handlers = new ArrayList<>();
        if (!empty(checked)) {
            for (String id : checked.split("[, \n]+]")) {
                handlers.add(shouldSwap(scope) ? new UncheckedFilterHandler(id) : new CheckedFilterHandler(id));
            }
        }
        if (!empty(unchecked)) {
            for (String id : unchecked.split("[, \n]+]")) {
                handlers.add(shouldSwap(scope) ? new CheckedFilterHandler(id) : new UncheckedFilterHandler(id));
            }
        }
        return handlers;
    }

    private boolean shouldSwap(Map<String, Object> scope) {
        if (empty(swap_if)) return false;
        final String swap = render(swap_if, scope);
        return Boolean.valueOf(swap);
    }

    @AllArgsConstructor
    private abstract class CheckedFilterHandlerBase extends ElementFilterHandler {
        protected String id;
        @Override public String getElement() { return "input"; }
        @Override public String getMatch_attr_name() { return "id"; }
        @Override public String getMatch_attr_value() { return id; }
        @Override public String getReplace_attr() { return "checked"; }
    }

    private class CheckedFilterHandler extends CheckedFilterHandlerBase {
        public CheckedFilterHandler(String id) { super(id); }
        @Override public String getValue() { return "checked"; }
    }

    private class UncheckedFilterHandler extends CheckedFilterHandlerBase {
        public UncheckedFilterHandler(String id) { super(id); }
        @Override public String getValue() { return ""; }
    }
}
