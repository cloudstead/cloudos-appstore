package cloudos.appstore.model.support;

import cloudos.appstore.model.*;
import cloudos.appstore.model.app.AppLevel;
import cloudos.appstore.model.app.AppManifest;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JavaType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.cobbzilla.wizard.dao.SearchResults;
import org.cobbzilla.wizard.model.SemanticVersion;

import java.util.Comparator;
import java.util.List;

import static org.cobbzilla.util.daemon.ZillaRuntime.empty;
import static org.cobbzilla.util.reflect.ReflectionUtil.copy;

@NoArgsConstructor @Accessors(chain=true) @Slf4j
public class AppListing {

    public static final JavaType searchResultType = SearchResults.jsonType(AppListing.class);

    public static final Comparator<? super AppListing> COMPARE_NAME = new Comparator<AppListing>() {
        @Override public int compare(AppListing a1, AppListing a2) {
            return a1.getName().compareTo(a2.getName());
        }
    };

    public static final AppListing UNPUBLISHED = new AppListing() {
        @Override public String getName() { return "unpublished"; }
    };

    public AppListing(AppListing other) { copy(this, other); }

    public AppListing(AppListing other, CloudAppVersion appVersion, AppManifest manifest) {
        copy(this, other);
        getPrivateData().setVersion(appVersion);
        getPrivateData().setManifest(manifest);
    }

    public String getId () { return getPublisher()+"/"+getName()+"/"+getVersion(); }
    public void setId(String id) { /* no-op */ }

    public String getPublisher () { return ensurePublisher().getName(); }
    public void setPublisher (String publisher) { ensurePublisher().setName(publisher); }

    public String getName () { return ensureApp().getName(); }
    public void setName (String name) { ensureApp().setName(name); }

    public String getVersion() { return ensureVersion().getVersion(); }
    public void setVersion (String version) { ensureVersion().setVersion(version); }

    @JsonIgnore public SemanticVersion getSemanticVersion () { return privateData.getVersion().getSemanticVersion(); }

    public AppVisibility getVisibility () { return ensureApp().getVisibility(); }
    public void setVisibility (AppVisibility visibility) { ensureApp().setVisibility(visibility); }

    public AppMutableData getAssets() { return ensureManifest().getAssets(); }
    public void setAssets (AppMutableData assets) { ensureManifest().setAssets(assets); }

    public AppLevel getLevel () { return ensureApp().getLevel(); }
    public void setLevel (AppLevel level) { ensureApp().setLevel(level); }

    public CloudAppStatus getStatus() { return ensureVersion().getStatus(); }
    public void setStatus (CloudAppStatus status) { ensureVersion().setStatus(status); }

    public boolean isInteractive () { return ensureManifest().isInteractive(); }
    public void setInteractive (boolean i) { ensureManifest().setInteractive(i); }

    public String getBundleUrlSha() { return ensureVersion().getBundleSha(); }
    public void setBundleUrlSha (String sha) { ensureVersion().setBundleSha(sha); }

    public String getApprovedBy () { return ensureApprovedBy().getName(); }
    public void setApprovedBy (String name) { ensureApprovedBy().setName(name); }

    public String getAuthor () { return ensureAuthor().getName(); }
    public void setAuthor (String name) { ensureAuthor().setName(name); }

    @Getter @Setter private String bundleUrl;

    @Getter @Setter private List<AppPrice> prices;
    @Getter @Setter private AppCommunityData community;

    @Getter @Setter private AppFootprint footprint;
    public boolean hasFootprint () { return footprint != null; }

    @Getter @Setter private AppInstallStatus installStatus = AppInstallStatus.available_appstore;

    @JsonIgnore @Getter private ListingPrivateData privateData = new ListingPrivateData();

    @Getter @Setter private List<CloudAppVersion> availableVersions;

