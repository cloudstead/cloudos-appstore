package cloudos.appstore.test;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.cobbzilla.wizard.model.SemanticVersion;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.cobbzilla.util.daemon.ZillaRuntime.die;
import static org.cobbzilla.util.daemon.ZillaRuntime.empty;
import static org.cobbzilla.util.json.JsonUtil.toJson;

@AllArgsConstructor
public class MockAppStoreHttpClient implements HttpClient {

    @Getter private MockAppStoreApiClient apiClient;

    @Override public HttpParams getParams() {
        return die("unsupported");
    }

    @Override public ClientConnectionManager getConnectionManager() {
        return die("unsupported");
    }

    @Override public HttpResponse execute(HttpUriRequest request) throws IOException, ClientProtocolException {
        return execute(request, null, null);
    }

    @Override
    public HttpResponse execute(HttpUriRequest request, HttpContext context) throws IOException, ClientProtocolException {
        return execute(request, null, context);
    }

    @Override
    public HttpResponse execute(HttpHost target, HttpRequest request) throws IOException, ClientProtocolException {
        return execute(target, request, null, null);
    }

    @Override
    public HttpResponse execute(HttpHost target, HttpRequest request, HttpContext context) throws IOException, ClientProtocolException {
        return execute(target, request, null, context);
    }

    @Override
    public <T> T execute(HttpUriRequest request, ResponseHandler<? extends T> responseHandler) throws IOException, ClientProtocolException {
        return execute(null, request, null, null);
    }

    @Override
    public <T> T execute(HttpUriRequest request, ResponseHandler<? extends T> responseHandler, HttpContext context) throws IOException, ClientProtocolException {
        return execute(null, request, responseHandler, context);
    }

    @Override
    public <T> T execute(HttpHost target, HttpRequest request, ResponseHandler<? extends T> responseHandler) throws IOException, ClientProtocolException {
        return execute(target, request, responseHandler, null);
    }

    public static final Pattern uriPattern = Pattern.compile("/appstore/apps/([-_\\w]+)/([-_\\w]+)(/("+SemanticVersion.SEMANTIC_VERSION_RE+"))?");

    @Override
    public <T> T execute(HttpHost target, HttpRequest request, ResponseHandler<? extends T> responseHandler, HttpContext context) throws IOException, ClientProtocolException {

        final String json;
        final String path = request.getRequestLine().getUri();
        final Matcher matcher = uriPattern.matcher(path);

        if (matcher.find()) {
            final String publisher = matcher.group(1).toLowerCase();
            final String app = matcher.group(2).toLowerCase();
            if (!empty(matcher.group(4))) {
                final String version = matcher.group(4);
                json = findAppVersion(publisher, app, version);
            } else {
                json = findApp(publisher, app);
            }
        } else {
            return die("invalid path: "+path);
        }

        final BasicHttpResponse response = new BasicHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, 200, "OK"));
        response.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
        response.setHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(json.length()));
        response.setEntity(new StringEntity(json));
        return (T) response;
    }

    protected String findApp(String publisher, String app) {
        try {
            return toJson(apiClient.findApp(publisher, app));
        } catch (Exception e) {
            return die("findApp: "+e, e);
        }
    }

    protected String findAppVersion(String publisher, String app, String version) {
        try {
            return toJson(apiClient.findVersion(publisher, app, version));
        } catch (Exception e) {
            return die("findAppVersion: "+e, e);
        }
    }
}
