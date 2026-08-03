package com.monkey.ultimatebot.listener;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.api.event.action.BotHealEvent;
import com.monkey.ultimatebot.api.event.action.BotTeleportEvent;
import com.monkey.ultimatebot.api.event.combat.BotDamageEvent;
import com.monkey.ultimatebot.api.event.combat.BotKillEntityEvent;
import com.monkey.ultimatebot.api.model.runtime.BotSnapshot;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.event.BotEventDispatcher;
import java.util.UUID;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

/** Bridges relevant Bukkit runtime actions to stable UltimateBot API events. */
public final class BotRuntimeEventListener implements Listener {
    private final UltimateBot plugin;
    private final BotEventDispatcher dispatcher;

    public BotRuntimeEventListener(UltimateBot plugin) {
        this.plugin = plugin;
        this.dispatcher = plugin.getBotEventDispatcher();
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    public void onDamage(EntityDamageEvent event) {
        BotContext context = findBot(event.getEntity().getUniqueId());
        if (context == null) return;
        Entity damager = event instanceof EntityDamageByEntityEvent byEntity ? byEntity.getDamager() : null;
        if (damager != null && context.bot.asPlayer().isBlocking()) {
            context.bot.getBotAI().recordShieldImpact(damager.getUniqueId());
        }
        BotDamageEvent botEvent = new BotDamageEvent(
                dispatcher.nextSequence(context.snapshot.botUUID()),
                context.snapshot,
                damager,
                event.getCause().name(),
                event.getDamage());
        botEvent.setCancelled(event.isCancelled());
        dispatcher.publish(botEvent);
        if (botEvent.isCancelled()) {
            event.setCancelled(true);
        } else {
            event.setDamage(botEvent.getDamage());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDeath(EntityDeathEvent event) {
        LivingEntity victim = event.getEntity();
        org.bukkit.entity.Player killer = victim.getKiller();
        if (killer == null) return;
        BotContext context = findBot(killer.getUniqueId());
        if (context == null) return;
        dispatcher.publish(
                new BotKillEntityEvent(dispatcher.nextSequence(context.snapshot.botUUID()), context.snapshot, victim));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onHeal(EntityRegainHealthEvent event) {
        BotContext context = findBot(event.getEntity().getUniqueId());
        if (context == null) return;
        BotHealEvent botEvent = dispatcher.publish(new BotHealEvent(
                dispatcher.nextSequence(context.snapshot.botUUID()),
                context.snapshot,
                event.getRegainReason().name(),
                event.getAmount()));
        if (botEvent.isCancelled()) {
            event.setCancelled(true);
        } else {
            event.setAmount(botEvent.getAmount());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        BotContext context = findBot(event.getPlayer().getUniqueId());
        if (context == null || event.getTo() == null) return;
        BotTeleportEvent botEvent = dispatcher.publish(new BotTeleportEvent(
                dispatcher.nextSequence(context.snapshot.botUUID()),
                context.snapshot,
                event.getFrom(),
                event.getTo(),
                event.getCause().name()));
        if (botEvent.isCancelled()) {
            event.setCancelled(true);
        } else {
            event.setTo(botEvent.getTo());
        }
    }

    private BotContext findBot(UUID botUUID) {
        UUID ownerUUID = plugin.getBotRegistry().getOwnerUUIDByBotUUID(botUUID);
        if (ownerUUID == null) return null;
        ITrainingBot bot = plugin.getBotRegistry().getBot(ownerUUID);
        BotSnapshot snapshot = dispatcher.snapshot(ownerUUID, bot);
        return snapshot == null ? null : new BotContext(bot, snapshot);
    }

    private record BotContext(ITrainingBot bot, BotSnapshot snapshot) {}
}
