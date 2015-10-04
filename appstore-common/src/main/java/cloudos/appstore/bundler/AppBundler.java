package cloudos.appstore.bundler;

import cloudos.appstore.model.app.AppManifest;

import java.io.File;

public interface AppBundler {

    public File bundle(BundlerOptions options, AppManifest manifest) throws Exception;

}
