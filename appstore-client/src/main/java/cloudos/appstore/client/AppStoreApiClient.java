package cloudos.appstore.client;

import org.apache.http.client.HttpClient;
import cloudos.appstore.model.*;
import cloudos.appstore.model.support.ApiToken;
import cloudos.appstore.model.support.AppListing;
import cloudos.appstore.model.support.AppStoreAccountRegistration;
import org.cobbzilla.util.json.JsonUtil;
import org.cobbzilla.wizard.client.ApiClientBase;
import org.cobbzilla.util.http.ApiConnectionInfo;
import org.cobbzilla.wizard.dao.SearchResults;
import org.cobbzilla.wizard.model.ResultPage;
import org.cobbzilla.wizard.util.RestResponse;

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
        final ApiToken token = new ApiToken(getToken());
        final RestResponse restResponse = post(AUTH_ENDPOINT, toJson(token));
        return fromJson(restResponse.json, ApiToken.class);
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

    public CloudApp defineApp(CloudApp app) throws Exception {
        final RestResponse restResponse = put(APPS_ENDPOINT, toJson(app));
        return fromJson(restResponse.json, CloudApp.class);
    }

    public CloudApp findApp(String uuid) throws Exception {
        final RestResponse restResponse = get(APPS_ENDPOINT + "/" + uuid);
        return fromJson(restResponse.json, CloudApp.class);
    }

    public CloudApp updateApp(CloudApp app) throws Exception {
        final RestResponse restResponse = post(APPS_ENDPOINT + "/" + app.getUuid(), toJson(app));
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

    public CloudAppVersion defineAppVersion(CloudAppVersion version) throws Exception {
        final RestResponse restResponse = put(APP_VERSIONS_ENDPOINT, toJson(version));
        return fromJson(restResponse.json, CloudAppVersion.class);
    }

    public CloudAppVersion updateAppVersion(CloudAppVersion version) throws Exception {
        final RestResponse restResponse = post(APP_VERSIONS_ENDPOINT + "/" + version.getUuid(), toJson(version));
        return fromJson(restResponse.json, CloudAppVersion.class);
    }

    public CloudAppVersion findAppVersion(String uuid) throws Exception {
        final RestResponse restResponse = get(APP_VERSIONS_ENDPOINT + "/" + uuid);
        return fromJson(restResponse.json, CloudAppVersion.class);
    }

    public SearchResults<AppListing> searchAppStore(ResultPage page) throws Exception {
        final RestResponse restResponse = post(APPSTORE_ENDPOINT, toJson(page));
        return JsonUtil.PUBLIC_MAPPER.readValue(restResponse.json, AppListing.searchResultType);
    }

    public AppListing findPublishedApp(String uuid) throws Exception {
        final RestResponse restResponse = get(APPSTORE_ENDPOINT + "/" + uuid);
        return fromJson(restResponse.json, AppListing.class);
    }

}
