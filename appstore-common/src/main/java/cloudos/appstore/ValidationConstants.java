package cloudos.appstore;

public class ValidationConstants {

    public static final String ERR_EMAIL_INVALID = "err.account.email.invalid";
    public static final String ERR_EMAIL_EMPTY = "err.account.email.empty";
    public static final String ERR_EMAIL_NOT_UNIQUE = "err.account.email.notUnique";
    public static final String ERR_EMAIL_LENGTH = "err.account.email.length";
    public static final int EMAIL_MAXLEN = 255;

    public static final String ERR_EXACTLY_ONE_TOS_REQUIRED = "err.account.tos.notOne";

    public static final String ERR_NAME_NOT_UNIQUE = "err.account.name.notUnique";

    public static final String ERR_PUBLISHER_NAME_NOT_UNIQUE = "err.publisher.name.notUnique";
    public static final String ERR_PUBLISHER_NAME_EMPTY = "err.publisher.name.empty";
    public static final String ERR_PUBLISHER_NAME_LENGTH = "err.publisher.name.length";
    public static final int PUBLISHER_NAME_MAXLEN = 255;

    public static final String ERR_OWNER_UUID_EMPTY = "err.publisher.owner.empty";
    public static final String ERR_OWNER_UUID_LENGTH = "err.publisher.owner.length";

    public static final String ERR_MEMBER_PUBLISHER_UUID_EMPTY = "err.member.publisher.empty";
    public static final String ERR_MEMBER_PUBLISHER_UUID_LENGTH = "err.member.publisher.length";

    public static final String ERR_MEMBER_ACCOUNT_UUID_EMPTY = "err.member.account.empty";
    public static final String ERR_MEMBER_ACCOUNT_UUID_LENGTH = "err.member.account.length";
    public static final String ERR_MEMBER_ACCOUNT_ACTIVATION_LENGTH = "err.member.activation.length";

    public static final String ERR_APP_PUBLISHER_UUID_EMPTY = "err.app.publisher.empty";
    public static final String ERR_APP_PUBLISHER_UUID_LENGTH = "err.app.publisher.length";
    public static final String ERR_APP_PUBLISHER_INVALID = "err.app.publisher.invalid";

    public static final String ERR_APP_AUTHOR_EMPTY = "err.app.author.empty";
    public static final String ERR_APP_AUTHOR_INVALID = "err.app.author.invalid";

    public static final String ERR_APP_CANNOT_DELETE_ACTIVE_VERSION = "err.app.cannotUnpublishActiveVersion";

    public static final String ERR_APP_CANNOT_CHANGE_SERVER_CONFIG = "err.app.cannotChangeServerConfig";

    public static final String ERR_PUBLISHED_APP_APPROVED_BY_UUID_EMPTY = "err.publishedApp.approvedBy.empty";
    public static final String ERR_PUBLISHED_APP_APPROVED_BY_UUID_LENGTH = "err.publishedApp.approvedBy.length";

    public static final String ERR_APP_NAME_NOT_UNIQUE = "err.app.name.notUnique";
    public static final String ERR_APP_NAME_EMPTY = "err.app.name.empty";
    public static final String ERR_APP_NAME_LENGTH = "err.app.name.length";
    public static final int APP_NAME_MAXLEN = 255;

    public static final String ERR_APP_DESCRIPTION_EMPTY = "err.app.description.empty";
    public static final String ERR_APP_DESCRIPTION_LENGTH = "err.app.description.length";
    public static final int APP_DESCRIPTION_MAXLEN = 16384;

    public static final String ERR_APP_BLURB_EMPTY = "err.app.blurb.empty";
    public static final String ERR_APP_BLURB_LENGTH = "err.app.blurb.length";
    public static final int APP_BLURB_MAXLEN = 200;

    public static final String ERR_APP_METADATA_LENGTH = "err.app.metadata.length";
    public static final int APP_METADATA_MAXLEN = 65536;

    public static final String ERR_APP_VERSION_EMPTY = "err.app.version.empty";
    public static final String ERR_APP_VERSION_INVALID = "err.app.version.invalid";
    public static final String ERR_APP_VERSION_LENGTH = "err.app.version.length";
    public static final int APP_VERSION_MAXLEN = 255;

    public static final String ERR_APP_VERSION_ALREADY_EXISTS = "err.app.version.alreadyExists";

