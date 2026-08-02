package com.monkey.ultimatebot.guard.addon;

import com.monkey.ultimatebot.common.guard.GuardBackend;
import com.monkey.ultimatebot.common.guard.GuardBackendContext;
import com.monkey.ultimatebot.common.guard.GuardMetadata;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustByBlockEvent;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.Nullable;

/** Protects only entities explicitly owned by UltimateBot or marked by this addon. */
final class PaperGuardBackend implements GuardBackend, Listener {
    private final Plugin plugin;
    private final Predicate<UUID> managedBotPredicate;
    private final Set<UUID> markedBots = ConcurrentHashMap.newKeySet();
    private final Set<UUID> luckPermsQueued = ConcurrentHashMap.newKeySet();
    private final Set<UUID> luckPermsLoaded = ConcurrentHashMap.newKeySet();
    private final Map<UUID, CompletableFuture<?>> luckPermsLoads = new ConcurrentHashMap<>();

    PaperGuardBackend(GuardBackendContext context) {
        this.plugin = requirePlugin(context.pluginHandle());
        this.managedBotPredicate = context.managedBotPredicate();
        context.listenerRegistrar().accept(this);
    }

    @Override
    public void markBot(Object entityHandle) {
        if (!(entityHandle instanceof Entity entity)) {
            return;
        }

        UUID uuid = entity.getUniqueId();
        markedBots.add(uuid);
        entity.setMetadata(GuardMetadata.MANAGED_BOT, new FixedMetadataValue(plugin, true));
        entity.setMetadata(GuardMetadata.NPC, new FixedMetadataValue(plugin, true));
        entity.setFireTicks(0);
        preloadLuckPerms(entity);
    }

    @Override
    public void forgetBot(UUID entityUuid) {
        if (entityUuid == null) {
            return;
        }
        markedBots.remove(entityUuid);
        luckPermsLoaded.remove(entityUuid);
        luckPermsQueued.remove(entityUuid);
        CompletableFuture<?> load = luckPermsLoads.remove(entityUuid);
        if (load != null) {
            load.cancel(false);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onEntitySpawn(EntitySpawnEvent event) {
        markIfManaged(event.getEntity());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onPlayerJoin(PlayerJoinEvent event) {
        markIfManaged(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onCombust(EntityCombustEvent event) {
        protectFromCombust(event.getEntity(), event);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onCombustByBlock(EntityCombustByBlockEvent event) {
        protectFromCombust(event.getEntity(), event);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onCombustByEntity(EntityCombustByEntityEvent event) {
        protectFromCombust(event.getEntity(), event);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onDamage(EntityDamageEvent event) {
        if (!isProtectedFireCause(event.getCause()) || !isManaged(event.getEntity())) {
            return;
        }
        if (hasActiveBotFireAspect(event.getEntity())) {
            return;
        }
        markBot(event.getEntity());
        event.setCancelled(true);
    }

    private void protectFromCombust(Entity entity, EntityCombustEvent event) {
        if (!isManaged(entity) || hasActiveBotFireAspect(entity)) {
            return;
        }
        markBot(entity);
        event.setCancelled(true);
    }

    private void markIfManaged(Entity entity) {
        if (isManaged(entity)) {
            markBot(entity);
        }
    }

    private boolean isManaged(Entity entity) {
        UUID uuid = entity.getUniqueId();
        return markedBots.contains(uuid)
                || hasTruthyMetadata(entity, GuardMetadata.MANAGED_BOT)
                || managedBotPredicate.test(uuid);
    }

    private boolean hasActiveBotFireAspect(Entity entity) {
        long now = System.currentTimeMillis();
        for (MetadataValue value : entity.getMetadata(GuardMetadata.FIRE_ASPECT_UNTIL)) {
            if (Objects.equals(value.getOwningPlugin(), plugin) && value.asLong() > now) {
                return true;
            }
        }
        entity.removeMetadata(GuardMetadata.FIRE_ASPECT_UNTIL, plugin);
        return false;
    }

    private void preloadLuckPerms(Entity entity) {
        UUID uuid = entity.getUniqueId();
        if (luckPermsLoaded.contains(uuid) || !luckPermsQueued.add(uuid)) {
            return;
        }

        Plugin luckPermsPlugin = Bukkit.getPluginManager().getPlugin("LuckPerms");
        if (luckPermsPlugin == null || !luckPermsPlugin.isEnabled()) {
            luckPermsQueued.remove(uuid);
            return;
        }

        try {
            CompletableFuture<?> loadFuture = loadLuckPermsUser(luckPermsPlugin, uuid);
            if (loadFuture == null) {
                luckPermsQueued.remove(uuid);
                return;
            }
            CompletableFuture<?> completion = loadFuture.whenComplete((ignored, error) -> {
                luckPermsQueued.remove(uuid);
                luckPermsLoads.remove(uuid);
                if (error == null) {
                    luckPermsLoaded.add(uuid);
                }
            });
            luckPermsLoads.put(uuid, completion);
        } catch (ReflectiveOperationException | LinkageError | IllegalStateException error) {
            luckPermsQueued.remove(uuid);
        }
    }

    private static @Nullable CompletableFuture<?> loadLuckPermsUser(Plugin luckPermsPlugin, UUID uuid)
            throws ReflectiveOperationException {
        ClassLoader classLoader = luckPermsPlugin.getClass().getClassLoader();
        Class<?> providerClass = Class.forName("net.luckperms.api.LuckPermsProvider", true, classLoader);
        Object luckPerms = providerClass.getMethod("get").invoke(null);
        Object userManager = luckPerms.getClass().getMethod("getUserManager").invoke(luckPerms);
        Object future = userManager.getClass().getMethod("loadUser", UUID.class).invoke(userManager, uuid);
        return future instanceof CompletableFuture<?> completableFuture ? completableFuture : null;
    }

    private static boolean hasTruthyMetadata(Entity entity, String metadataKey) {
        return entity.getMetadata(metadataKey).stream().anyMatch(MetadataValue::asBoolean);
    }

    private static boolean isProtectedFireCause(EntityDamageEvent.DamageCause cause) {
        return cause == EntityDamageEvent.DamageCause.FIRE
                || cause == EntityDamageEvent.DamageCause.FIRE_TICK
                || cause == EntityDamageEvent.DamageCause.LAVA
                || cause == EntityDamageEvent.DamageCause.HOT_FLOOR;
    }

    private static Plugin requirePlugin(Object pluginHandle) {
        if (pluginHandle instanceof Plugin plugin) {
            return plugin;
        }
        throw new IllegalArgumentException("Guard addon requires a Bukkit Plugin handle");
    }

    @Override
    public void close() {
        HandlerList.unregisterAll(this);
        luckPermsLoads.values().forEach(future -> future.cancel(false));
        luckPermsLoads.clear();
        luckPermsQueued.clear();
        luckPermsLoaded.clear();
        markedBots.clear();
    }
}
