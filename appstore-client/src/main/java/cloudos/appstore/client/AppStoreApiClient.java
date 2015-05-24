package cloudos.appstore.client;

import cloudos.appstore.model.*;
import cloudos.appstore.model.support.*;
import org.apache.http.client.HttpClient;
import org.cobbzilla.util.http.ApiConnectionInfo;
import org.cobbzilla.util.json.JsonUtil;
import org.cobbzilla.wizard.client.ApiClientBase;
import org.cobbzilla.wizard.dao.SearchResults;
import org.cobbzilla.wizard.model.ResultPage;
import org.cobbzilla.wizard.util.RestResponse;

import java.io.File;

import static cloudos.appstore.ApiConstants.*;
import static org.cobbzilla.util.json.JsonUtil.fromJson;
import static org.cobbzilla.util.json.JsonUtil.toJson;

public class AppStoreApiClient extends ApiClientBase {

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

    public ApiToken refreshToken () throws Exception {
        final RefreshTokenRequest request = new RefreshTokenRequest(new ApiToken(getToken()));
        final RestResponse restResponse = post(AUTH_ENDPOINT, toJson(request));
        return fromJson(restResponse.json, ApiToken.class);
    }

    public ApiToken refreshToken (String email, String password) throws Exception {
        final RefreshTokenRequest request = new RefreshTokenRequest(email, password);
        final RestResponse restResponse = post(AUTH_ENDPOINT, toJson(request));
        return fromJson(restResponse.json, ApiToken.class);
    }

    public boolean deleteToken (String token) throws Exception {
        return delete(AUTH_ENDPOINT+"/"+token).status == 200;
    }

    public AppStoreAccount findAccount () throws Exception {
        final RestResponse restResponse = get(ACCOUNTS_ENDPOINT);
        return fromJson(restResponse.json, AppStoreAccount.class);
    }

    public AppStoreAccount findAccount (String uuid) throws Exception {
        final RestResponse restResponse = get(ACCOUNTS_ENDPOINT+"/"+uuid);
        return fromJson(restResponse.json, AppStoreAccount.class);
    }

    public AppStorePublisher findPublisher(String uuid) throws Exception {
        final RestResponse restResponse = get(PUBLISHERS_ENDPOINT + "/" + uuid);
        return fromJson(restResponse.json, AppStorePublisher.class);
    }

    public void deleteAccount() throws Exception { delete(ACCOUNTS_ENDPOINT); }

    public CloudAppVersion defineApp(DefineCloudAppRequest request) throws Exception {
        final RestResponse restResponse = post(APPS_ENDPOINT, toJson(request));
        return fromJson(restResponse.json, CloudAppVersion.class);
    }

    public CloudApp findApp(String name) throws Exception {
        final RestResponse restResponse = get(APPS_ENDPOINT + "/" + name);
        return fromJson(restResponse.json, CloudApp.class);
    }

    public AppFootprint getAppFootprint(CloudApp app) throws Exception {
        final RestResponse restResponse = get(APPS_ENDPOINT + "/" + app.getUuid() + EP_FOOTPRINT);
        return fromJson(restResponse.json, AppFootprint.class);
    }

    public AppFootprint setAppFootprint(AppFootprint footprint) throws Exception {
        final RestResponse restResponse = post(APPS_ENDPOINT + "/" + footprint.getCloudApp() + EP_FOOTPRINT, toJson(footprint));
        return fromJson(restResponse.json, AppFootprint.class);
    }

    public AppPrice[] getAppPrices(CloudApp app) throws Exception {
        final RestResponse restResponse = get(APPS_ENDPOINT + "/" + app.getUuid() + EP_PRICES);
        return fromJson(restResponse.json, AppPrice[].class);
    }

    public AppPrice setAppPrice(AppPrice price) throws Exception {
        final RestResponse restResponse = post(APPS_ENDPOINT + "/" + price.getCloudApp() + EP_PRICES, toJson(price));
        return fromJson(restResponse.json, AppPrice.class);
    }

    public SearchResults<AppListing> searchAppStore(ResultPage page) throws Exception {
        final RestResponse restResponse = post(APPSTORE_ENDPOINT, toJson(page));
        return JsonUtil.PUBLIC_MAPPER.readValue(restResponse.json, AppListing.searchResultType);
    }

    public AppListing findPublishedApp(String uuid) throws Exception {
        final RestResponse restResponse = get(APPSTORE_ENDPOINT + "/" + uuid);
        return fromJson(restResponse.json, AppListing.class);
    }

    public CloudAppVersion updateAppStatus(String app, String version, CloudAppStatus status) throws Exception {
        final RestResponse restResponse = post(APPS_ENDPOINT + "/" + app + "/versions/" + version + "/status", toJson(status));
        return fromJson(restResponse.json, CloudAppVersion.class);
    }

    public CloudAppVersion findVersion(String app, String version) throws Exception {
        final RestResponse restResponse = get(APPS_ENDPOINT + "/" + app + "/versions/" + version);
        return fromJson(restResponse.json, CloudAppVersion.class);
    }

    public File getLatestAppBundle(String app) throws Exception {
        return getLatestAsset(app, "bundle");
    }
    public File getLatestAsset(String app, String asset) throws Exception {
        return getFile(APPS_ENDPOINT+"/"+app+"/assets/"+asset);
    }

    public File getAppBundle(String app, String version) throws Exception {
        return getAppAsset(app, version, "bundle");
    }
    public File getAppAsset(String app, String version, String asset) throws Exception {
        return getFile(APPS_ENDPOINT+"/"+app+"/versions/"+version+"/assets/"+asset);
    }
}
