package cloudos.appstore.model.app;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class AppChefConfig {

    @Getter @Setter private List<String> install = new ArrayList<>();

    @Getter @Setter private List<AppChefUpgrade> upgrades;
}
