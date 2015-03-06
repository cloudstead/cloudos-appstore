package cloudos.appstore.model.app.filter;

import cloudos.appstore.model.AppRuntime;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

import static org.cobbzilla.util.daemon.ZillaRuntime.die;

public class PluginFilterHandler extends AppFilterHandlerBase {

    @Getter @Setter private String name;

    @Override public String apply(String document, Map<String, Object> scope) {
        final AppRuntime runtime = (AppRuntime) scope.get(FSCOPE_RUNTIME);
        if (runtime == null) die("No " + FSCOPE_RUNTIME + " found in scope: " + scope);
        return runtime.applyCustomFilter(name, document, scope);
    }

}
