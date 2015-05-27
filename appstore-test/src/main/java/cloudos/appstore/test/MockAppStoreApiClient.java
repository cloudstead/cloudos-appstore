package cloudos.appstore.test;

import cloudos.appstore.client.AppStoreApiClient;
import cloudos.appstore.model.*;
import cloudos.appstore.model.app.AppManifest;
import cloudos.appstore.model.support.*;
import lombok.Getter;
import org.cobbzilla.util.http.ApiConnectionInfo;
import org.cobbzilla.util.io.StreamUtil;
import org.cobbzilla.wizard.dao.SearchResults;
import org.cobbzilla.wizard.model.ResultPage;
import org.cobbzilla.wizard.validation.ConstraintViolationBean;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.cobbzilla.util.daemon.ZillaRuntime.die;

public class MockAppStoreApiClient extends AppStoreApiClient {

    @Getter private Map<String, AppStoreAccount> accounts = new HashMap<>();
    @Getter private Map<String, PublishedApp> publishedApps = new HashMap<>();
    @Getter private Map<String, MockCloudApp> apps = new HashMap<>();
    @Getter private Map<String, CloudAppVersion> appVersions = new HashMap<>();
    @Getter private Map<String, AppStorePublisher> publishersByUuid = new HashMap<>();
    @Getter private Map<String, AppStorePublisher> publishersByName = new HashMap<>();
    @Getter private Map<String, String> accountPublisherMap = new HashMap<>();
    @Getter private Map<String, AppPrice> prices = new HashMap<>();
    @Getter private Map<String, AppFootprint> footprints = new HashMap<>();
    @Getter private Map<String, String> sessions = new HashMap<>();

    private AssetWebServer webServer;
    public MockAppStoreApiClient(AssetWebServer webServer, ApiConnectionInfo appStore) {
        super(appStore);
        this.webServer = webServer;
        try { registerAccount(appStore.getUser()); } catch (Exception e) { die("error registering account: "+e); }
    }

    @Override
    public AppListing findPublishedApp(String publisherName, String appName) throws Exception {

        PublishedApp publishedApp = null;
        for (PublishedApp app : publishedApps.values()) {
            if (app.getAppName().equals(appName)) {
                publishedApp = app; break;
            }
        }
        if (publishedApp == null) return null;

        final CloudApp app = apps.get(publishedApp.getAppName());
        if (app == null) return null;

        final AppStorePublisher publisher = publishersByUuid.get(app.getPublisher());
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
            AppListing listing = findPublishedApp(null, app.getAppName());
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
        if (listing == null) return false;
        if (page.getHasFilter()) return listing.getApp().getAppName().contains(page.getFilter());
        return true;
    }

    @Override
    public AppPrice setAppPrice(String publisher, AppPrice price) throws Exception {
        price.initUuid();
        prices.put(price.getUuid(), price);
        return price;
    }

    @Override
    public AppPrice[] getAppPrices(String publisher, CloudApp app) throws Exception {
        List<AppPrice> found = new ArrayList<>();
        for (AppPrice price : prices.values()) {
            if (price.getCloudApp().equals(app.getUuid())) found.add(price);
        }
        return found.toArray(new AppPrice[found.size()]);
    }

    @Override
    public AppFootprint setAppFootprint(String publisher, AppFootprint footprint) throws Exception {
        footprint.initUuid();
        footprints.put(footprint.getUuid(), footprint);
        return footprint;
    }

    @Override
    public AppFootprint getAppFootprint(String publisher, CloudApp app) throws Exception {
        for (AppFootprint footprint : footprints.values()) {
            if (footprint.getCloudApp().equals(app.getUuid())) return footprint;
        }
        return null;
    }

    @Override
    public CloudApp findApp(String publisherName, String name) throws Exception { return apps.get(name); }

    @Override
    public CloudAppVersion defineApp(String publisherName, DefineCloudAppRequest request) throws Exception {

        final List<ConstraintViolationBean> violations = new ArrayList<>();
        final AppBundle bundle = new AppBundle(request.getBundleUrl(), request.getBundleUrlSha(), webServer.getBaseUrl(), violations);

        final AppManifest manifest = bundle.getManifest();
        final AppStoreAccount account = findAccount();
        final AppStorePublisher publisher = publishersByName.get(publisherName);

        final MockCloudApp app = (MockCloudApp) new MockCloudApp()
                .setVersion(manifest.getVersion())
                .setBundle(bundle)
                .setVisibility(request.getVisibility())
                .setPublisher(publisher.getUuid())
                .setAuthor(account.getUuid())
                .setName(manifest.getName());

        final CloudAppVersion version = new CloudAppVersion(manifest.getName(), manifest.getVersion());
        apps.put(app.getName(), app);

        final String key = app.getName() + "/" + manifest.getVersion();
        appVersions.put(key, version);

        return version;
    }


