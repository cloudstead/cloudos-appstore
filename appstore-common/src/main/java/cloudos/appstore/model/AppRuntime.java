package cloudos.appstore.model;

import cloudos.appstore.model.app.AppAuthConfig;
import com.sun.jersey.api.core.HttpContext;
import org.cobbzilla.util.http.HttpRequestBean;
import org.cobbzilla.wizard.util.BufferedResponse;

import java.util.Map;

public interface AppRuntime {

    public AppRuntimeDetails getDetails();
    public AppRuntime setDetails (AppRuntimeDetails details);

    public boolean isLoginPage(String document);
    public boolean isRegistrationPage(String document);
    public boolean isErrorPage(String document);

    public HttpRequestBean<String> buildLoginRequest(CloudOsAccount account,
                                                     BufferedResponse initialResponse,
                                                     HttpContext context,
                                                     String appPath);

    public HttpRequestBean<String> buildRegistrationRequest(CloudOsAccount account,
                                                            BufferedResponse initialResponse,
                                                            HttpContext context,
                                                            String appPath);

    public AppAuthConfig getAuthentication();
    public AppRuntime setAuthentication(AppAuthConfig authentication);
    public boolean hasAuthentication ();
    public boolean hasUserManagement ();

    public String applyCustomFilter (String filterName, String document, Map<String, Object> scope);

}
