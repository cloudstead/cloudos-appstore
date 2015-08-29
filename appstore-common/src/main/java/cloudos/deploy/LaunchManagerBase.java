package cloudos.deploy;

import cloudos.model.instance.CloudOsBase;
import cloudos.model.instance.CloudOsState;
import cloudos.model.instance.CloudOsTaskResultBase;
import lombok.extern.slf4j.Slf4j;
import org.cobbzilla.util.system.Sleep;
import org.cobbzilla.wizard.model.UniquelyNamedEntity;
import org.cobbzilla.wizard.server.config.RestServerConfiguration;
import org.cobbzilla.wizard.task.TaskId;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.TimeUnit;

import static org.cobbzilla.util.daemon.ZillaRuntime.die;
import static org.cobbzilla.util.reflect.ReflectionUtil.getFirstTypeParam;
import static org.cobbzilla.util.reflect.ReflectionUtil.instantiate;

@Slf4j
public abstract class LaunchManagerBase<A extends UniquelyNamedEntity,
                                        C extends CloudOsBase,
                                        R extends CloudOsTaskResultBase<A, C>,
                                        T extends CloudOsLaunchTaskBase<A, C, R>> {

    @Autowired protected CloudOsTaskService<A, C, T, R> taskService;

    public abstract RestServerConfiguration getConfiguration ();

    protected T launchTask(A account, C instance) {
        final Class<T> taskClass = getFirstTypeParam(getClass(), CloudOsLaunchTaskBase.class);
        final T task = getConfiguration().autowire(instantiate(taskClass));
        task.init(account, instance);
        return task;
    }

    public R getResult(TaskId taskId) { return taskService.getResult(taskId.getUuid()); }

    public boolean isRunning(C instance) { return taskService.isRunning(instance.getUuid()); }

    public TaskId launch(A account, C instance) {
        if (!instance.getAdminUuid().equals(account.getUuid())) die("launch: not owner");

        if (isRunning(instance)) die("launch: already running");

        final T task = launchTask(account, instance);
        return taskService.execute(task);
    }

    public static final long DESTROY_TIMEOUT = TimeUnit.MINUTES.toMillis(5);

    public CloudOsState destroy(A account, C instance) {
        if (!instance.getAdminUuid().equals(account.getUuid())) die("destroy: not owner");

        if (!instance.isDestroyable()) {
            log.warn("destroy: not destroyable");
            return instance.getState();
        }

        T task = taskService.getTask(instance.getUuid());
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

    protected boolean timedOut(long start) { return System.currentTimeMillis() - start > DESTROY_TIMEOUT; }

}
