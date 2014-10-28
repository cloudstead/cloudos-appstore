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

import java.util.List;

@Accessors(chain=true)
public class AppListing {

    public static final JavaType searchResultType = SearchResults.jsonType(AppListing.class);

    public String getId () { return appVersion.getUuid(); }
    public void setId(String id) { /* no-op */ }

    @Getter @Setter private PublishedApp appVersion;
    @Getter @Setter private String name;
    @Getter @Setter private AppStorePublisher publisher;
    @Getter @Setter private List<AppPrice> prices;
    @Getter @Setter private AppCommunityData community;

    @Getter @Setter private AppFootprint footprint;
    public boolean hasFootprint () { return footprint != null; }

    @Getter @Setter private AppInstallStatus installStatus = AppInstallStatus.available;

}
