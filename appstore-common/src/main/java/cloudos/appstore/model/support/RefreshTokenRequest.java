package cloudos.appstore.model.support;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.cobbzilla.util.string.StringUtil;

@NoArgsConstructor @AllArgsConstructor @Accessors(chain=true)
public class RefreshTokenRequest {

    @Getter @Setter private String email;
    @Getter @Setter private String password;
    @Getter @Setter private ApiToken apiToken;

    public boolean hasToken () { return apiToken != null && !StringUtil.empty(apiToken.getToken()); }
    public boolean hasEmail () { return email != null && !StringUtil.empty(email); }

    public RefreshTokenRequest (String email, String password) {
        this(email, password, null);
    }

    public RefreshTokenRequest (ApiToken token) {
        this(null, null, token);
    }

}
