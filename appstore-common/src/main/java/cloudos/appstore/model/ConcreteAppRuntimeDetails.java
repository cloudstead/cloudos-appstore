package cloudos.appstore.model;

import lombok.Delegate;

public class ConcreteAppRuntimeDetails extends AppRuntimeDetails {

    private interface ConcreteAppRuntimeOverrides { public String getPath(); }

    @Delegate(excludes=ConcreteAppRuntimeOverrides.class) private AppRuntimeDetails details;

    private String uriBase;

    public ConcreteAppRuntimeDetails(AppRuntimeDetails details, String uriBase) {
        this.details = details; this.uriBase = uriBase;
    }

    @Override public String getPath() { return details.getPath(uriBase); }

}
