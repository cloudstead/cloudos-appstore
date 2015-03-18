package cloudos.appstore.model.app;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Accessors(chain=true) @ToString
public class AppShellCommand {

    @Getter @Setter private String exec;
    @Getter @Setter private String user = "root";
    @Getter @Setter private String dir;
    @Getter @Setter private String stdin;
    @Getter @Setter private String unless;

}
