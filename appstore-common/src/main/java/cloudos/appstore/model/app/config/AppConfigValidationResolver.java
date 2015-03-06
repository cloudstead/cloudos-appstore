package cloudos.appstore.model.app.config;

public interface AppConfigValidationResolver {

    public boolean isValidGroup(String name);

    public boolean isValidAccount(String name);

}
