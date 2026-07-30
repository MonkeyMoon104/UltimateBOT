package com.monkey.mcbot.wrapper.folia;

import com.monkey.mcbot.wrapper.PlatformWrapper;
import com.monkey.mcbot.wrapper.WrapperCapabilities;
import com.monkey.mcbot.wrapper.WrapperTask;
import com.monkey.mcbot.wrapper.WrapperType;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public final class FoliaWrapper implements PlatformWrapper {

    private final JavaPlugin plugin;
    private final WrapperCapabilities capabilities;

    private final Object globalRegionScheduler;
    private final Object asyncScheduler;
    private final Method entitySchedulerGetter;

    private final Method globalRunMethod;
    private final Method globalRunDelayedMethod;
    private final Method asyncRunNowMethod;
    private final Method asyncRunDelayedMethod;
    private final Method asyncRunAtFixedRateMethod;
    private final Method entityRunMethod;
    private final Method entityRunDelayedMethod;

    public FoliaWrapper(JavaPlugin plugin) {
        this.plugin = plugin;

        Server server = Bukkit.getServer();
        this.globalRegionScheduler = invokeNoArgs(server, "getGlobalRegionScheduler");
        this.asyncScheduler = invokeNoArgs(server, "getAsyncScheduler");
        this.entitySchedulerGetter = findMethod(Entity.class, "getScheduler", 0);

        this.globalRunMethod =
                globalRegionScheduler == null ? null : findMethod(globalRegionScheduler.getClass(), "run", 2);
        this.globalRunDelayedMethod =
                globalRegionScheduler == null ? null : findMethod(globalRegionScheduler.getClass(), "runDelayed", 3);

        this.asyncRunNowMethod = asyncScheduler == null ? null : findMethod(asyncScheduler.getClass(), "runNow", 2);
        this.asyncRunDelayedMethod =
                asyncScheduler == null ? null : findMethod(asyncScheduler.getClass(), "runDelayed", 4);
        this.asyncRunAtFixedRateMethod =
                asyncScheduler == null ? null : findMethod(asyncScheduler.getClass(), "runAtFixedRate", 5);

        Method resolvedEntityRun = null;
        Method resolvedEntityRunDelayed = null;
        if (entitySchedulerGetter != null) {
            Class<?> schedulerType = entitySchedulerGetter.getReturnType();
            resolvedEntityRun = findMethod(schedulerType, "run", 3);
            resolvedEntityRunDelayed = findMethod(schedulerType, "runDelayed", 4);
        }
        this.entityRunMethod = resolvedEntityRun;
        this.entityRunDelayedMethod = resolvedEntityRunDelayed;

        boolean foliaDetected =
                globalRegionScheduler != null || asyncScheduler != null || entitySchedulerGetter != null;
        boolean hasGlobal = globalRunMethod != null || globalRunDelayedMethod != null;
        boolean hasAsync =
                asyncRunNowMethod != null || asyncRunDelayedMethod != null || asyncRunAtFixedRateMethod != null;
        boolean hasEntity =
                entitySchedulerGetter != null && (entityRunMethod != null || entityRunDelayedMethod != null);

        this.capabilities = new WrapperCapabilities(foliaDetected, hasGlobal, hasAsync, hasEntity);
    }

    @Override
    public WrapperType type() {
        return WrapperType.FOLIA;
    }

    @Override
    public WrapperCapabilities capabilities() {
        return capabilities;
    }

    @Override
    public WrapperTask runSync(Runnable task) {
        if (capabilities.globalRegionScheduler()) {
            WrapperTask foliaTask = runGlobal(task, 0L);
            if (foliaTask.cancelAction() != null) {
                return foliaTask;
            }
        }

        BukkitScheduler scheduler = Bukkit.getScheduler();
        return WrapperTask.bukkit("bukkit-sync", scheduler.runTask(plugin, wrap(task)));
    }

    @Override
    public WrapperTask runSyncLater(Runnable task, long delayTicks) {
        if (capabilities.globalRegionScheduler()) {
            WrapperTask foliaTask = runGlobal(task, delayTicks);
            if (foliaTask.cancelAction() != null) {
                return foliaTask;
            }
        }

        BukkitScheduler scheduler = Bukkit.getScheduler();
        return WrapperTask.bukkit(
                "bukkit-sync-later", scheduler.runTaskLater(plugin, wrap(task), Math.max(1L, delayTicks)));
    }

    @Override
    public WrapperTask runEntity(Player player, Runnable task) {
        return runEntityLater(player, 0L, task);
    }

    @Override
    public WrapperTask runEntityLater(Player player, long delayTicks, Runnable task) {
        if (player != null && capabilities.entityScheduler()) {
            try {
                Object entityScheduler = entitySchedulerGetter.invoke(player);
                if (entityScheduler != null) {
                    Consumer<Object> taskConsumer = ignored -> wrap(task).run();
                    Runnable retired = () -> {};

                    if (delayTicks <= 0L && entityRunMethod != null) {
                        Object handle = entityRunMethod.invoke(entityScheduler, plugin, taskConsumer, retired);
                        return WrapperTask.reflective("folia-entity-run", handle);
                    }
                    if (entityRunDelayedMethod != null) {
                        Object handle = entityRunDelayedMethod.invoke(
                                entityScheduler, plugin, taskConsumer, retired, Math.max(1L, delayTicks));
                        return WrapperTask.reflective("folia-entity-delayed", handle);
                    }
                }
            } catch (Exception ex) {
                logWrapperFailure("entity scheduler", ex);
            }
        }

        return runSyncLater(task, delayTicks);
    }

    @Override
    public WrapperTask runAsync(Runnable task) {
        if (capabilities.asyncScheduler() && asyncRunNowMethod != null) {
            try {
                Object handle = asyncRunNowMethod.invoke(asyncScheduler, plugin, (Consumer<Object>)
                        ignored -> wrap(task).run());
                return WrapperTask.reflective("folia-async-run", handle);
            } catch (Exception ex) {
                logWrapperFailure("async scheduler runNow", ex);
            }
        }

        BukkitScheduler scheduler = Bukkit.getScheduler();
        return WrapperTask.bukkit("bukkit-async", scheduler.runTaskAsynchronously(plugin, wrap(task)));
    }

    @Override
    public WrapperTask runAsyncLater(Runnable task, long delayTicks) {
        if (capabilities.asyncScheduler() && asyncRunDelayedMethod != null) {
            try {
                Object handle = invokeAsyncMethod(asyncRunDelayedMethod, task, delayTicks, null);
                return WrapperTask.reflective("folia-async-delayed", handle);
            } catch (Exception ex) {
                logWrapperFailure("async scheduler runDelayed", ex);
            }
        }

        BukkitScheduler scheduler = Bukkit.getScheduler();
        return WrapperTask.bukkit(
                "bukkit-async-later",
                scheduler.runTaskLaterAsynchronously(plugin, wrap(task), Math.max(1L, delayTicks)));
    }

    @Override
    public WrapperTask runAsyncRepeating(Runnable task, long delayTicks, long periodTicks) {
        if (capabilities.asyncScheduler() && asyncRunAtFixedRateMethod != null) {
            try {
                Object handle =
                        invokeAsyncMethod(asyncRunAtFixedRateMethod, task, delayTicks, Math.max(1L, periodTicks));
                return WrapperTask.reflective("folia-async-repeating", handle);
            } catch (Exception ex) {
                logWrapperFailure("async scheduler runAtFixedRate", ex);
            }
        }

        BukkitScheduler scheduler = Bukkit.getScheduler();
        return WrapperTask.bukkit(
                "bukkit-async-repeating",
                scheduler.runTaskTimerAsynchronously(
                        plugin, wrap(task), Math.max(1L, delayTicks), Math.max(1L, periodTicks)));
    }

    private WrapperTask runGlobal(Runnable task, long delayTicks) {
        try {
            Consumer<Object> taskConsumer = ignored -> wrap(task).run();
            if (delayTicks <= 0L && globalRunMethod != null) {
                Object handle = globalRunMethod.invoke(globalRegionScheduler, plugin, taskConsumer);
                return WrapperTask.reflective("folia-global-run", handle);
            }
            if (globalRunDelayedMethod != null) {
                Object handle = globalRunDelayedMethod.invoke(
                        globalRegionScheduler, plugin, taskConsumer, Math.max(1L, delayTicks));
                return WrapperTask.reflective("folia-global-delayed", handle);
            }
        } catch (Exception ex) {
            logWrapperFailure("global region scheduler", ex);
        }
        return WrapperTask.none("folia-global-unavailable");
    }

    private Object invokeAsyncMethod(Method method, Runnable task, long delayTicks, Long periodTicks) throws Exception {
        Consumer<Object> taskConsumer = ignored -> wrap(task).run();
        Class<?>[] parameterTypes = method.getParameterTypes();
        Object[] args = new Object[parameterTypes.length];

        args[0] = plugin;
        args[1] = taskConsumer;

        int durationIndex = 0;
        for (int i = 2; i < parameterTypes.length; i++) {
            Class<?> parameterType = parameterTypes[i];
            if (parameterType == TimeUnit.class) {
                args[i] = TimeUnit.MILLISECONDS;
                continue;
            }
            if (parameterType == Runnable.class) {
                args[i] = (Runnable) () -> {};
                continue;
            }

            long ticksValue = durationIndex == 0
                    ? Math.max(0L, delayTicks)
                    : Math.max(1L, periodTicks == null ? 1L : periodTicks);
            long millisValue = ticksToMillis(ticksValue);

            if (parameterType == long.class || parameterType == Long.class) {
                args[i] = millisValue;
                durationIndex++;
                continue;
            }
            if (parameterType == int.class || parameterType == Integer.class) {
                args[i] = (int) Math.min(Integer.MAX_VALUE, millisValue);
                durationIndex++;
                continue;
            }
            if ("java.time.Duration".equals(parameterType.getName())) {
                args[i] = Duration.ofMillis(millisValue);
                durationIndex++;
                continue;
            }

            throw new IllegalStateException("Unsupported async scheduler argument type: " + parameterType.getName());
        }

        return method.invoke(asyncScheduler, args);
    }

    private Runnable wrap(Runnable delegate) {
        return () -> {
            try {
                delegate.run();
            } catch (Throwable error) {
                logWrapperFailure("scheduled task", error);
            }
        };
    }

    private void logWrapperFailure(String context, Throwable error) {
        plugin.getLogger().log(Level.WARNING, "[Wrapper/Folia] " + context + " failed: " + error.getMessage(), error);
    }

    private static long ticksToMillis(long ticks) {
        return Math.max(0L, ticks) * 50L;
    }

    private static Object invokeNoArgs(Object target, String methodName) {
        if (target == null) {
            return null;
        }
        try {
            Method method = target.getClass().getMethod(methodName);
            return method.invoke(target);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static Method findMethod(Class<?> owner, String methodName, int parameterCount) {
        if (owner == null) {
            return null;
        }
        for (Method method : owner.getMethods()) {
            if (!method.getName().equals(methodName)) {
                continue;
            }
            if (method.getParameterCount() != parameterCount) {
                continue;
            }
            return method;
        }
        return null;
    }
}
