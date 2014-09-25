package cloudos.appstore.bundler;

import cloudos.appstore.model.app.AppManifest;

public class DefaultAppBundlerFactory implements AppBundlerFactory {

    @Override public AppBundler getBundler(AppManifest manifest) {
        return new DefaultAppBundler();
    }

}
