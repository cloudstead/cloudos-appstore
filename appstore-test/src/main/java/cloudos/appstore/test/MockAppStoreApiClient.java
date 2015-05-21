package cloudos.appstore.test;

import cloudos.appstore.client.AppStoreApiClient;
import cloudos.appstore.model.*;
import cloudos.appstore.model.app.AppManifest;
import cloudos.appstore.model.support.*;
import lombok.Getter;
import org.cobbzilla.wizard.dao.SearchResults;
import org.cobbzilla.wizard.model.ResultPage;
import org.cobbzilla.wizard.validation.ConstraintViolationBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.cobbzilla.util.daemon.ZillaRuntime.die;

public class MockAppStoreApiClient extends AppStoreApiClient {

    @Getter private Map<String, AppStoreAccount> accounts = new HashMap<>();
    @Getter private Map<String, PublishedApp> publishedApps = new HashMap<>();
    @Getter private Map<String, MockCloudApp> apps = new HashMap<>();
    @Getter private Map<String, CloudAppStatus> appStatus = new HashMap<>();
    @Getter private Map<String, CloudAppVersion> appVersions = new HashMap<>();
    @Getter private Map<String, AppStorePublisher> publishers = new HashMap<>();
    @Getter private Map<String, String> accountPublisherMap = new HashMap<>();
    @Getter private Map<String, AppPrice> prices = new HashMap<>();
    @Getter private Map<String, AppFootprint> footprints = new HashMap<>();
    @Getter private Map<String, String> sessions = new HashMap<>();

    private AssetWebServer webServer;
    public MockAppStoreApiClient(AssetWebServer webServer) {
        super("");
        this.webServer = webServer;
    }

    @Override
    public AppListing findPublishedApp(String appName) throws Exception {

        PublishedApp publishedApp = null;
        for (PublishedApp app : publishedApps.values()) {
            if (app.getAppName().equals(appName)) {
                publishedApp = app; break;
            }
        }
        if (publishedApp == null) return null;

        final CloudApp app = apps.get(publishedApp.getAppName()+"/"+publishedApp.getVersion());
        if (app == null) return null;

        final AppStorePublisher publisher = publishers.get(app.getPublisher());
        if (publisher == null) return null;

        return new AppListing()
                .setApp(publishedApp)
                .setPublisher(publisher);
    }

    @Override
    public SearchResults<AppListing> searchAppStore(ResultPage page) throws Exception {

        final List<AppListing> matches = new ArrayList<>();
        int totalCount = 0;
        int i = 0;
        for (PublishedApp app : publishedApps.values()) {
            AppListing listing = findPublishedApp(app.getAppName());
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
        if (page.getHasFilter()) return listing.getApp().getAppName().contains(page.getFilter());
        return true;
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
    public CloudApp findApp(String name) throws Exception { return apps.get(name); }

    @Override
    public CloudAppVersion defineApp(DefineCloudAppRequest request) throws Exception {

        final List<ConstraintViolationBean> violations = new ArrayList<>();
        final AppBundle bundle = new AppBundle(request.getBundleUrl(), request.getBundleUrlSha(), webServer.getBaseUrl(), violations);

        final AppManifest manifest = bundle.getManifest();
        final AppStoreAccount account = findAccount();
        final AppStorePublisher publisher = findPublisher(account.getUuid());

        final MockCloudApp app = (MockCloudApp) new MockCloudApp()
                .setVersion(manifest.getVersion())
                .setBundle(bundle)
                .setPublisher(publisher.getUuid())
                .setAuthor(account.getUuid())
                .setName(manifest.getName());

        final CloudAppVersion version = new CloudAppVersion(manifest.getName(), manifest.getVersion());
        apps.put(app.getName()+"/"+manifest.getVersion(), app);
        appStatus.put(version.toString(), CloudAppStatus.created);
        return version;
    }


    @Override
    public CloudAppStatus updateAppStatus(String app, String version, CloudAppStatus status) throws Exception {

        final String key = app + "/" + version;
        final MockCloudApp cloudApp = apps.get(key);
        if (cloudApp == null) die("Not found: "+key);

        final CloudAppStatus cloudAppStatus = appStatus.get(key);
        if (cloudAppStatus != CloudAppStatus.created) die("Expected status to be 'created'");
        appStatus.put(key, status);

        if (status.isPublished()) {
            final AppManifest manifest = cloudApp.getBundle().getManifest();
            PublishedApp publishedApp = new PublishedApp(manifest);
            publishedApp
                    .setPublisher(cloudApp.getPublisher())
                    .setAuthor(cloudApp.getAuthor())
                    .setBundleUrl(webServer.getBundleUrl(manifest))
                    .setBundleUrlSha(webServer.getBundleSha(manifest));
            publishedApps.put(app, publishedApp);
        }
        return status;
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
