package cloudos.appstore.model.app.filter;

import java.util.Map;

public interface AppFilterHandler {

    public static final String FSCOPE_CONFIG = "config";
    public static final String FSCOPE_RUNTIME = "runtime";
    public static final String FSCOPE_APP = "app";
    public static final String FSCOPE_APP_URI = "app_uri";
    public static final String FSCOPE_APP_CONFIG = "app_config";
    public static final String FSCOPE_CONTEXT = "context";
    public static final String FSCOPE_COOKIE_JAR = "cookieJar";
    public static final String FSCOPE_METHOD = "method";

    public AppFilterHandler configure(Map<String, String> config);

    public String apply(String document, Map<String, Object> scope);

}
