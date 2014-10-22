package cloudos.appstore.model.support;

import cloudos.appstore.model.PublishedApp;
import lombok.Getter;
import lombok.Setter;

public class AppCommunityData {

    @Getter @Setter private PublishedApp publishedApp;
    @Getter @Setter private int installCount;
    @Getter @Setter private int ratingsCount;
    @Getter @Setter private int averageRating; // avg # stars x 10 (0 >= averageRating >= 50)

}
