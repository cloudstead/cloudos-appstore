package cloudos.appstore.bundler;

import lombok.Getter;
import lombok.Setter;
import org.kohsuke.args4j.Argument;

import java.io.File;

public class MultiBundlerOptions extends BundlerOptions {

    public static final String USAGE_DIR = "The directory to scan for apps. Default is the current directory.";
    @Argument(usage=USAGE_DIR)
    @Getter @Setter private File dir = new File(System.getProperty("user.dir"));

}
