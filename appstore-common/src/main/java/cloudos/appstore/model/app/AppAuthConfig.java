package cloudos.appstore.model.app;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.cobbzilla.util.http.HttpAuthType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.cobbzilla.util.daemon.ZillaRuntime.empty;

public class AppAuthConfig {

    @Getter @Setter
    HttpAuthType http_auth;
    public boolean hasHttp_auth () { return http_auth != null; }

    @Getter @Setter AppUserManagement user_management;
    public boolean hasUser_management () { return user_management != null; }

    @Setter private String home_path;
    public String getHome_path () { return empty(home_path) ? "" : home_path; }

    @Getter @Setter private Map<String, String> login_fields = new HashMap<>();
    @Getter @Setter private String login_path = "";

    // how we recognize a login page. default is the presence of the names of the login fields (and absence of any error markers)
    @Setter private List<String> login_page_markers = new ArrayList<>();
    @Getter(value=AccessLevel.PROTECTED, lazy=true) private final List<String> defaultLoginPageMarkers = initDefaultLoginPageMarkers();
    private List<String> initDefaultLoginPageMarkers() { return new ArrayList<>(login_fields.keySet()); }

    public List<String> getLogin_page_markers() {
        return login_page_markers.isEmpty() ? getDefaultLoginPageMarkers() : login_page_markers;
    }

    @Getter @Setter private String login_redirect;
    public boolean hasLogin_redirect() { return !empty(login_redirect); }

    @JsonIgnore @Getter(lazy=true) private final Pattern loginRedirectPattern = initLoginRedirectPattern();
    private Pattern initLoginRedirectPattern() { return login_redirect == null ? null : Pattern.compile(login_redirect); }

    @Getter @Setter private String registration_redirect;
    public boolean hasRegistration_redirect() { return !empty(registration_redirect); }

    @JsonIgnore @Getter(lazy=true) private final Pattern registrationRedirectPattern = initRegistrationRedirectPattern();
    private Pattern initRegistrationRedirectPattern() { return registration_redirect == null ? null : Pattern.compile(registration_redirect); }

    @Getter @Setter private Map<String, String> registration_fields = new HashMap<>();
    @Getter @Setter private String registration_path;

    @Setter private List<String> registration_page_markers = new ArrayList<>();
    @Getter(value=AccessLevel.PROTECTED, lazy=true) private final List<String> defaultRegistrationPageMarkers = initDefaultRegistrationPageMarkers();

    private List<String> initDefaultRegistrationPageMarkers() { return new ArrayList<>(registration_fields.keySet()); }

    public List<String> getRegistration_page_markers() {
        return registration_page_markers.isEmpty() ? getDefaultRegistrationPageMarkers() : registration_page_markers;
    }

    @Getter @Setter private List<String> error_page_markers = new ArrayList<>();
}
