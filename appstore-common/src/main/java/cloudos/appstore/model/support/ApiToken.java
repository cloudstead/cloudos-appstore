package cloudos.appstore.model.support;

import lombok.*;

import java.util.UUID;

@NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode(callSuper=false)
public class ApiToken {

    @Getter @Setter private String token;

    public ApiToken init () { token = UUID.randomUUID().toString(); return this; }

}
