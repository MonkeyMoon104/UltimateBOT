package com.monkey.mcbot.wrapper;

import java.lang.reflect.Method;
import org.bukkit.scheduler.BukkitTask;

public record WrapperTask(String backend, Object handle, Runnable cancelAction) {
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
        try {
            Method cancel = taskHandle.getClass().getMethod("cancel");
            cancel.invoke(taskHandle);
        } catch (ReflectiveOperationException cancelError) {
            LOGGER.log(System.Logger.Level.WARNING, "Failed to cancel reflective scheduler task", cancelError);
        }
    }
}
