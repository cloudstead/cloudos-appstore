package cloudos.appstore.model.app;

import lombok.Getter;
import lombok.Setter;
import org.cobbzilla.util.collection.ArrayUtil;

import java.util.concurrent.TimeUnit;

public class AppValidation {

    @Getter @Setter private long timeout = TimeUnit.MINUTES.toSeconds(1);
    @Getter @Setter private String service_url = null;

    @Getter @Setter private AppShellCommand[] pre_scripts;
    public void addPreScript(AppShellCommand script) { pre_scripts = ArrayUtil.append(pre_scripts, script); }
    public void addPreScript(String script) { addPreScript(new AppShellCommand().setExec(script)); }

    @Getter @Setter private AppShellCommand[] post_scripts;
    public void addPostScript(AppShellCommand script) { post_scripts = ArrayUtil.append(post_scripts, script); }
    public void addPostScript(String script) { addPostScript(new AppShellCommand().setExec(script)); }

}
