package cloudos.appstore.test;

import cloudos.appstore.client.AppStoreApiClient;
import cloudos.appstore.model.support.ApiToken;
import cloudos.appstore.model.support.AppStoreAccountRegistration;
import cloudos.appstore.model.CloudAppVersion;
import cloudos.appstore.model.support.DefineCloudAppRequest;
import org.apache.commons.lang3.RandomStringUtils;
import org.cobbzilla.util.security.ShaUtil;

import static org.cobbzilla.util.string.StringUtil.empty;
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

    public static CloudAppVersion newCloudApp(AppStoreApiClient appStoreClient, String publisherName, String bundleUrl, String bundleUrlSha) throws Exception {
        final DefineCloudAppRequest request = new DefineCloudAppRequest()
                .setBundleUrl(bundleUrl)
                .setBundleUrlSha(empty(bundleUrlSha) ? ShaUtil.sha256_url(bundleUrl) : bundleUrlSha)
                .setPublisher(publisherName);
        return appStoreClient.defineApp(request);
    }

}
