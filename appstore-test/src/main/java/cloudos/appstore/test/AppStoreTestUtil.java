package cloudos.appstore.test;

import cloudos.appstore.client.AppStoreApiClient;
import cloudos.appstore.model.*;
import cloudos.appstore.model.support.ApiToken;
import cloudos.appstore.model.support.AppStoreAccountRegistration;
import org.cobbzilla.util.io.StreamUtil;
import org.cobbzilla.wizard.model.HashedPassword;
import org.cobbzilla.wizard.server.RestServer;
import org.springframework.context.ApplicationContext;

import java.io.IOException;

import static org.cobbzilla.util.security.ShaUtil.sha256_url;
import static org.cobbzilla.wizardtest.RandomUtil.randomEmail;
import static org.cobbzilla.wizardtest.RandomUtil.randomName;

public class AppStoreTestUtil {

    public static AppStoreTestUser createAdminUser (AppStoreApiClient appStoreClient,
                                                    RestServer server) throws Exception {
        final AppStoreAccountRegistration registration = AppStoreTestUtil.buildPublisherRegistration();

        final String adminToken = appStoreClient.registerAccount(registration).getToken();
        AppStoreAccount admin = appStoreClient.findAccount();
        admin.setAdmin(true);
        admin.setPassword(new HashedPassword(registration.getPassword()));

        // crack open the application context to access the DAO directly and set the admin flag to true
        final ApplicationContext applicationContext = server.getApplicationContext();
        final Object accountDAO = applicationContext.getBean("appStoreAccountDAO");
        accountDAO.getClass().getMethod("update", Object.class).invoke(accountDAO, admin);

        appStoreClient.setToken(null);
        return new AppStoreTestUser(adminToken, admin);
    }

    public static ApiToken registerPublisher(AppStoreApiClient appStoreClient) throws Exception {
        return appStoreClient.registerAccount(buildPublisherRegistration());
    }

    public static AppStoreAccountRegistration buildPublisherRegistration() {
        final AppStoreAccountRegistration registration = new AppStoreAccountRegistration();
        final String password = randomName();
        registration.setEmail(randomEmail());
        registration.setPassword(password);
        registration.setPublisherName(randomName());
        registration.setPublisherTos(1);
        return registration;
    }

    public static CloudApp newCloudApp(AppStoreApiClient appStoreClient, String publisherUuid) throws Exception {
        final CloudApp app = buildCloudApp(publisherUuid);
        return appStoreClient.defineApp(app);
    }

    public static CloudApp buildCloudApp(String publisherUuid) {
        final CloudApp app = new CloudApp();
        app.setPublisher(publisherUuid);
        app.setName(randomName());
        return app;
    }

    public static CloudAppVersion newCloudAppVersion(AppStoreApiClient appStoreClient, CloudApp app) throws Exception {
        final CloudAppVersion version = buildCloudAppVersion(app);
        return appStoreClient.defineAppVersion(version);
    }

    public static CloudAppVersion buildCloudAppVersion(CloudApp app) throws Exception {
        final CloudAppVersion version = new CloudAppVersion();
        version.setApp(app.getUuid());
        version.setAppStatus(CloudAppStatus.NEW);

        final AppMutableData data = new AppMutableData();
        data.setDescription(randomName(1000));

        data.setSmallIconUrl(assetUrl("assets/cloud_files_small.jpg"));
        data.setSmallIconUrlSha(sha256_url(data.getSmallIconUrl()));

        data.setLargeIconUrl(assetUrl("assets/cloud_files_small.jpg"));
        data.setLargeIconUrlSha(sha256_url(data.getLargeIconUrl()));

        version.setData(data);

        version.setServerConfigUrl(assetUrl("assets/cloud_files_config.json"));
        version.setServerConfigUrlSha(sha256_url(version.getServerConfigUrl()));
        return version;
    }

    public static String assetUrl(String asset) throws IOException {
        return "file://"+ StreamUtil.stream2file(StreamUtil.loadResourceAsStream(asset)).getAbsolutePath();
    }

}
