package cloudos.appstore.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.cobbzilla.wizard.validation.HasValue;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.Size;

import static cloudos.appstore.ValidationConstants.*;
import static org.cobbzilla.wizard.model.BasicConstraintConstants.HASHEDPASSWORD_MAXLEN;
import static org.cobbzilla.wizard.model.BasicConstraintConstants.URL_MAXLEN;

/**
 * Data that can change within a CloudAppVersion that is marked LIVE
 * Changes are limited because PublishedApps may refer to these versions
 */
@Embeddable @Accessors(chain=true)
public class AppMutableData {

    @HasValue(message=ERR_APP_BLURB_EMPTY)
    @Size(max=APP_BLURB_MAXLEN, message=ERR_APP_BLURB_LENGTH)
    @Column(nullable=false)
    @Getter @Setter private String blurb;

    @HasValue(message=ERR_APP_DESCRIPTION_EMPTY)
    @Size(max=APP_DESCRIPTION_MAXLEN, message=ERR_APP_DESCRIPTION_LENGTH)
    @Column(nullable=false)
    @Getter @Setter private String description;

    @Size(max=APP_METADATA_MAXLEN, message=ERR_APP_METADATA_LENGTH)
    @Getter @Setter private String metadata;

    @Size(max=URL_MAXLEN, message=ERR_APP_TB_ICON_URL_LENGTH)
    @Column(length=URL_MAXLEN)
    @Getter @Setter private String taskBarIconUrl;

    @Size(max=HASHEDPASSWORD_MAXLEN, message=ERR_APP_TB_ICON_SHA_LENGTH)
    @Column(length=HASHEDPASSWORD_MAXLEN)
    @Getter @Setter private String taskBarIconUrlSha;

    @HasValue(message=ERR_APP_SM_ICON_URL_EMPTY)
    @Size(max=URL_MAXLEN, message=ERR_APP_SM_ICON_URL_LENGTH)
    @Column(nullable=false, length=URL_MAXLEN)
    @Getter @Setter private String smallIconUrl;

    @HasValue(message=ERR_APP_SM_ICON_SHA_EMPTY)
    @Size(max=HASHEDPASSWORD_MAXLEN, message=ERR_APP_SM_ICON_SHA_LENGTH)
    @Column(nullable=false, length=HASHEDPASSWORD_MAXLEN)
    @Getter @Setter private String smallIconUrlSha;

    @HasValue(message=ERR_APP_LG_ICON_URL_EMPTY)
    @Size(max=URL_MAXLEN, message=ERR_APP_LG_ICON_URL_LENGTH)
    @Column(nullable=false, length=URL_MAXLEN)
    @Getter @Setter private String largeIconUrl;

    @HasValue(message=ERR_APP_LG_ICON_SHA_EMPTY)
    @Size(max=HASHEDPASSWORD_MAXLEN, message=ERR_APP_LG_ICON_SHA_LENGTH)
    @Column(nullable=false, length=HASHEDPASSWORD_MAXLEN)
    @Getter @Setter private String largeIconUrlSha;

}
