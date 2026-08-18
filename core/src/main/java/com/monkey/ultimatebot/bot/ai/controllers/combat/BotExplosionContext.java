package com.monkey.ultimatebot.bot.ai.controllers.combat;

import com.monkey.ultimatebot.api.event.combat.BotExplosionEvent;
import com.monkey.ultimatebot.api.event.combat.BotExplosionType;
import com.monkey.ultimatebot.api.model.runtime.BotSnapshot;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import org.bukkit.Location;
import org.jspecify.annotations.Nullable;

public final class BotExplosionContext {
    private static final ThreadLocal<Integer> BLOCK_PROTECTION_DEPTH = ThreadLocal.withInitial(() -> 0);

    private BotExplosionContext() {}

    public static <T> T execute(boolean blockDamage, Supplier<T> action) {
        if (blockDamage) {
            return action.get();
        }

        BLOCK_PROTECTION_DEPTH.set(BLOCK_PROTECTION_DEPTH.get() + 1);
        try {
            return action.get();
        } finally {
            int remaining = BLOCK_PROTECTION_DEPTH.get() - 1;
            if (remaining <= 0) {
                BLOCK_PROTECTION_DEPTH.remove();
            } else {
                BLOCK_PROTECTION_DEPTH.set(remaining);
            }
        }
    }

    public static <T> T execute(
            @Nullable ITrainingBot bot,
            BotExplosionType type,
            Location location,
            boolean blockDamage,
            Function<Boolean, T> action,
            T cancelledResult) {
        boolean resolvedBlockDamage = blockDamage;
        if (bot != null && bot.getPlugin() != null) {
            UUID ownerUUID = bot.getPlugin().getBotRegistry().getOwnerUUIDByBotUUID(bot.getUniqueId());
            BotSnapshot snapshot = ownerUUID == null
                    ? null
                    : bot.getPlugin().getBotEventDispatcher().snapshot(ownerUUID, bot);
            if (snapshot != null) {
                BotExplosionEvent event = bot.getPlugin()
                        .getBotEventDispatcher()
                        .publish(new BotExplosionEvent(
                                bot.getPlugin().getBotEventDispatcher().nextSequence(bot.getUniqueId()),
                                snapshot,
                                type,
                                location,
                                blockDamage));
                if (event.isCancelled()) {
                    return cancelledResult;
                }
                resolvedBlockDamage = event.isBlockDamage();
            }
        }

        boolean finalBlockDamage = resolvedBlockDamage;
        return execute(finalBlockDamage, () -> action.apply(finalBlockDamage));
    }

    public static boolean isBlockDamageSuppressed() {
        return BLOCK_PROTECTION_DEPTH.get() > 0;
    }
}
