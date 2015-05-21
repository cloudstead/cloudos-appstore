package cloudos.appstore.model.support;

import com.fasterxml.jackson.databind.JavaType;
import lombok.Getter;
import lombok.Setter;
import cloudos.appstore.model.AppFootprint;
import cloudos.appstore.model.AppPrice;
import cloudos.appstore.model.AppStorePublisher;
import cloudos.appstore.model.PublishedApp;
import lombok.experimental.Accessors;
import org.cobbzilla.wizard.dao.SearchResults;
import org.cobbzilla.wizard.model.ResultPage;

import java.util.List;

@Accessors(chain=true)
public class AppListing {

    public static final JavaType searchResultType = SearchResults.jsonType(AppListing.class);
    public static final ResultPage DEFAULT_PAGE = new ResultPage()
            .setSortField("name").setSortOrder(ResultPage.SortOrder.ASC);

    public String getId () { return app.getId(); }
    public void setId(String id) { /* no-op */ }

    @Getter @Setter private PublishedApp app;
    @Getter @Setter private AppStorePublisher publisher;
    @Getter @Setter private List<AppPrice> prices;
    @Getter @Setter private AppCommunityData community;

    @Getter @Setter private AppFootprint footprint;
    public boolean hasFootprint () { return footprint != null; }

    @Getter @Setter private AppInstallStatus installStatus = AppInstallStatus.available_appstore;

}
