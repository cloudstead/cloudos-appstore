package cloudos.appstore.model.support;

import cloudos.model.auth.ApiToken;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import static org.cobbzilla.util.daemon.ZillaRuntime.empty;

@NoArgsConstructor @AllArgsConstructor @Accessors(chain=true)
public class RefreshTokenRequest {

    @Getter @Setter private String email;
    @Getter @Setter private String password;
    @Getter @Setter private String secondFactor;
    @Getter @Setter private ApiToken apiToken;

    public boolean hasToken () { return apiToken != null && !empty(apiToken.getToken()); }
    public boolean hasEmail () { return email != null && !empty(email); }

    public RefreshTokenRequest (String email, String password) {
        this(email, password, null, null);
    }

    public RefreshTokenRequest (ApiToken token) {
        this(null, null, null, token);
    }

}
