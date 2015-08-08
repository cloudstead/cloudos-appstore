package cloudos.appstore.test;

import cloudos.appstore.client.AppStoreApiClient;
import cloudos.appstore.model.*;
import cloudos.appstore.model.app.AppManifest;
import cloudos.appstore.model.support.*;
import cloudos.model.auth.ApiToken;
import lombok.Getter;
import org.apache.http.client.HttpClient;
import org.cobbzilla.util.http.ApiConnectionInfo;
import org.cobbzilla.util.io.StreamUtil;
import org.cobbzilla.wizard.dao.SearchResults;
import org.cobbzilla.wizard.validation.ConstraintViolationBean;
import org.cobbzilla.wizardtest.TestNames;

import java.io.File;
import java.util.*;

import static org.cobbzilla.util.daemon.ZillaRuntime.die;
import static org.cobbzilla.wizardtest.TestNames.animal;

public class MockAppStoreApiClient extends AppStoreApiClient {

    @Getter private Map<String, AppStoreAccount> accounts = new HashMap<>();
    @Getter private Map<String, AppListing> appListings = new HashMap<>();
    @Getter private Map<String, MockCloudApp> apps = new HashMap<>();
    @Getter private Map<String, CloudAppVersion> appVersions = new HashMap<>();
    @Getter private Map<String, AppStorePublisher> publishersByUuid = new HashMap<>();
    @Getter private Map<String, AppStorePublisher> publishersByName = new HashMap<>();
    @Getter private Map<String, String> accountPublisherMap = new HashMap<>();
    @Getter private Map<String, AppPrice> prices = new HashMap<>();
    @Getter private Map<String, AppFootprint> footprints = new HashMap<>();
    @Getter private Map<String, String> sessions = new HashMap<>();

    private AssetWebServer webServer;
    private HttpClient mockHttpClient;

    public MockAppStoreApiClient(AssetWebServer webServer, ApiConnectionInfo info) {
        super(info);
        if (info.getUser() == null) info.setUser(TestNames.safeAnimal());

        this.webServer = webServer;
        this.mockHttpClient = new MockAppStoreHttpClient(this);
        registerAccount(info.getUser());
    }

    @Override public HttpClient getHttpClient() { return mockHttpClient; }

//    @Override public ApiConnectionInfo getConnectionInfo() {
//        return mockConnectionInfo;
//    }

    @Override
    public AppListing findAppListing(String publisherName, String appName) throws Exception {

        AppListing listing = null;
        for (AppListing app : appListings.values()) {
            if (app.getName().equals(appName)) {
                listing = app; break;
            }
        }
        if (listing == null) return null;

        final CloudApp app = apps.get(listing.getName());
        if (app == null) return null;

        final AppStorePublisher publisher = publishersByUuid.get(app.getPublisher());
        if (publisher == null) return null;

        listing = new AppListing();
        listing.getPrivateData()
                .setPublisher(quickPublisherView(publisherName));
        return listing;
    }

    protected AppStorePublisher.PublicView quickPublisherView(String publisherName) {
        final AppStorePublisher publisher = new AppStorePublisher();
        publisher.setName(publisherName);
        return publisher.publicView();
    }

    @Override
    public SearchResults<AppListing> searchAppStore(AppStoreQuery query) throws Exception {

        final List<AppListing> matches = new ArrayList<>();
        int totalCount = 0;
        for (AppListing listing : appListings.values()) {
            if (listing.matches(query)) {
                totalCount++;
                matches.add(listing);
            }
        }

        if (matches.isEmpty()) return new SearchResults<>(Collections.EMPTY_LIST, 0);

        // find the correct page of results
        final int start = query.getPageOffset();
        final int end = query.getPageEndOffset();

        if (start >= matches.size()) return new SearchResults<>(Collections.EMPTY_LIST, totalCount);
        if (end >= matches.size()) return new SearchResults<>(matches.subList(start, matches.size()), totalCount);

        return new SearchResults<>(matches.subList(start, end), totalCount);
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
    public CloudApp findApp(String publisherName, String name) throws Exception {
        final MockCloudApp cloudApp = apps.get(name);
        if (cloudApp == null) return null;
        cloudApp.setPublishedBy(publishersByUuid.get(cloudApp.getPublisher()));
        cloudApp.setAuthoredBy(buildAccount(cloudApp));
        cloudApp.setVersions(findVersions(name));
        return cloudApp;
    }

    private List<CloudAppVersion> findVersions(String name) {
        final List<CloudAppVersion> found = new ArrayList<>();
        for (Map.Entry<String, CloudAppVersion> v : appVersions.entrySet()) {
            if (v.getKey().startsWith(name + "/")) {
                found.add(v.getValue());
            }
        }
        return found;
    }

    @Override
    public CloudAppVersion defineApp(String publisherName, DefineCloudAppRequest request) throws Exception {

        final List<ConstraintViolationBean> violations = new ArrayList<>();

        final AppAssetUrlGenerator assetUrlGenerator = new AppAssetUrlGenerator() {
            @Override public String generateBaseUrl(String app, String version) {
                return webServer.getBaseUrl();
            }
        };

        final AppBundle bundle = new AppBundle(request.getBundleUrl(), request.getBundleUrlSha(), assetUrlGenerator, violations);

        final AppManifest manifest = bundle.getManifest();
        final AppStoreAccount account = findAccount();
        final AppStorePublisher publisher = publishersByName.get(publisherName.toLowerCase());

        final MockCloudApp app = (MockCloudApp) new MockCloudApp()
                .setVersion(manifest.getVersion())
                .setBundle(bundle)
                .setVisibility(request.getVisibility())
                .setLevel(manifest.getLevel())
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

        app = app.toLowerCase(); // ensure app is lowercase
        final MockCloudApp cloudApp = apps.get(app);
        if (cloudApp == null) die("Not found: "+app);

        final String key = app + "/" + version;
        final CloudAppVersion appVersion = appVersions.get(key);
        final CloudAppStatus cloudAppStatus = appVersion.getStatus();
        if (cloudAppStatus != CloudAppStatus.created) die("Expected status to be 'created'");
        appVersion.setStatus(status);

        if (status.isPublished()) {
            final AppManifest manifest = cloudApp.getBundle().getManifest();
            final AppListing listing = new AppListing()
                    .setBundleUrl(webServer.getBundleUrl(manifest));

            listing.getPrivateData()
                    .setPublisher(quickPublisherView(publisher))
                    .setAuthor(buildAccount(cloudApp).publicView())
                    .setApp(cloudApp)
                    .setVersion(appVersion);
            appListings.put(app, listing);
        }
        return appVersion;
    }

    protected AppStoreAccount buildAccount(MockCloudApp cloudApp) {
        final AppStoreAccount account = (AppStoreAccount) new AppStoreAccount()
                .setFirstName(animal())
                .setLastName(animal())
                .setName(cloudApp.getAuthor());
        return account;
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
    public ApiToken registerAccount(String user) {
        final AppStoreAccountRegistration reg = (AppStoreAccountRegistration) new AppStoreAccountRegistration()
                .setTos(true)
                .setEmail(user + "@example.com")
                .setName(user);
        try {
            return registerAccount(reg);
        } catch (Exception e) { return die("registerAccount: "+e, e); }
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
