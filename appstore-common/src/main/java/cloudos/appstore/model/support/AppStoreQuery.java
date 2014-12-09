package cloudos.appstore.model.support;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.cobbzilla.wizard.model.ResultPage;

@NoArgsConstructor
public class AppStoreQuery extends ResultPage {

    public AppStoreQuery (AppStoreObjectType type, ResultPage other) {
        super(other);
        setType(type);
    }

    @Getter @Setter AppStoreObjectType type;

}
