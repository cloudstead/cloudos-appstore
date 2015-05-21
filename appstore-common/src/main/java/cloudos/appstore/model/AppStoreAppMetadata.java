package cloudos.appstore.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.cobbzilla.util.io.FileUtil;

import java.io.File;

import static org.cobbzilla.util.daemon.ZillaRuntime.die;
import static org.cobbzilla.util.io.FileUtil.toFileOrDie;
import static org.cobbzilla.util.json.JsonUtil.fromJsonOrDie;
import static org.cobbzilla.util.json.JsonUtil.toJsonOrDie;

@Accessors(chain=true)
public class AppStoreAppMetadata {

    public static final String APPSTORE_APP_METADATA = "appstore-metadata.json";

    @Getter @Setter private String bundleSha;
    @Getter @Setter private CloudAppStatus status;
    @Getter @Setter private String approvedBy; // uuid of account that moved this to "published" statue

    @JsonIgnore public boolean isPublished() { return status.isPublished(); }

    public static File getMetadataFile(File versionDir) { return new File(versionDir, APPSTORE_APP_METADATA); }

    public void write(File versionDir) {
        if (versionDir == null || !versionDir.isDirectory()) die("Not a directory: "+versionDir);
        toFileOrDie(getMetadataFile(versionDir), toJsonOrDie(this));
    }

    public static AppStoreAppMetadata read(File versionDir) {
        return fromJsonOrDie(FileUtil.toStringOrDie(getMetadataFile(versionDir)), AppStoreAppMetadata.class);
    }

}
