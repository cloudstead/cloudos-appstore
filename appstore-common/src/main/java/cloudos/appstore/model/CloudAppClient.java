package cloudos.appstore.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import cloudos.appstore.ValidationConstants;
import org.cobbzilla.wizard.model.IdentifiableBase;
import org.cobbzilla.wizard.validation.HasValue;
import org.cobbzilla.wizard.validation.ValidEnum;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Transient;
import javax.validation.constraints.Size;

import static cloudos.appstore.ValidationConstants.*;
import static org.cobbzilla.wizard.model.BasicConstraintConstants.*;

@Entity
public class CloudAppClient extends IdentifiableBase {

    @HasValue(message=ERR_UUID_EMPTY)
    @Size(max=UUID_MAXLEN, message=ERR_UUID_LENGTH)
    @Column(unique=true, nullable=false)
    @Getter @Setter private String cloudApp;

    @ValidEnum(type=CloudAppClientType.class, emptyOk=false, message=ValidationConstants.ERR_APP_CLIENT_TYPE_INVALID)
    @Getter @Setter private String type;

    @JsonIgnore @Transient
    public CloudAppClientType getClientType () { return CloudAppClientType.valueOf(type); }
    public void setClientType(CloudAppClientType type) { this.type = type.name(); }

    @HasValue(message=ERR_APP_CLIENT_URL_EMPTY)
    @Size(max=URL_MAXLEN, message=ERR_APP_CLIENT_URL_LENGTH)
    @Column(nullable=false, length=URL_MAXLEN)
    @Getter @Setter private String clientUrl;

    @HasValue(message=ERR_APP_CLIENT_URL_SHA_EMPTY)
    @Size(max=HASHEDPASSWORD_MAXLEN, message=ERR_APP_CLIENT_URL_SHA_LENGTH)
    @Column(nullable=false, length=HASHEDPASSWORD_MAXLEN)
    @Getter @Setter private String clientUrlSha;

}
