package cloudos.deploy;

import cloudos.model.instance.CloudOsBase;
import cloudos.model.instance.CloudOsState;
import cloudos.model.instance.CloudOsTaskResultBase;
import edu.emory.mathcs.backport.java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.map.LRUMap;
import org.cobbzilla.util.system.Sleep;
import org.cobbzilla.wizard.model.UniquelyNamedEntity;
import org.cobbzilla.wizard.task.TaskId;
import org.cobbzilla.wizard.task.TaskServiceBase;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.cobbzilla.util.daemon.ZillaRuntime.die;

@Slf4j
public abstract class LaunchManagerBase<A extends UniquelyNamedEntity,
                                        C extends CloudOsBase,
                                        R extends CloudOsTaskResultBase<A, C>,
                                        T extends CloudOsLaunchTaskBase<A, C, R>> {

    @Autowired protected TaskServiceBase<T, R> taskService;

    protected final Map<String, T> launchers = Collections.synchronizedMap(new LRUMap(100));

    protected abstract T launchTask(A account, C instance);

    public R getResult(TaskId taskId) { return taskService.getResult(taskId.getUuid()); }

    public TaskId launch(A account, C instance) {
        if (!instance.getAdminUuid().equals(account.getUuid())) die("launch: not owner");

        final T task = launchTask(account, instance);
        final T existing = launchers.get(instance.getUuid());
        if (existing != null) {
            if (!existing.getResult().isComplete()) {
                die("launch: already launching with taskId: " + existing.getResult());
            } else if (existing.getResult().getCloudOs().isRunning()) {
                die("launch: already running: "+existing.getResult());
            } else if (!existing.getResult().getCloudOs().isLaunchable()) {
                die("launch: not launchable: "+existing.getResult());
            }
        }

        launchers.put(instance.getUuid(), task);
        return taskService.execute(task);
    }

    public static final long DESTROY_TIMEOUT = TimeUnit.MINUTES.toMillis(5);

    public CloudOsState destroy(A account, C instance) {
        if (!instance.getAdminUuid().equals(account.getUuid())) die("destroy: not owner");

        if (!instance.isDestroyable()) {
            log.warn("destroy: not destroyable");
            return instance.getState();
        }

        T task = launchers.remove(instance.getUuid());
        if (task != null) task = taskService.cancel(task.getTaskId().getUuid());

        if (task == null) {
            try {
                task = launchTask(account, instance);
                if (task.teardown()) return instance.getState();

            } catch (Exception e) {
                die("destroy: " + e, e);
            }
        }

        long start = System.currentTimeMillis();
        while (instance.getState() != CloudOsState.destroyed && !timedOut(start)) {
            Sleep.sleep(1000);
            try {
                if (!task.isInstanceRunning()) break;
            } catch (Exception e) {
                log.error("Error checking if instance is running: "+e, e);
            }
        }

        if (timedOut(start)) log.error("destroy: instance could not be destroyed (timeout)");
        return instance.getState();
    }

    protected boolean timedOut(long start) {
        return System.currentTimeMillis() - start > DESTROY_TIMEOUT;
    }
}
