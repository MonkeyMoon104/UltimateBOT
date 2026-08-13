package com.monkey.ultimatebot.wrapper;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.scheduler.BukkitTask;
import org.jspecify.annotations.Nullable;

public final class WrapperTask {
    private final String backend;
    private final @Nullable Object handle;
    private final @Nullable Runnable cancelAction;

    public WrapperTask(String backend, @Nullable Object handle, @Nullable Runnable cancelAction) {
        this.backend = backend;
        this.handle = handle;
        this.cancelAction = cancelAction;
    }

    public String backend() {
        return backend;
    }
    public @Nullable Object handle() {
        return handle;
    }
    public @Nullable Runnable cancelAction() {
        return cancelAction;
    }

    private static final Logger LOGGER = Logger.getLogger(WrapperTask.class.getName());

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
        if (taskHandle instanceof ScheduledTask) { ScheduledTask scheduledTask = (ScheduledTask) taskHandle;
            scheduledTask.cancel();
            return;
        }

        try {
            Method cancel = taskHandle.getClass().getMethod("cancel");
            try {
                cancel.setAccessible(true);
            } catch (SecurityException ignored) {
                // best-effort on older JVMs
            }
            cancel.invoke(taskHandle);
        } catch (ReflectiveOperationException cancelError) {
            LOGGER.log(Level.WARNING, "Failed to cancel reflective scheduler task", cancelError);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof WrapperTask)) {
            return false;
        }
        WrapperTask other = (WrapperTask) obj;
        return java.util.Objects.equals(backend, other.backend) && java.util.Objects.equals(handle, other.handle) && java.util.Objects.equals(cancelAction, other.cancelAction);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(backend, handle, cancelAction);
    }

    @Override
    public String toString() {
        return "WrapperTask[backend=" + backend + ", handle=" + handle + ", cancelAction=" + cancelAction + "]";
    }
}
