package cloudos.appstore.model;

import com.sun.jersey.api.core.HttpContext;
import org.cobbzilla.util.http.HttpRequestBean;
import org.cobbzilla.wizard.util.BufferedResponse;

import static org.cobbzilla.util.daemon.ZillaRuntime.die;

public class SystemAppRuntime extends AppRuntimeBase {

    @Override public boolean isLoginPage(String document) { return false; }
    @Override public boolean isErrorPage(String document) { return false; }

    @Override public HttpRequestBean<String> buildLoginRequest(CloudOsAccount account, BufferedResponse initialResponse, HttpContext context, String appPath) {
        return die("buildLoginRequest: not supported for "+getDetails());
    }
}
