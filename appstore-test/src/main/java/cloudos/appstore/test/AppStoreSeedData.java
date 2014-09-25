package cloudos.appstore.test;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.math.RandomUtils;
import cloudos.appstore.client.AppStoreApiClient;
import cloudos.appstore.model.AppStoreAccount;
import cloudos.appstore.model.CloudApp;
import cloudos.appstore.model.CloudAppStatus;
import cloudos.appstore.model.CloudAppVersion;
import cloudos.appstore.model.support.ApiToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AppStoreSeedData {

    @Getter @Setter private Map<AppStoreAccount, ApiToken> tokens = new HashMap<>();
    @Getter @Setter private Map<String, AppStoreAccount> publishersByUuid = new HashMap<>();
    @Getter @Setter private Multimap<AppStoreAccount, CloudApp> apps = HashMultimap.create();
    @Getter @Setter private Multimap<CloudApp, CloudAppVersion> versions = HashMultimap.create();
    @Getter @Setter private Map<CloudApp, CloudAppVersion> publishedApps = new HashMap<>();

    public CloudApp getFirstApp () {
        return publishedApps.keySet().iterator().next();
    }

    public AppStoreSeedData (AppStoreApiClient appStoreClient,
                             String adminToken,
                             int numAccounts, int numApps, int numVersions) throws Exception {

        // create publisher accounts
        for (int i=0; i<numAccounts; i++) {
            ApiToken token = AppStoreTestUtil.registerPublisher(appStoreClient);
            AppStoreAccount account = appStoreClient.findAccount();
            tokens.put(account, token);
            publishersByUuid.put(account.getUuid(), account);
        }

        // each account has some apps
        for (AppStoreAccount publisher : tokens.keySet()) {
            final ApiToken token = tokens.get(publisher);
            for (int i=0; i<numApps; i++) {
                appStoreClient.pushToken(token.getToken());
                CloudApp app = AppStoreTestUtil.newCloudApp(appStoreClient, publisher.getUuid());
                apps.put(publisher, app);
                appStoreClient.popToken();
            }
        }

        // each app has some versions
        for (CloudApp app : apps.values()) {
            for (int i=0; i<numVersions; i++) {
                appStoreClient.pushToken(tokens.get(publishersByUuid.get(app.getPublisher())).getToken());
                CloudAppVersion version = AppStoreTestUtil.newCloudAppVersion(appStoreClient, app);
                versions.put(app, version);
                appStoreClient.popToken();
            }
        }

        // publish a random version of each app
        appStoreClient.pushToken(adminToken);
        for (CloudApp app : apps.values()) {
            ArrayList<CloudAppVersion> available = new ArrayList<>(versions.get(app));
            CloudAppVersion version = available.get(RandomUtils.nextInt() % available.size());
            version.setAppStatus(CloudAppStatus.PUBLISHED);
            appStoreClient.updateAppVersion(version);
            publishedApps.put(app, version);
        }
        appStoreClient.popToken();

    }
}
