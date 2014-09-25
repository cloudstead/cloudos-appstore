package cloudos.appstore.test;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import cloudos.appstore.model.AppStoreAccount;

@NoArgsConstructor @AllArgsConstructor
public class AppStoreTestUser {

    @Getter @Setter private String token;
    @Getter @Setter private AppStoreAccount account;

}
