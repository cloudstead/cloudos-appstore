package cloudos.appstore.model.app.filter;

import java.util.List;
import java.util.Map;

public abstract class CompoundFilterHandler extends AppFilterHandlerBase {

    protected abstract List<AppFilterHandler> getHandlers(String document, Map<String, Object> scope);

    @Override
    public String apply(String document, Map<String, Object> scope) {
        for (AppFilterHandler handler : getHandlers(document, scope)) {
            document = handler.apply(document, scope);
        }
        return document;
    }

}
