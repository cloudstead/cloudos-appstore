package cloudos.appstore.model.app;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class AppBackupConfig {
    @Getter @Setter private List<String> files;
    @Getter @Setter private AppShellCommand[] pre;
    @Getter @Setter private AppShellCommand[] post;
}
