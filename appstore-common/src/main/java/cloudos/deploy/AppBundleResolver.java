package cloudos.deploy;

import java.io.File;

public interface AppBundleResolver {

    public File getAppBundle (String name) throws Exception;

}
