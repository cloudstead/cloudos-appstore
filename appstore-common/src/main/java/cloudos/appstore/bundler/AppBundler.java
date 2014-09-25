package cloudos.appstore.bundler;

import cloudos.appstore.model.app.AppManifest;

import java.io.IOException;

public interface AppBundler {

    public void bundle(BundlerOptions options, AppManifest manifest) throws Exception;

}
