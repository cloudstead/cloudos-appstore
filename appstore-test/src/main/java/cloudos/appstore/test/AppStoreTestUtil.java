package cloudos.appstore.test;

import cloudos.appstore.client.AppStoreApiClient;
import cloudos.appstore.model.AppMutableData;
import cloudos.appstore.model.CloudApp;
import cloudos.appstore.model.CloudAppStatus;
import cloudos.appstore.model.CloudAppVersion;
import cloudos.appstore.model.support.ApiToken;
import cloudos.appstore.model.support.AppStoreAccountRegistration;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.cobbzilla.util.io.StreamUtil;

import java.io.IOException;

import static org.cobbzilla.util.io.FileUtil.abs;
import static org.cobbzilla.util.security.ShaUtil.sha256_url;
import static org.cobbzilla.wizardtest.RandomUtil.randomEmail;
import static org.cobbzilla.wizardtest.RandomUtil.randomName;

public class AppStoreTestUtil {

    public static ApiToken registerPublisher(AppStoreApiClient appStoreClient) throws Exception {
        return appStoreClient.registerAccount(buildPublisherRegistration());
    }

    public static AppStoreAccountRegistration buildPublisherRegistration() {
        final String name = randomName();
        final String password = randomName();
        final AppStoreAccountRegistration registration = (AppStoreAccountRegistration) new AppStoreAccountRegistration()
                .setTos(true)
                .setPassword(password)
                .setEmail(randomEmail())
                .setMobilePhoneCountryCode(1)
                .setMobilePhone(RandomStringUtils.randomNumeric(10))
                .setFirstName(name)
                .setLastName(name)
                .setName(name);
        return registration;
    }

    public static CloudApp newCloudApp(AppStoreApiClient appStoreClient, String publisherUuid) throws Exception {
        return newCloudApp(appStoreClient, publisherUuid, randomName());
    }

    public static CloudApp newCloudApp(AppStoreApiClient appStoreClient, String publisherUuid, String name) throws Exception {
        final CloudApp app = buildCloudApp(publisherUuid, name);
        return appStoreClient.defineApp(app);
    }

    public static CloudApp buildCloudApp(String publisherUuid) {
        return buildCloudApp(publisherUuid, randomName());
    }

    public static CloudApp buildCloudApp(String publisherUuid, String name) {
        final CloudApp app = new CloudApp();
        app.setPublisher(publisherUuid);
        app.setName(name);
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
        version.setInteractive(RandomUtils.nextInt(0, 1) == 1);
        version.setVersion("1.0.0");

        final AppMutableData data = new AppMutableData();
        data.setBlurb(randomName(50));
        data.setDescription(randomName(1000));

        data.setSmallIconUrl(assetUrl("assets/cloud_files_small.jpg"));
        data.setSmallIconUrlSha(sha256_url(data.getSmallIconUrl()));

        data.setLargeIconUrl(assetUrl("assets/cloud_files_small.jpg"));
        data.setLargeIconUrlSha(sha256_url(data.getLargeIconUrl()));

        if (version.isInteractive()) {
            data.setTaskbarIconUrl(data.getSmallIconUrl());
            data.setTaskbarIconUrlSha(data.getSmallIconUrlSha());
        }

        version.setData(data);

        version.setBundleUrl(assetUrl("assets/cloud_files_config.json"));
        version.setBundleUrlSha(sha256_url(version.getBundleUrl()));
        return version;
    }

    public static String assetUrl(String asset) throws IOException {
        return "file://"+abs(StreamUtil.stream2file(StreamUtil.loadResourceAsStream(asset)));
    }

}
