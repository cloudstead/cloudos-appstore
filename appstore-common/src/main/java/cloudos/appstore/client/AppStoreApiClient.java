package cloudos.appstore.client;

import cloudos.appstore.model.*;
import cloudos.appstore.model.support.*;
import org.cobbzilla.wizard.model.ApiToken;
import org.apache.http.client.HttpClient;
import org.cobbzilla.util.http.ApiConnectionInfo;
import org.cobbzilla.util.http.HttpStatusCodes;
import org.cobbzilla.util.json.JsonUtil;
import org.cobbzilla.wizard.api.ForbiddenException;
import org.cobbzilla.wizard.client.ApiClientBase;
import org.cobbzilla.wizard.dao.SearchResults;
import org.cobbzilla.wizard.util.RestResponse;

import java.io.File;

import static cloudos.appstore.ApiConstants.*;
import static org.cobbzilla.util.daemon.ZillaRuntime.die;
import static org.cobbzilla.util.daemon.ZillaRuntime.empty;
import static org.cobbzilla.util.json.JsonUtil.fromJson;
import static org.cobbzilla.util.json.JsonUtil.toJson;

public class AppStoreApiClient extends ApiClientBase {

    public static final String PUBLIC_APPS = "public";

    public AppStoreApiClient(ApiConnectionInfo connectionInfo) { super(connectionInfo); }
    public AppStoreApiClient(String baseUri) { super(baseUri); }

    public AppStoreApiClient(ApiConnectionInfo connectionInfo, HttpClient httpClient) {
        super(connectionInfo, httpClient);
    }

    public AppStoreApiClient(String baseUri, HttpClient httpClient) { super(baseUri, httpClient); }

    @Override protected String getTokenHeader() { return H_TOKEN; }

    public ApiToken registerAccount (AppStoreAccountRegistration registration) throws Exception {
        final RestResponse restResponse = put(AUTH_ENDPOINT, toJson(registration));
        final ApiToken token = fromJson(restResponse.json, ApiToken.class);
        setToken(token.getToken());
        return token;
    }

    public ApiToken login () throws Exception {
        return refreshToken(connectionInfo.getUser(), connectionInfo.getPassword());
    }

    public ApiToken refreshToken () throws Exception {
        return refreshToken(new RefreshTokenRequest(new ApiToken(getToken())));
    }

    public ApiToken refreshToken (String email, String password) throws Exception {
        return refreshToken(new RefreshTokenRequest(email, password));
    }

    public ApiToken refreshToken(RefreshTokenRequest request) throws Exception {
        final RestResponse restResponse = post(AUTH_ENDPOINT, toJson(request));
        final ApiToken apiToken = fromJson(restResponse.json, ApiToken.class);
        setToken(apiToken.getToken());
        return apiToken;
    }

    public boolean deleteToken (String token) throws Exception {
        return delete(AUTH_ENDPOINT+"/"+token).status == 200;
    }

    public AppStoreAccount findAccount () throws Exception {
        final RestResponse restResponse = get(ACCOUNTS_ENDPOINT);
        return fromJson(restResponse.json, AppStoreAccount.class);
    }

    public AppStoreAccount findAccount (String uuid) throws Exception {
        final RestResponse restResponse = doGet(ACCOUNTS_ENDPOINT + "/" + uuid);
        return objectOrNull(restResponse, AppStoreAccount.class);
    }

    public AppStorePublisher findPublisher(String name) throws Exception {
        final RestResponse restResponse = doGet(PUBLISHERS_ENDPOINT + "/" + name);
        return objectOrNull(restResponse, AppStorePublisher.class);
    }

    public void deleteAccount() throws Exception { delete(ACCOUNTS_ENDPOINT); }

    public CloudAppVersion defineApp(String publisherName, DefineCloudAppRequest request) throws Exception {
        final RestResponse restResponse = post(APPS_ENDPOINT+"/"+publisherName, toJson(request));
        return fromJson(restResponse.json, CloudAppVersion.class);
    }

    public CloudApp findApp(String publisherName, String name) throws Exception {
        if (empty(publisherName)) publisherName = PUBLIC_APPS;
        final RestResponse restResponse = doGet(APPS_ENDPOINT + "/" + publisherName + "/" + name);
        return objectOrNull(restResponse, CloudApp.class);
    }

    public void deleteVersion(String pubName, String appName, String version) throws Exception {
        delete(APPS_ENDPOINT + "/" + pubName + "/" + appName + "/versions/" + version);
    }

    public void deleteApp(String pubName, String appName) throws Exception {
        delete(APPS_ENDPOINT + "/" + pubName + "/" + appName);
    }

