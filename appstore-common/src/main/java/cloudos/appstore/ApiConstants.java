package cloudos.appstore;

public class ApiConstants {

    public static final String H_TOKEN = "x-cloudos-appstore-api-token";

    // the only paths that allow requests without the H_TOKEN above
    public static final String AUTH_ENDPOINT = "/auth"; // for registering new accounts and refreshing tokens
    public static final String APPSTORE_ENDPOINT = "/appstore"; // for searching the app store

    // publisher endpoints
    public static final String ACCOUNTS_ENDPOINT = "/accounts";
    public static final String PUBLISHERS_ENDPOINT = "/publishers";
    public static final String APPS_ENDPOINT = "/apps";
    public static final String EP_FOOTPRINT = "/footprint";
    public static final String EP_PRICES = "/prices";

    public static final String APP_VERSIONS_ENDPOINT = "/versions";

    // consumer endpoints
    public static final String CHECKOUT_ENDPOINT = "/checkout"; // for donating to apps
}
