package cloudos.appstore.model;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.google.common.collect.Multimap;
import com.sun.jersey.api.core.HttpContext;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.cobbzilla.util.http.HttpMethods;
import org.cobbzilla.util.http.HttpRequestBean;
import org.cobbzilla.util.http.URIUtil;
import org.cobbzilla.util.string.StringUtil;
import org.cobbzilla.util.system.CommandShell;
import org.cobbzilla.util.xml.XPathUtil;
import org.cobbzilla.wizard.util.BufferedResponse;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.cobbzilla.util.string.StringUtil.urlEncode;

@Slf4j
public class ConfigurableAppRuntime extends AppRuntimeBase {

    public static final MustacheFactory mustacheFactory = new DefaultMustacheFactory();

    @Override
    public boolean isLoginPage(String document) {
        if (document == null) return false;
        return containsAllMarkers(document, authentication.getLogin_page_markers());
    }

    @Override
    public boolean isRegistrationPage(String document) {
        if (document == null
                || StringUtil.empty(authentication.getRegistration_path())
                || authentication.getRegistration_page_markers().isEmpty()) return false;
        return containsAllMarkers(document, authentication.getRegistration_page_markers());
    }

    protected boolean containsAllMarkers(String document, List<String> markers) {
        for (String marker : markers) {
            if (!document.contains(marker)) return false;
        }
        return true;
    }

    @Override
    public HttpRequestBean<String> buildLoginRequest(CloudOsAccount account,
                                                     BufferedResponse initialResponse,
                                                     HttpContext context,
                                                     String appPath) {

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

    protected static String parseMapping(CloudOsAccount account, String value, String appPath) {
        final Map<String, Object> scope = new HashMap<>();
        scope.put("account", account);
        scope.put("emailDomain", getEmailDomain(appPath));
        scope.put("email_domain", getEmailDomain(appPath));
        scope.put("timezone-offset", getTimezoneOffset());

        StringWriter w = new StringWriter();
        final Mustache mustache = getMustache(value);
        mustache.execute(w, scope);
        return w.toString();
    }

    private static Map<String, Mustache> mustacheCache = new ConcurrentHashMap<>();
    private static Mustache getMustache(String value) {
        Mustache m = mustacheCache.get(value);
        if (m == null) {
            m = mustacheFactory.compile(new StringReader(value), value);
            mustacheCache.put(value, m);
        }
        return m;
    }

    protected static Map<String, String> parsePassThruFields(Collection<String> passThruXPaths, BufferedResponse response) {
        try {
            return new XPathUtil(passThruXPaths, true).getFirstMatchMap(new ByteArrayInputStream(response.getDocument().getBytes()));
        } catch (Exception e) {
            log.error("parsePassThruFields: XPath not found in document? "+e, e);
            throw new IllegalStateException(e);
        }
    }

}
