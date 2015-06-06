package cloudos.appstore.model.support;

import cloudos.appstore.model.app.AppLevel;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.cobbzilla.wizard.model.ResultPage;

import static org.cobbzilla.util.daemon.ZillaRuntime.empty;
import static org.cobbzilla.util.reflect.ReflectionUtil.copy;

@Accessors(chain=true)
public class AppStoreQuery extends ResultPage {

    public AppStoreQuery() {
        setSortField("name");
        setSortOrder(SortOrder.ASC);
    }

    public AppStoreQuery (String filter) {
        this();
        setFilter(filter);
    }

    public AppStoreQuery(ResultPage page) { copy(this, page); }

    public AppStoreQuery(AppStoreObjectType type, ResultPage page) {
        this(page);
        setType(type);
    }

    @Getter @Setter String publisher;
    @JsonIgnore public boolean hasPublisher () { return !empty(publisher); }

    @Getter @Setter String app;
    @JsonIgnore public boolean hasApp () { return !empty(app); }

    @Getter @Setter String version;
    @JsonIgnore public boolean hasVersion () { return !empty(version); }

    @Getter @Setter AppStoreObjectType type;
    @JsonIgnore public boolean hasType () { return !empty(type); }

    @Getter @Setter private AppLevel level = AppLevel.app;
    @JsonIgnore public boolean hasLevel () { return !empty(level); }

    // for anonymous queries where we can't get the locale from the account
    @Getter @Setter private String locale = null;
    @JsonIgnore public boolean hasLocale () { return !empty(locale); }
}
