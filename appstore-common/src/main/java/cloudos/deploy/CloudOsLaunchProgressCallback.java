package cloudos.deploy;

import cloudos.model.instance.CloudOsTaskResultBase;
import lombok.AllArgsConstructor;
import org.cobbzilla.util.system.CommandProgressCallback;
import org.cobbzilla.util.system.CommandProgressMarker;

@AllArgsConstructor
public class CloudOsLaunchProgressCallback implements CommandProgressCallback {

    private CloudOsTaskResultBase status;

    @Override public void updateProgress(CommandProgressMarker marker) {
        status.update("{setup.cheffing.percent_done_"+marker.getPercent()+"}");
    }

}