    @Accessors(chain=true)
    public class ListingPrivateData {
        @Getter @Setter private AppStorePublisher.PublicView publisher;
        @Getter @Setter private CloudApp app;
        @Getter @Setter private CloudAppVersion version;
        @Getter @Setter private AppStoreAccount.PublicView author;
        @Getter @Setter private AppStoreAccount.PublicView approvedBy;
        @Getter @Setter private AppManifest manifest;
    }

    protected AppStorePublisher.PublicView ensurePublisher() {
        if (empty(privateData.publisher)) privateData.publisher = new AppStorePublisher.PublicView();
        return privateData.publisher;
    }

    protected CloudApp ensureApp() {
        if (empty(privateData.app)) privateData.app = new CloudApp();
        return privateData.app;
    }

    protected CloudAppVersion ensureVersion() {
        if (empty(privateData.version)) privateData.version = new CloudAppVersion();
        return privateData.version;
    }

    protected AppStoreAccount.PublicView ensureAuthor() {
        if (empty(privateData.author)) privateData.author = new AppStoreAccount.PublicView();
        return privateData.author;
    }

    protected AppStoreAccount.PublicView ensureApprovedBy() {
        if (empty(privateData.approvedBy)) privateData.approvedBy = new AppStoreAccount.PublicView();
        return privateData.approvedBy;
    }

    protected AppManifest ensureManifest() {
        if (empty(privateData.manifest)) privateData.manifest = new AppManifest();
        return privateData.manifest;
    }

    public boolean matches(AppStoreQuery query) { return matches(query, this); }

    public static boolean matches(AppStoreQuery query, AppListing listing) {

        // must be at the right level
        if (query.hasLevel() && listing.getLevel() != query.getLevel()) return false;

        // is the query for an exact match on publisher?
        if (query.hasPublisher() && !listing.getPublisher().equals(query.getPublisher())) return false;

        // is the query for an exact match on app name?
        if (query.hasApp() && !listing.getName().equals(query.getApp())) return false;

        // check for strings that match the filter query
        final String filter = query.getFilter();
        if (empty(filter)) return true;

        final AppStoreObjectType type = query.hasType() ? query.getType() : AppStoreObjectType.app;

        if (query.hasType()) {
            switch (type) {
                case account:
                    return matchesAccount(filter, listing);

                case publisher:
                    return matchesPublisher(filter, listing);

                case app:
                default:
                    return matchesApp(filter, listing);
            }

        } else {
            return matchesAccount(filter, listing)
                    || matchesPublisher(filter, listing)
                    || matchesApp(filter, listing);
        }
    }

    public static boolean matchesAccount(String filter, AppListing listing) {
        filter = filter.toLowerCase();
        try {
            final AppStoreAccount.PublicView author = listing.getPrivateData().getAuthor();
            return author.getFirstName().toLowerCase().contains(filter)
                    || author.getLastName().toLowerCase().contains(filter)
                    || author.getFullName().toLowerCase().contains(filter)
                    || author.getName().toLowerCase().contains(filter);
        } catch (Exception e) {
            log.warn("matchesAccount error: "+e);
            return false;
        }
    }

    public static boolean matchesPublisher(String filter, AppListing listing) {
        filter = filter.toLowerCase();
        try {
            final AppStorePublisher.PublicView publisher = listing.getPrivateData().getPublisher();
            return publisher.getName().toLowerCase().contains(filter);
        } catch (Exception e) {
            log.warn("matchesAccount error: "+e);
            return false;
        }
    }

    public static boolean matchesApp(String filter, AppListing listing) {
        filter = filter.toLowerCase();
        try {
            final CloudApp app = listing.getPrivateData().getApp();
            final AppMutableData assets = listing.getAssets();
            return app.getName().toLowerCase().contains(filter)
                    || (assets != null && (assets.getBlurb().toLowerCase().contains(filter) || assets.getDescription().toLowerCase().contains(filter)));
        } catch (Exception e) {
            log.warn("matchesAccount error: "+e);
            return false;
        }
    }

}
