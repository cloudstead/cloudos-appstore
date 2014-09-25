package cloudos.appstore.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import cloudos.appstore.ValidationConstants;
import lombok.experimental.Accessors;
import org.cobbzilla.util.reflect.ReflectionUtil;
import org.cobbzilla.wizard.model.IdentifiableBase;
import org.cobbzilla.wizard.validation.HasValue;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.Currency;
import java.util.Locale;

import static cloudos.appstore.ValidationConstants.*;
import static org.cobbzilla.wizard.model.BasicConstraintConstants.UUID_MAXLEN;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"cloudApp", "isoCurrency"}))
@NoArgsConstructor
@Accessors(chain=true)
public class AppPrice extends IdentifiableBase {

    public AppPrice(AppPrice other) { ReflectionUtil.copy(this, other); }

    @HasValue(message=ERR_APP_PRICE_APP_UUID_EMPTY)
    @Size(max=UUID_MAXLEN, message=ERR_FOOTPRINT_APP_UUID_LENGTH)
    @Column(length=UUID_MAXLEN, nullable=false)
    @Getter @Setter private String cloudApp;

    @HasValue(message=ERR_APP_PRICE_CURRENCY_EMPTY)
    @Size(max=CURRENCY_MAXLEN, message=ValidationConstants.ERR_APP_PRICE_CURRENCY_LENGTH)
    @Getter @Setter private String isoCurrency = Currency.getInstance(Locale.US).getCurrencyCode();

    @JsonIgnore @Transient
    public Currency getCurrency () { return Currency.getInstance(isoCurrency); }

    @Getter @Setter private boolean paymentRequired;
    @Getter @Setter private int initialCost;
    @Getter @Setter private int monthlyFixedCost;

}
