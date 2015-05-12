package cloudos.appstore.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@NoArgsConstructor @AllArgsConstructor @Accessors(chain=true)
public class AppStorePublisherMemberInvitation {

    @Getter @Setter private String accountName;
    @Getter @Setter private String publisherUuid;

}
