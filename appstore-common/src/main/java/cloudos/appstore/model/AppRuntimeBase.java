package cloudos.appstore.model;

import cloudos.appstore.model.app.AppAuthConfig;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.sun.jersey.api.core.HttpContext;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.cobbzilla.util.http.HttpRequestBean;
import org.cobbzilla.wizard.util.BufferedResponse;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import static javax.ws.rs.core.HttpHeaders.*;

@NoArgsConstructor @AllArgsConstructor @Accessors(chain=true)
public abstract class AppRuntimeBase implements AppRuntime {

    public static final String PROXY_USER_AGENT = "CloudOsProxy/1.0";
    public static final String REFERER = "Referer";

    @Getter @Setter protected AppRuntimeDetails details;

    @Getter @Setter protected AppAuthConfig authentication;
    public boolean hasAuthentication() { return authentication != null; }
    public boolean hasUserManagement() { return hasAuthentication() && authentication.hasUser_management(); }

    @Override public boolean isRegistrationPage(String document) { return false; }

    @Override public HttpRequestBean<String> buildRegistrationRequest(CloudOsAccount account,
                                                                      BufferedResponse initialResponse,
                                                                      HttpContext context,
                                                                      String appPath) {
        throw new UnsupportedOperationException("buildRegistrationRequest: not supported for "+getClass().getName());
    }

    @Override public String applyCustomFilter(String filterName, String document, Map<String, Object> scope) {
        throw new UnsupportedOperationException("applyCustomFilter: not supported for "+getClass().getName());
    }

    protected static String getEmailDomain(String baseUri) {
        final String baseHost;
        try {
            final String host = new URI(baseUri).getHost();
            baseHost = host.substring(host.indexOf('.')+1);

        } catch (URISyntaxException e) {
            throw new IllegalStateException("Invalid baseUri: "+baseUri);
        }
        return baseHost;
    }

    protected static Multimap<String, String> populateHeaders(BufferedResponse initialResponse) {
        final Multimap<String, String> headers = ArrayListMultimap.create();
        headers.put(ACCEPT, "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        headers.put(ACCEPT_LANGUAGE, "en-US,en;q=0.5");

        final String referer = initialResponse.getFirstHeaderValue(REFERER);
        if (referer != null) headers.put(REFERER, referer);

        headers.put(USER_AGENT, "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.9; rv:28.0) Gecko/20100101 Firefox/28.0 "+PROXY_USER_AGENT);
        headers.put(CONTENT_TYPE, "application/x-www-form-urlencoded");
        return headers;
    }

}