    public AppFootprint getAppFootprint(String publisher, CloudApp app) throws Exception {
        if (empty(publisher)) publisher = PUBLIC_APPS;
        final RestResponse restResponse = doGet(APPS_ENDPOINT + "/" + publisher + "/" + app.getUuid() + EP_FOOTPRINT);
        return objectOrNull(restResponse, AppFootprint.class);
    }

    public AppFootprint setAppFootprint(String publisher, AppFootprint footprint) throws Exception {
        final RestResponse restResponse = post(APPS_ENDPOINT + "/" + publisher + "/" + footprint.getCloudApp() + EP_FOOTPRINT, toJson(footprint));
        return fromJson(restResponse.json, AppFootprint.class);
    }

    public AppPrice[] getAppPrices(String publisher, CloudApp app) throws Exception {
        if (empty(publisher)) publisher = PUBLIC_APPS;
        final RestResponse restResponse = doGet(APPS_ENDPOINT + "/" + publisher + "/" + app.getName() + EP_PRICES);
        return objectOrNull(restResponse, AppPrice[].class);
    }

    public AppPrice setAppPrice(String publisher, AppPrice price) throws Exception {
        final RestResponse restResponse = post(APPS_ENDPOINT + "/" + publisher + "/" + price.getCloudApp() + EP_PRICES, toJson(price));
        return fromJson(restResponse.json, AppPrice.class);
    }

    public SearchResults<AppListing> searchAppStore(AppStoreQuery query) throws Exception {
        final RestResponse restResponse = post(APPSTORE_ENDPOINT, toJson(query));
        return JsonUtil.PUBLIC_MAPPER.readValue(restResponse.json, AppListing.searchResultType);
    }

    public AppListing findAppListing(String publisher, String name) throws Exception {
        if (empty(publisher)) publisher = PUBLIC_APPS;
        final RestResponse restResponse = doGet(APPSTORE_ENDPOINT + "/" + publisher + "/" + name);
        return objectOrNull(restResponse, AppListing.class);
    }

    public AppListing findAppListing(String publisher, String name, String version) throws Exception {
        if (empty(publisher)) publisher = PUBLIC_APPS;
        final RestResponse restResponse = doGet(APPSTORE_ENDPOINT + "/" + publisher + "/" + name + "/" + version);
        return objectOrNull(restResponse, AppListing.class);
    }

    public CloudAppVersion updateAppStatus(String publisher, String app, String version, CloudAppStatus status) throws Exception {
        final RestResponse restResponse = post(APPS_ENDPOINT + "/" + publisher + "/" + app + "/versions/" + version + "/status", toJson(status));
        return fromJson(restResponse.json, CloudAppVersion.class);
    }

    public CloudAppVersion updateAppAttribute(String publisher, String app, String attribute, String value) throws Exception {
        final RestResponse restResponse = post(APPS_ENDPOINT + "/" + publisher + "/" + app + "/attributes/" + attribute, toJson(value));
        return fromJson(restResponse.json, CloudAppVersion.class);
    }

    public CloudAppVersion findVersion(String publisher, String app, String version) throws Exception {
        if (empty(publisher)) publisher = PUBLIC_APPS;
        final RestResponse restResponse = doGet(APPS_ENDPOINT + "/" + publisher + "/" + app + "/versions/" + version);
        return objectOrNull(restResponse, CloudAppVersion.class);
    }

    public File getLatestAppBundle(String publisher, String app) throws Exception {
        return getLatestAsset(publisher, app, "bundle");
    }
    public File getLatestAsset(String publisher, String app, String asset) throws Exception {
        if (empty(publisher)) publisher = PUBLIC_APPS;
        return assetFile(APPS_ENDPOINT + "/" + publisher + "/" + app + "/assets/" + asset);
    }

    public File getAppBundle(String publisher, String app, String version) throws Exception {
        return getAppAsset(publisher, app, version, "bundle");
    }
    public File getAppAsset(String publisher, String app, String version, String asset) throws Exception {
        if (empty(publisher)) publisher = PUBLIC_APPS;
        return assetFile(APPS_ENDPOINT + "/" + publisher + "/" + app + "/versions/" + version + "/assets/" + asset);
    }

    private File assetFile(String url) throws Exception {
        try {
            return getFile(url);
        } catch (ForbiddenException e) {
            login();
            return getFile(url);
        }
    }

    protected String getTempFileSuffix(String path, String contentType) {
        if (empty(path)) return ".temp";
        if (path.endsWith("/bundle")) return ".tar.gz";
        return super.getTempFileSuffix(path, contentType);
    }

    protected <T> T objectOrNull(RestResponse restResponse, Class<T> clazz) throws Exception {
        if (restResponse.status == HttpStatusCodes.NOT_FOUND) return null;
        if (restResponse.isSuccess()) {
            return fromJson(restResponse.json, clazz);
        } else {
            return die("findApp: "+restResponse);
        }
    }

}
