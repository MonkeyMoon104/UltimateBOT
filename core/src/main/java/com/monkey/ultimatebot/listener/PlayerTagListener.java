package com.monkey.ultimatebot.listener;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.bot.BotManager;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Soft-depends on CombatLogX: registered only when the plugin and its event class exist at runtime.
 * Uses reflection so UltimateBot can compile without the CombatLogX API on the classpath.
 */
public final class PlayerTagListener implements Listener {

    private static final String EVENT_CLASS = "com.github.sirblobman.combatlogx.api.event.PlayerPreTagEvent";

    private final BotManager botManager;
    private final Class<? extends Event> eventType;
    private final Method getPlayer;
    private final Method getEnemy;
    private final Method setCancelled;

    private PlayerTagListener(
            BotManager botManager,
            Class<? extends Event> eventType,
            Method getPlayer,
            Method getEnemy,
            Method setCancelled) {
        this.botManager = botManager;
        this.eventType = eventType;
        this.getPlayer = getPlayer;
        this.getEnemy = getEnemy;
        this.setCancelled = setCancelled;
    }

    public static PlayerTagListener create(UltimateBot plugin) throws ReflectiveOperationException {
        Class<? extends Event> eventType = Class.forName(EVENT_CLASS).asSubclass(Event.class);
        Method getPlayer = eventType.getMethod("getPlayer");
        Method getEnemy = eventType.getMethod("getEnemy");
        Method setCancelled = eventType.getMethod("setCancelled", boolean.class);
        return new PlayerTagListener(plugin.getBotManager(), eventType, getPlayer, getEnemy, setCancelled);
    }

    public void register(UltimateBot plugin) {
        EventExecutor executor = (listener, event) -> {
            try {
                handle(event);
            } catch (ReflectiveOperationException error) {
                throw new IllegalStateException("CombatLogX event bridge failed", error);
            }
        };
        plugin.getServer().getPluginManager().registerEvent(
                eventType,
                this,
                EventPriority.NORMAL,
                executor,
                plugin,
                false);
    }

    private void handle(Event event) throws ReflectiveOperationException {
        if (!eventType.isInstance(event)) {
            return;
        }
        Player player = (Player) getPlayer.invoke(event);
        ITrainingBot bot = botManager.getBot(player.getUniqueId());
        if (bot == null) {
            return;
        }
        Entity enemy = (Entity) getEnemy.invoke(event);
        UUID botId = bot.getUniqueId();
        if (enemy != null && enemy.getUniqueId().equals(botId)) {
            setCancelled.invoke(event, true);
        }
    }
}
