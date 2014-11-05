package cloudos.appstore.model.app;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum AppStyle {
    rails, php, nodejs, java_webapp, system;
    @JsonCreator public static AppStyle create(String op) { return valueOf(op.toLowerCase()); }
}