package cloudos.deploy;

import cloudos.model.instance.CloudOsBase;
import cloudos.model.instance.CloudOsTaskResultBase;
import org.cobbzilla.wizard.model.Identifiable;
import org.cobbzilla.wizard.task.SerialTaskService;

public class CloudOsTaskService<A extends Identifiable,
                                C extends CloudOsBase,
                                T extends CloudOsLaunchTaskBase<A, C, R>,
                                R extends CloudOsTaskResultBase<A, C>>
        extends SerialTaskService<T, R> {

    @Override protected String getSerialIdentifier(T task) {
        return task.getResult().getCloudOs().getUuid();
    }

    @Override protected T mergeTask(T task, T found) {
        task.getResult().getEvents().addAll(found.getResult().getEvents());
        return task;
    }

}
