package cloudos.appstore.model.app;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.cobbzilla.util.collection.ArrayUtil;

import static org.cobbzilla.util.string.StringUtil.empty;

public class AppValidation {

    @Getter @Setter private Boolean web = null;
    @JsonIgnore public boolean hasWeb() { return web != null; }

    @Getter @Setter private boolean services = true;

    @Getter @Setter private String[] ports = null;
    @JsonIgnore public boolean hasPorts() { return !empty(ports); }

    public void addPort (String port) { ports = ArrayUtil.append(ports, port); }
    public void addPort (int port) { ports = ArrayUtil.append(ports, String.valueOf(port)); }

    @Getter @Setter private AppShellCommand[] pre_scripts;
    public void addPreScript(AppShellCommand script) { pre_scripts = ArrayUtil.append(pre_scripts, script); }
    public void addPreScript(String script) { addPreScript(new AppShellCommand().setExec(script)); }

    @Getter @Setter private AppShellCommand[] post_scripts;
    public void addPostScript(AppShellCommand script) { post_scripts = ArrayUtil.append(post_scripts, script); }
    public void addPostScript(String script) { addPostScript(new AppShellCommand().setExec(script)); }

}
