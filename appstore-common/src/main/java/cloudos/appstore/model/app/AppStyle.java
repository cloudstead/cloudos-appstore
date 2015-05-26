package cloudos.appstore.model.app;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum AppStyle {

    rails (false), php (null), nodejs (null), java_webapp (true), system (null), chef (false);

    @JsonCreator public static AppStyle create(String op) { return valueOf(op.toLowerCase()); }

    @Getter private final Boolean autoMigration;

    public boolean isChef() { return this == chef; }

}