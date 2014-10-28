package cloudos.appstore.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.cobbzilla.util.string.StringUtil;
import org.cobbzilla.wizard.model.IdentifiableBase;
import org.cobbzilla.wizard.model.SemanticVersion;
import org.cobbzilla.wizard.validation.HasValue;
import org.cobbzilla.wizard.validation.ValidEnum;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.validation.Valid;
import javax.validation.constraints.Size;

import static cloudos.appstore.ValidationConstants.*;
import static org.cobbzilla.wizard.model.BasicConstraintConstants.HASHEDPASSWORD_MAXLEN;
import static org.cobbzilla.wizard.model.BasicConstraintConstants.URL_MAXLEN;
import static org.cobbzilla.wizard.model.BasicConstraintConstants.UUID_MAXLEN;

@MappedSuperclass @Accessors(chain=true)
public class AppVersionBase extends IdentifiableBase {

    @Column(nullable=false, length=UUID_MAXLEN)
    @Getter @Setter private String author;

    @HasValue(message=ERR_VERSION_APP_UUID_EMPTY)
    @Size(max=UUID_MAXLEN, message=ERR_VERSION_APP_UUID_LENGTH)
    @Column(nullable=false, updatable=false)
    @Getter @Setter private String app;

    @Embedded @Valid
    @Getter @Setter private SemanticVersion version = new SemanticVersion();

    @Size(max=UUID_MAXLEN, message=ERR_APP_PREV_VERSION_LENGTH)
    @Column(updatable=false)
    @Getter @Setter private String previousVersion;
    public boolean hasPreviousVerison () { return !StringUtil.empty(previousVersion); }

    @ValidEnum(type=CloudAppStatus.class, emptyOk=false, message=ERR_APP_STATUS_INVALID)
    @Column(nullable=false)
    @Getter @Setter private String status;

    @JsonIgnore @Transient
    public CloudAppStatus getAppStatus() { return CloudAppStatus.valueOf(status); }
    public AppVersionBase setAppStatus (CloudAppStatus status) { this.status = status.name(); return this; }

    @Embedded @Valid
    @Getter @Setter private AppMutableData data;

    @HasValue(message=ERR_APP_BUNDLE_URL_EMPTY)
    @Size(max=URL_MAXLEN, message=ERR_APP_BUNDLE_URL_LENGTH)
    @Column(nullable=false, length=URL_MAXLEN)
    @Getter @Setter private String bundleUrl;

    @HasValue(message=ERR_APP_BUNDLE_SHA_EMPTY)
    @Size(max=HASHEDPASSWORD_MAXLEN, message=ERR_APP_BUNDLE_CONFIG_SHA_LENGTH)
    @Column(nullable=false, length=HASHEDPASSWORD_MAXLEN)
    @Getter @Setter private String bundleUrlSha;

}
