package com.monkey.ultimatebot.wrapper;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import java.lang.reflect.Method;
import org.bukkit.scheduler.BukkitTask;
import org.jspecify.annotations.Nullable;

public record WrapperTask(
        String backend, @Nullable Object handle, @Nullable Runnable cancelAction) {
    private static final System.Logger LOGGER = System.getLogger(WrapperTask.class.getName());

    public static WrapperTask none(String backend) {
        return new WrapperTask(backend, null, null);
    }

    public static WrapperTask bukkit(String backend, BukkitTask task) {
        if (task == null) {
            return none(backend);
        }
        return new WrapperTask(backend, task, task::cancel);
    }

    public static WrapperTask reflective(String backend, Object taskHandle) {
        if (taskHandle == null) {
            return none(backend);
        }
        return new WrapperTask(backend, taskHandle, () -> invokeCancel(taskHandle));
    }

    public void cancel() {
        if (cancelAction != null) {
            cancelAction.run();
        }
    }

    private static void invokeCancel(Object taskHandle) {
        if (taskHandle instanceof ScheduledTask scheduledTask) {
            scheduledTask.cancel();
            return;
        }

        try {
            Method cancel = taskHandle.getClass().getMethod("cancel");
            if (!cancel.canAccess(taskHandle) && !cancel.trySetAccessible()) {
                throw new IllegalAccessException(
                        "Cannot access cancel() on " + taskHandle.getClass().getName());
            }
            cancel.invoke(taskHandle);
        } catch (ReflectiveOperationException cancelError) {
            LOGGER.log(System.Logger.Level.WARNING, "Failed to cancel reflective scheduler task", cancelError);
        }
    }
}
