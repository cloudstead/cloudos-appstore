package cloudos.deploy;

import cloudos.model.instance.CloudOsStatusBase;
import lombok.AllArgsConstructor;
import org.cobbzilla.util.system.CommandProgressCallback;
import org.cobbzilla.util.system.CommandProgressMarker;

@AllArgsConstructor
public class CloudOsLaunchProgressCallback implements CommandProgressCallback {

    private CloudOsStatusBase status;

    @Override public void updateProgress(CommandProgressMarker marker) {
        status.update("{setup.cheffing.percent_done_"+marker.getPercent()+"}");
    }

}
