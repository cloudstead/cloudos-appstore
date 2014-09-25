package cloudos.appstore.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.cobbzilla.wizard.model.IdentifiableBase;
import org.cobbzilla.wizard.validation.HasValue;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.Size;

import static cloudos.appstore.ValidationConstants.*;
import static org.cobbzilla.wizard.model.BasicConstraintConstants.UUID_MAXLEN;

@Accessors(chain=true)
@Entity @NoArgsConstructor
public class AppFootprint extends IdentifiableBase /*implements Footprint*/ {

//    public AppFootprint (Footprint other) {
//        ReflectionUtil.copy(this, other);
//    }

    @HasValue(message=ERR_FOOTPRINT_APP_UUID_EMPTY)
    @Size(max=UUID_MAXLEN, message=ERR_FOOTPRINT_APP_UUID_LENGTH)
    @Column(length=UUID_MAXLEN, nullable=false, unique=true)
    @Getter @Setter private String cloudApp;

    @HasValue(message=ERR_FOOTPRINT_CPUS_EMPTY)
    @Column(nullable=false)
    @Getter @Setter private Integer cpus;

    @HasValue(message=ERR_FOOTPRINT_MEMORY_EMPTY)
    @Column(nullable=false)
    @Getter @Setter private Integer memory;

//    @ValidEnum(type=CsUsageLevel.class, emptyOk=false, message= ERR_FOOTPRINT_NETWORK_IO_INVALID)
//    @Column(nullable=false)
//    @Getter @Setter private String networkIo;
//
//    @Transient @JsonIgnore
//    public CsUsageLevel getNetworkIoLevel () { return networkIo == null ? null : CsUsageLevel.valueOf(networkIo); }
//    public void setNetworkIoLevel (CsUsageLevel level) { this.networkIo = (level == null) ? null : level.name(); }
//
//    @ValidEnum(type=CsUsageLevel.class, emptyOk=false, message= ERR_FOOTPRINT_DISK_IO_INVALID)
//    @Column(nullable=false)
//    @Getter @Setter private String diskIo;
//
//    @Transient @JsonIgnore
//    public CsUsageLevel getDiskIoLevel () { return diskIo == null ? null : CsUsageLevel.valueOf(diskIo); }
//    public void setDiskIoLevel (CsUsageLevel level) { this.diskIo = (level == null) ? null : level.name(); }

}
