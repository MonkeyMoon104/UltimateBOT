package com.monkey.ultimatebot.extension.runtime;

import com.monkey.ultimatebot.api.extension.nativeaccess.NativeBotAccess;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.common.model.PlatformCapability;
import com.monkey.ultimatebot.nms.INMSBridge;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.jspecify.annotations.Nullable;

public final class CoreNativeBotAccess implements NativeBotAccess {
    private final String minecraftVersion;
    private final ITrainingBot bot;
    private final INMSBridge bridge;
    private @Nullable Object target;

    public CoreNativeBotAccess(String minecraftVersion, ITrainingBot bot, INMSBridge bridge) {
        this.minecraftVersion = Objects.requireNonNull(minecraftVersion, "minecraftVersion");
        this.bot = Objects.requireNonNull(bot, "bot");
        this.bridge = Objects.requireNonNull(bridge, "bridge");
    }

    public void target(org.bukkit.entity.@Nullable LivingEntity target) {
        if (target == null) {
            clearTarget();
            return;
        }
        this.target = requireHandle(target, "target");
    }

    public void clearTarget() {
        this.target = null;
    }

    @Override
    public String minecraftVersion() {
        return minecraftVersion;
    }

    @Override
    public Set<PlatformCapability> capabilities() {
        return bridge.capabilities();
    }

    @Override
    public <T> T requireBotHandle(Class<T> type) {
        return cast(type, botHandle(), "bot handle");
    }

    @Override
    public <T> Optional<T> targetHandle(Class<T> type) {
        Class<T> checkedType = Objects.requireNonNull(type, "type");
        Object current = target;
        return current == null || !checkedType.isInstance(current)
                ? Optional.empty()
                : Optional.of(checkedType.cast(current));
    }

    @Override
    public <T> T requireLevelHandle(Class<T> type) {
        return cast(type, resolveLevel(botHandle()), "level handle");
    }

    @Override
    public <T> T requireBridge(Class<T> type) {
        return cast(type, bridge, "NMS bridge");
    }

    private Object botHandle() {
        return requireHandle(bot.asBukkitPlayer(), "bot");
    }

    private static Object requireHandle(Object entity, String label) {
        try {
            Method getHandle = entity.getClass().getMethod("getHandle");
            Object handle = getHandle.invoke(entity);
            if (handle == null) {
                throw new IllegalStateException(label + " getHandle() returned null");
            }
            return handle;
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(label + " does not expose a CraftBukkit handle", e);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("failed to resolve " + label + " NMS handle", e);
        }
    }

    private static Object resolveLevel(Object nmsHandle) {
        Object level = invokeNoArg(nmsHandle, "level");
        if (level != null) {
            return level;
        }
        level = invokeNoArg(nmsHandle, "getLevel");
        if (level != null) {
            return level;
        }
        level = invokeNoArg(nmsHandle, "getWorld");
        if (level != null) {
            return level;
        }
        level = readField(nmsHandle, "level");
        if (level != null) {
            return level;
        }
        throw new IllegalStateException(
                "unable to resolve level handle from " + nmsHandle.getClass().getName());
    }

    private static @Nullable Object invokeNoArg(Object target, String methodName) {
        try {
            Method method = target.getClass().getMethod(methodName);
            return method.invoke(target);
        } catch (NoSuchMethodException ignored) {
            return null;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(
                    "failed to invoke " + methodName + " on "
                            + target.getClass().getName(),
                    e);
        }
    }

    private static @Nullable Object readField(Object target, String fieldName) {
        Class<?> type = target.getClass();
        while (type != null && type != Object.class) {
            try {
                Field field = type.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(target);
            } catch (NoSuchFieldException ignored) {
                type = type.getSuperclass();
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(
                        "failed to read field " + fieldName + " on "
                                + target.getClass().getName(),
                        e);
            }
        }
        return null;
    }

    private static <T> T cast(Class<T> type, Object value, String label) {
        Class<T> checkedType = Objects.requireNonNull(type, "type");
        if (!checkedType.isInstance(value)) {
            throw new IllegalStateException(
                    label + " is " + value.getClass().getName() + ", not " + checkedType.getName());
        }
        return checkedType.cast(value);
    }
}
