package cloudos.appstore.test;

import cloudos.appstore.client.AppStoreApiClient;
import cloudos.appstore.model.*;
import cloudos.appstore.model.support.ApiToken;
import cloudos.appstore.model.support.AppListing;
import cloudos.appstore.model.support.AppStoreAccountRegistration;
import lombok.Getter;
import org.apache.http.client.HttpClient;
import org.cobbzilla.util.http.ApiConnectionInfo;
import org.cobbzilla.wizard.dao.SearchResults;
import org.cobbzilla.wizard.model.ResultPage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MockAppStoreApiClient extends AppStoreApiClient {

    public MockAppStoreApiClient () { super(""); }
    public MockAppStoreApiClient(ApiConnectionInfo connectionInfo) { super(connectionInfo); }
    public MockAppStoreApiClient(String baseUri) { super(baseUri); }
    public MockAppStoreApiClient(ApiConnectionInfo connectionInfo, HttpClient httpClient) { super(connectionInfo, httpClient); }
    public MockAppStoreApiClient(String baseUri, HttpClient httpClient) { super(baseUri, httpClient); }

    @Getter private Map<String, AppStoreAccount> accounts = new HashMap<>();
    @Getter private Map<String, PublishedApp> publishedApps = new HashMap<>();
    @Getter private Map<String, CloudApp> apps = new HashMap<>();
    @Getter private Map<String, CloudAppVersion> appVersions = new HashMap<>();
    @Getter private Map<String, AppStorePublisher> publishers = new HashMap<>();
    @Getter private Map<String, String> accountPublisherMap = new HashMap<>();
    @Getter private Map<String, AppPrice> prices = new HashMap<>();
    @Getter private Map<String, AppFootprint> footprints = new HashMap<>();
    @Getter private Map<String, String> sessions = new HashMap<>();

    @Override
    public AppListing findPublishedApp(String uuid) throws Exception {

        PublishedApp publishedApp = null;
        for (PublishedApp app : publishedApps.values()) {
            if (app.getUuid().equals(uuid)) {
                publishedApp = app; break;
            }
        }
        if (publishedApp == null) return null;

        final CloudApp app = apps.get(publishedApp.getApp());
        if (app == null) return null;

        final AppStorePublisher publisher = publishers.get(app.getPublisher());
        if (publisher == null) return null;

        return new AppListing()
                .setAppVersion(publishedApp)
                .setPublisher(publisher)
                .setName(app.getName());
    }

    @Override
    public SearchResults<AppListing> searchAppStore(ResultPage page) throws Exception {

        final List<AppListing> matches = new ArrayList<>();
        int totalCount = 0;
        int i = 0;
        for (PublishedApp app : publishedApps.values()) {
            AppListing listing = findPublishedApp(app.getUuid());
            if (isMatch(listing, page)) {
                totalCount++;
                if (page.containsResult(i)) {
                    matches.add(listing);
                }
            }
            i++;
        }
        return new SearchResults<>(matches, totalCount);
    }

    private boolean isMatch(AppListing listing, ResultPage page) {
        if (page.getHasFilter()) return listing.getName().contains(page.getFilter());
        return true;
    }

    @Override
    public CloudAppVersion findAppVersion(String uuid) throws Exception { return appVersions.get(uuid); }

    @Override
    public CloudAppVersion updateAppVersion(CloudAppVersion version) throws Exception {
        if (version.getAppStatus() == CloudAppStatus.PUBLISHED) {
            publishedApps.put(apps.get(version.getApp()).getName(), new PublishedApp(version));
        }
        appVersions.put(version.getUuid(), version);
        return version;
    }

    @Override
    public CloudAppVersion defineAppVersion(CloudAppVersion version) throws Exception {
        version.initUuid();
        appVersions.put(version.getUuid(), version);
        return version;
    }

    @Override
    public AppPrice setAppPrice(AppPrice price) throws Exception {
        price.initUuid();
        prices.put(price.getUuid(), price);
        return price;
    }

    @Override
    public AppPrice[] getAppPrices(CloudApp app) throws Exception {
        List<AppPrice> found = new ArrayList<>();
        for (AppPrice price : prices.values()) {
            if (price.getCloudApp().equals(app.getUuid())) found.add(price);
        }
        return found.toArray(new AppPrice[found.size()]);
    }

    @Override
    public AppFootprint setAppFootprint(AppFootprint footprint) throws Exception {
        footprint.initUuid();
        footprints.put(footprint.getUuid(), footprint);
        return footprint;
    }

    @Override
    public AppFootprint getAppFootprint(CloudApp app) throws Exception {
        for (AppFootprint footprint : footprints.values()) {
            if (footprint.getCloudApp().equals(app.getUuid())) return footprint;
        }
        return null;
    }

    @Override
    public CloudApp updateApp(CloudApp app) throws Exception {
        return apps.put(app.getUuid(), app);
    }

    @Override
    public CloudApp findApp(String uuid) throws Exception { return apps.get(uuid); }

    @Override
    public CloudApp defineApp(CloudApp app) throws Exception {
        app.initUuid();
        apps.put(app.getUuid(), app);
        return app;
    }

    @Override
    public void deleteAccount() throws Exception {
        final String uuid = sessions.get(token);
        if (uuid != null) {
            accounts.remove(uuid);
            accountPublisherMap.remove(uuid);
            publishers.remove(uuid);

            // todo: remove other publishers and their apps/members/etc where (publisher.owner == account.uuid)
        }
    }

    @Override
    public AppStorePublisher findPublisher(String uuid) throws Exception {
        return publishers.get(uuid);
    }

    @Override
    public AppStoreAccount findAccount(String uuid) throws Exception {
        return accounts.get(uuid);
    }

    @Override
    public AppStoreAccount findAccount() throws Exception {
        return accounts.get(sessions.get(token));
    }

    @Override
    public boolean deleteToken(String token) throws Exception {
        return (sessions.remove(token) != null);
    }

    @Override
    public ApiToken refreshToken(String email, String password) throws Exception {
        return super.refreshToken(email, password);
    }

    @Override
    public ApiToken refreshToken() throws Exception {
        final ApiToken newToken = new ApiToken().init();
        final AppStoreAccount account = accounts.get(sessions.get(token));
        sessions.remove(token);
        sessions.put(newToken.getToken(), account.getUuid());
        return newToken;
    }

    @Override
    public ApiToken registerAccount(AppStoreAccountRegistration registration) throws Exception {
        final AppStoreAccount account = new AppStoreAccount();
        account.populate(registration);
        account.initUuid();
        accounts.put(account.getUuid(), account);

        final AppStorePublisher publisher = new AppStorePublisher();
        publisher.setUuid(account.getUuid());
        publisher.setOwner(account.getUuid());
        publisher.setName(registration.getName());
        publishers.put(account.getUuid(), publisher);

        final AppStorePublisherMember member = new AppStorePublisherMember();
        member.setAccount(account.getUuid());
        member.setPublisher(publisher.getUuid());
        accountPublisherMap.put(account.getUuid(), publisher.getUuid());

        final ApiToken newToken = new ApiToken().init();
        sessions.put(newToken.getToken(), account.getUuid());
        return newToken;
    }
}
