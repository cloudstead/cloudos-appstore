package cloudos.appstore;

public class ApiConstants {

    public static final String H_TOKEN = "x-cloudos-appstore-api-token";

    // the only paths that allow requests without the H_TOKEN above
    public static final String AUTH_ENDPOINT = "/auth"; // for registering new accounts and refreshing tokens
    public static final String APPSTORE_ENDPOINT = "/appstore"; // for searching the app store

    // publisher endpoints
    public static final String ACCOUNTS_ENDPOINT = "/accounts";
    public static final String PUBLISHERS_ENDPOINT = "/publishers";
    public static final String MEMBERS_ENDPOINT = "/members";
    public static final String APPS_ENDPOINT = "/apps";
    public static final String EP_FOOTPRINT = "/footprint";
    public static final String EP_PRICES = "/prices";

    // cloud-user endpoints
    public static final String CLOUDS_ENDPOINT = "/clouds/auth";
    public static final String CLOUDS_API_ENDPOINT = "/clouds/api";

    // admin endpoints
    public static final String SEARCH_ENDPOINT = "/admin/search"; // for searching everything but the app store

}
