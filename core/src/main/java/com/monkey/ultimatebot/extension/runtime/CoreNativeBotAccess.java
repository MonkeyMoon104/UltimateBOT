package com.monkey.ultimatebot.extension.runtime;

import com.monkey.ultimatebot.api.extension.nativeaccess.NativeBotAccess;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.nms.INMSBridge;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.jspecify.annotations.Nullable;

public final class CoreNativeBotAccess implements NativeBotAccess {
    private final String minecraftVersion;
    private final ITrainingBot bot;
    private final INMSBridge bridge;
    private @Nullable LivingEntity target;

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
        if (!(target instanceof CraftLivingEntity craftTarget)) {
            throw new IllegalArgumentException("target does not expose a CraftBukkit handle");
        }
        this.target = craftTarget.getHandle();
    }

    public void clearTarget() {
        this.target = null;
    }

    @Override
    public String minecraftVersion() {
        return minecraftVersion;
    }

    @Override
    public <T> T requireBotHandle(Class<T> type) {
        return cast(type, botHandle(), "bot handle");
    }

    @Override
    public <T> Optional<T> targetHandle(Class<T> type) {
        Class<T> checkedType = Objects.requireNonNull(type, "type");
        LivingEntity current = target;
        return current == null || !checkedType.isInstance(current)
                ? Optional.empty()
                : Optional.of(checkedType.cast(current));
    }

    @Override
    public <T> T requireLevelHandle(Class<T> type) {
        return cast(type, botHandle().level(), "level handle");
    }

    @Override
    public <T> T requireBridge(Class<T> type) {
        return cast(type, bridge, "NMS bridge");
    }

    private static <T> T cast(Class<T> type, Object value, String label) {
        Class<T> checkedType = Objects.requireNonNull(type, "type");
        if (!checkedType.isInstance(value)) {
            throw new IllegalStateException(
                    label + " is " + value.getClass().getName() + ", not " + checkedType.getName());
        }
        return checkedType.cast(value);
    }

    private Player botHandle() {
        org.bukkit.entity.Player bukkitBot = bot.asBukkitPlayer();
        if (!(bukkitBot instanceof CraftPlayer craftBot)) {
            throw new IllegalStateException("bot does not expose a CraftBukkit handle");
        }
        return craftBot.getHandle();
    }
}
