package cloudos.appstore.model.app;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum AppWebMode {

    service, vhost, vhost_root, proxy, proxy_service, proxy_root;

    @JsonCreator public static AppWebMode create(String value) { return valueOf(value.toLowerCase()); }

    public boolean isRoot() { return this == proxy_root || this == vhost_root; }

    public boolean isProxy() { return this == proxy || this == proxy_service || this == proxy_root; }

    public boolean isProxy_service() { return this == proxy_service; }

    public boolean isSeparateHostname() { return this == vhost || this == proxy || this == proxy_root; }

}
