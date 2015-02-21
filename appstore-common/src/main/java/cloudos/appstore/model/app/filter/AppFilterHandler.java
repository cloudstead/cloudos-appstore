package cloudos.appstore.model.app.filter;

import java.util.Map;

public interface AppFilterHandler {

    public AppFilterHandler configure(Map<String, String> config);

    public String apply(String document, Map<String, Object> scope);

}
