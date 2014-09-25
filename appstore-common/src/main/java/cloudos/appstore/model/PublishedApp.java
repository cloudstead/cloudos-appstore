package cloudos.appstore.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.cobbzilla.util.reflect.ReflectionUtil;
import org.cobbzilla.util.string.StringUtil;
import org.cobbzilla.wizard.validation.HasValue;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Transient;
import javax.validation.constraints.Size;

import static cloudos.appstore.ValidationConstants.ERR_PUBLISHED_APP_APPROVED_BY_UUID_EMPTY;
import static cloudos.appstore.ValidationConstants.ERR_PUBLISHED_APP_APPROVED_BY_UUID_LENGTH;
import static org.cobbzilla.wizard.model.BasicConstraintConstants.UUID_MAXLEN;

@Entity @NoArgsConstructor
public class PublishedApp extends AppVersionBase {

    @HasValue(message=ERR_PUBLISHED_APP_APPROVED_BY_UUID_EMPTY)
    @Size(max=UUID_MAXLEN, message=ERR_PUBLISHED_APP_APPROVED_BY_UUID_LENGTH)
    @Column(nullable=false, updatable=false, length=UUID_MAXLEN)
    @Getter @Setter private String approvedBy;

    public PublishedApp(CloudAppVersion version) {
        ReflectionUtil.copy(this, version);
    }

    // needed for ember
    @Transient public String getId() { return getUuid(); }
    public void setId(String id) { setUuid(id); }

    @Override
    public void beforeCreate() {
        if (StringUtil.empty(getUuid())) throw new IllegalArgumentException("PublishedApp.beforeCreate: uuid cannot be null");
    }

}