    @Override
    public CloudAppVersion updateAppStatus(String publisher, String app, String version, CloudAppStatus status) throws Exception {

        final MockCloudApp cloudApp = apps.get(app);
        if (cloudApp == null) die("Not found: "+app);

        final String key = app + "/" + version;
        final CloudAppVersion appVersion = appVersions.get(key);
        final CloudAppStatus cloudAppStatus = appVersion.getStatus();
        if (cloudAppStatus != CloudAppStatus.created) die("Expected status to be 'created'");
        appVersion.setStatus(status);

        if (status.isPublished()) {
            final AppManifest manifest = cloudApp.getBundle().getManifest();
            PublishedApp publishedApp = new PublishedApp(manifest);
            publishedApp
                    .setPublisher(cloudApp.getPublisher())
                    .setAuthor(cloudApp.getAuthor())
                    .setVisibility(cloudApp.getVisibility())
                    .setBundleUrl(webServer.getBundleUrl(manifest))
                    .setBundleUrlSha(webServer.getBundleSha(manifest))
                    .setStatus(CloudAppStatus.published);
            publishedApps.put(app, publishedApp);
        }
        return appVersion;
    }

    @Override
    public CloudAppVersion findVersion(String publisher, String app, String version) throws Exception {
        final String key = app + "/" + version;
        return appVersions.get(key);
    }

    @Override
    public void deleteAccount() throws Exception {
        final String uuid = sessions.get(token);
        if (uuid != null) {
            accounts.remove(uuid);
            accountPublisherMap.remove(uuid);
            final AppStorePublisher removed = publishersByUuid.remove(uuid);
            if (removed != null) publishersByName.remove(removed.getName());

            // todo: remove other publishersByUuid and their apps/members/etc where (publisher.owner == account.uuid)
        }
    }

    @Override
    public AppStorePublisher findPublisher(String uuid) throws Exception {
        return publishersByUuid.get(uuid);
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
        ApiToken newToken = new ApiToken().init();
        token = newToken.getToken();
        for (AppStoreAccount a : accounts.values()) {
            if (a.getName().equals(email) || a.getEmail().equals(email)) {
                sessions.put(token, a.getUuid());
                return newToken;
            }
        }
        return die("refreshToken: account not found: "+email);
    }

    @Override
    public ApiToken refreshToken() throws Exception {
        final ApiToken newToken = new ApiToken().init();
        final AppStoreAccount account = accounts.get(sessions.get(token));
        sessions.remove(token);
        sessions.put(newToken.getToken(), account.getUuid());
        token = newToken.getToken();
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
        publishersByUuid.put(publisher.getUuid(), publisher);
        publishersByName.put(publisher.getName(), publisher);

        final AppStorePublisherMember member = new AppStorePublisherMember();
        member.setAccount(account.getUuid());
        member.setPublisher(publisher.getUuid());
        accountPublisherMap.put(account.getUuid(), publisher.getUuid());

        final ApiToken newToken = new ApiToken().init();
        token = newToken.getToken();
        sessions.put(token, account.getUuid());
        return newToken;
    }

    // for tests
    public void registerAccount(String user) throws Exception {
        final AppStoreAccountRegistration reg = (AppStoreAccountRegistration) new AppStoreAccountRegistration()
                .setTos(true)
                .setEmail(user + "@example.com")
                .setName(user);
        registerAccount(reg);
    }

    @Override
    public File getLatestAppBundle(String publisher, String app) throws Exception {
        return getAppBundle(publisher, app, null);
    }

    @Override
    public File getLatestAsset(String publisher, String app, String asset) throws Exception {
        return getAppAsset(publisher, app, null, asset);
    }

    @Override
    public File getAppBundle(String publisher, String app, String version) throws Exception {
        return StreamUtil.loadResourceAsFile("test-bundle.tar.gz");
    }

    @Override
    public File getAppAsset(String publisher, String app, String version, String asset) throws Exception {
        return null;
    }
}
