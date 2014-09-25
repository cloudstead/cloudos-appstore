package cloudos.appstore.bundler;

import cloudos.appstore.model.app.AppManifest;

public interface AppBundlerFactory {

    public AppBundler getBundler(AppManifest manifest);

}