    public static final String ERR_APP_STATUS_INVALID = "err.app.status.invalid";

    public static final String ERR_APP_TB_ICON_ALT_TEXT_LENGTH = "err.app.taskbarIconAltText.length";
    public static final int APP_TB_ICON_ALT_TEXT_LENGTH = 30;

    public static final String ERR_APP_TB_ICON_URL_LENGTH = "err.app.taskbarIconUrl.length";
    public static final String ERR_APP_TB_ICON_SHA_LENGTH = "err.app.taskbarIconSha.length";

    public static final String ERR_APP_SM_ICON_URL_EMPTY = "err.app.smallIconUrl.empty";
    public static final String ERR_APP_SM_ICON_URL_LENGTH = "err.app.smallIconUrl.length";
    public static final String ERR_APP_SM_ICON_SHA_EMPTY = "err.app.smallIconSha.empty";
    public static final String ERR_APP_SM_ICON_SHA_LENGTH = "err.app.smallIconSha.length";

    public static final String ERR_APP_LG_ICON_URL_EMPTY = "err.app.largeIconUrl.empty";
    public static final String ERR_APP_LG_ICON_URL_LENGTH = "err.app.largeIconUrl.length";
    public static final String ERR_APP_LG_ICON_SHA_EMPTY = "err.app.largeIconSha.empty";
    public static final String ERR_APP_LG_ICON_SHA_LENGTH = "err.app.largeIconSha.length";

    public static final String ERR_APP_BUNDLE_URL_LENGTH = "err.app.bundleUrl.length";
    public static final String ERR_APP_BUNDLE_URL_EMPTY = "err.app.bundleUrl.empty";
    public static final String ERR_APP_BUNDLE_SHA_EMPTY = "err.app.bundleSha.empty";
    public static final String ERR_APP_BUNDLE_CONFIG_SHA_LENGTH = "err.app.serverConfigSha.length";

    public static final String ERR_SHA_MISMATCH_PREFIX = "err.sha.mismatch.";
    public static final String ERR_SHA_CALCULATION_PREFIX = "err.sha.calculation.";
    public static final String ERR_URL_EMPTY = "err.url.empty.";
    public static final String ERR_SHA_EMPTY = "err.sha.empty.";

    public static final String SMALL_ICON = "smallIcon";
    public static final String LARGE_ICON = "largeIcon";
    public static final String TASKBAR_ICON = "taskbarIcon";
    public static final String BUNDLE_URL = "bundleUrl";

    public static final String ERR_VERSION_APP_UUID_EMPTY = "err.app.version.app.empty";
    public static final String ERR_VERSION_APP_UUID_LENGTH = "err.app.version.app.length";

    public static final String ERR_APP_CLIENT_TYPE_INVALID = "err.app.client.type.invalid";

    public static final String ERR_APP_CLIENT_URL_EMPTY = "err.app.client.url.invalid";
    public static final String ERR_APP_CLIENT_URL_LENGTH = "err.app.client.url.length";

    public static final String ERR_APP_CLIENT_URL_SHA_EMPTY = "err.app.client.urlSha.invalid";
    public static final String ERR_APP_CLIENT_URL_SHA_LENGTH = "err.app.client.urlSha.length";

    public static final String ERR_FOOTPRINT_APP_UUID_EMPTY = "err.footprint.appUuid.empty";
    public static final String ERR_FOOTPRINT_APP_UUID_LENGTH = "err.footprint.appUuid.lemgth";

    public static final String ERR_FOOTPRINT_CPUS_EMPTY = "err.footprint.cpus.empty";
    public static final String ERR_FOOTPRINT_MEMORY_EMPTY = "err.footprint.memory.empty";

    public static final String ERR_FOOTPRINT_NETWORK_IO_INVALID = "err.footprint.networkIo.invalid";
    public static final String ERR_FOOTPRINT_DISK_IO_INVALID = "err.footprint.diskIo.invalid";

    public static final String ERR_APP_PRICE_APP_UUID_EMPTY = "err.appPrice.appUuid.empty";

    public static final String ERR_APP_PRICE_CURRENCY_EMPTY = "err.appPrice.currency.empty";
    public static final String ERR_APP_PRICE_CURRENCY_LENGTH = "err.appPrice.currency.length";
    public static final int CURRENCY_MAXLEN = 3;
}
