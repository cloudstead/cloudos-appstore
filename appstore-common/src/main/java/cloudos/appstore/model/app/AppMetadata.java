package cloudos.appstore.model.app;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.cobbzilla.util.io.FileUtil;
import org.cobbzilla.util.json.JsonUtil;
import org.cobbzilla.wizard.model.SemanticVersion;

import java.io.File;

import static org.cobbzilla.util.daemon.ZillaRuntime.empty;
import static org.cobbzilla.util.io.FileUtil.abs;

@Accessors(chain=true) @ToString
public class AppMetadata {

    public static final String METADATA_JSON = "metadata.json";

    private static final String NOT_FOUND = "app.notFound";
    private static final String INVALID_JSON = "app.invalidJson";

    @Getter @Setter private String active_version;
    @Getter @Setter private String installed_by;
    @Getter @Setter private boolean interactive = false;

    @JsonIgnore public SemanticVersion getSemanticVersion () {
        return empty(active_version) ? null : new SemanticVersion(active_version);
    }

    @JsonIgnore public boolean isActive () { return !empty(active_version); }

    @Getter @Setter private String error;
    public boolean hasError () { return !empty(error); }

    public static AppMetadata fromJson(File appDir) {

        final String name = appDir.getName();
        if (!appDir.exists() || !appDir.isDirectory()) return errorMetadata(name, NOT_FOUND);

        final File metaFile = new File(appDir, METADATA_JSON);
        if (!metaFile.exists()) return errorMetadata(name, NOT_FOUND);

        try {
            return JsonUtil.fromJson(metaFile, AppMetadata.class);
        } catch (Exception e) {
            return errorMetadata(name, INVALID_JSON);
        }
    }

    public void write (File appDir) {
        if (!appDir.exists() || !appDir.isDirectory()) throw new IllegalArgumentException("Invalid appDir: "+abs(appDir));

        final File metaFile = new File(appDir, METADATA_JSON);
        FileUtil.toFileOrDie(metaFile, JsonUtil.toJsonOrDie(this));
    }

    private static AppMetadata errorMetadata(String name, String error) {
        return new AppMetadata().setError(error);
    }

    public boolean isVersion(String version) { return active_version != null && active_version.equals(version); }

}
