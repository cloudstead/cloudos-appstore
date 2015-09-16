package cloudos.appstore.model;

import com.google.common.collect.Multimap;
import com.sun.jersey.api.core.HttpContext;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.cobbzilla.util.http.HttpMethods;
import org.cobbzilla.util.http.HttpRequestBean;
import org.cobbzilla.util.http.URIUtil;
import org.cobbzilla.util.io.FileUtil;
import org.cobbzilla.util.mustache.MustacheUtil;
import org.cobbzilla.util.system.CommandShell;
import org.cobbzilla.util.xml.XPathUtil;
import org.cobbzilla.wizard.util.BufferedResponse;

import java.io.ByteArrayInputStream;
import java.util.*;

import static org.cobbzilla.util.daemon.ZillaRuntime.die;
import static org.cobbzilla.util.daemon.ZillaRuntime.empty;
import static org.cobbzilla.util.string.StringUtil.urlEncode;

@Slf4j
public class ConfigurableAppRuntime extends AppRuntimeBase {

    @Override public boolean isLoginPage(String document) {
        if (document == null) return false;
        return containsAllMarkers(document, authentication.getLogin_page_markers());
    }

    @Override
    public boolean isRegistrationPage(String document) {
        if (document == null
                || empty(authentication.getRegistration_path())
                || authentication.getRegistration_page_markers().isEmpty()) return false;
        return containsAllMarkers(document, authentication.getRegistration_page_markers());
    }

    @Override public boolean isErrorPage(String document) {
        if (document == null) return true;
        return containsAnyMarkers(document, authentication.getError_page_markers());
    }

    protected boolean containsAllMarkers(String document, List<String> markers) {
        for (String marker : markers) {
            if (!document.contains(marker)) return false;
        }
        return true;
    }

    protected boolean containsAnyMarkers(String document, List<String> markers) {
        for (String marker : markers) {
            if (document.contains(marker)) return true;
        }
        return false;
    }

    @Override
    public HttpRequestBean<String> buildLoginRequest(CloudOsAccount account,
                                                     BufferedResponse initialResponse,
                                                     HttpContext context,
                                                     String appPath) {
        if (authentication == null) {
            log.warn("buildLoginRequest: authentication was null");
            return null;
        }
        return buildRequest(account, initialResponse, appPath, authentication.getLogin_fields(), authentication.getLogin_path());
    }

    @Override
    public HttpRequestBean<String> buildRegistrationRequest(CloudOsAccount account,
                                                            BufferedResponse initialResponse,
                                                            HttpContext context,
                                                            String appPath) {
        String path = authentication.getRegistration_path();
        if (path != null && path.equals("self")) path = URIUtil.getPath(initialResponse.getRequestUri());

        return buildRequest(account, initialResponse, appPath, authentication.getRegistration_fields(), path);
    }

    public HttpRequestBean<String> buildRequest(CloudOsAccount account, BufferedResponse initialResponse,
                                                String appPath,
                                                Map<String, String> fieldMappings, String submitPath) {

        final StringBuilder auth = new StringBuilder();
        final Map<String, String> passThruXpaths = new LinkedHashMap<>();

        for (Map.Entry<String, String> mapping : fieldMappings.entrySet()) {
            final String field = mapping.getKey();

            if (mapping.getValue().equals("pass")) {
                passThruXpaths.put("//input[@name='" + field + "']/@value", field);

            } else {
                if (auth.length() > 0) auth.append("&");
                final String value = parseMapping(account, mapping.getValue(), appPath);
                auth.append(urlEncode(field)).append("=").append(urlEncode(value));
            }
        }

        if (!passThruXpaths.isEmpty()) {
            final Map<String, String> params = parsePassThruFields(passThruXpaths.keySet(), initialResponse);
            for (String key : params.keySet()) {
                if (auth.length() > 0) auth.append("&");
                final String value = params.get(key);
                if (value != null) auth.append(urlEncode(passThruXpaths.get(key))).append("=").append(urlEncode(value));
            }
        }

        final Multimap<String, String> headers = populateHeaders(initialResponse);

        final String uri = appPath + submitPath;
        return new HttpRequestBean<>(HttpMethods.POST, uri, auth.toString(), headers);
    }

    @Getter(lazy=true) private static final String timezoneOffset = initTimezoneOffset();
    private static String initTimezoneOffset() { return CommandShell.execScript(
            "offset=$(date +%z); echo \"$(expr ${offset} / 100)" +
                    "$(if [ $(expr ${offset} % 100) != 0 ] ; then " +
                      "echo \\\".$(expr ${offset} % 100)\\\" ; " +
                    "fi)\""
    ).trim();
    }

    @Getter(lazy=true) private static final String timezoneName = initTimezoneName();
    private static String initTimezoneName() { return FileUtil.toStringOrDie("/etc/timezone").trim(); }

    protected static String parseMapping(CloudOsAccount account, String value, String appPath) {
        final Map<String, Object> scope = new HashMap<>();
        scope.put("account", account);
        scope.put("emailDomain", getEmailDomain(appPath));
        scope.put("email_domain", getEmailDomain(appPath));
        scope.put("timezone-offset", getTimezoneOffset());
        scope.put("timezone-name", getTimezoneName());

        return MustacheUtil.render(value, scope);
    }

    protected static Map<String, String> parsePassThruFields(Collection<String> passThruXPaths, BufferedResponse response) {
        try {
            return new XPathUtil(passThruXPaths).getFirstMatchMap(new ByteArrayInputStream(response.getDocument().getBytes()));
        } catch (Exception e) {
            log.error("parsePassThruFields: XPath not found in document? "+e, e);
            return (Map<String, String>) die(e);
        }
    }

}
