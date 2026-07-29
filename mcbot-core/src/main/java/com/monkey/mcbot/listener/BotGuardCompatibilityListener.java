package com.monkey.mcbot.listener;

import com.monkey.mcbot.MinecraftBot;
import com.monkey.mcbot.bot.BotRegistry;
import com.monkey.mcbot.bot.ai.ITrainingBot;
import com.monkey.mcbot.bot.ai.controllers.attack.helper.AttackExecutor;
import com.monkey.mcbot.wrapper.WrapperTask;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public final class BotGuardCompatibilityListener implements Listener {

    private static final List<String> DEFAULT_CLASS_PREFIXES = List.of("com.monkey.mcbot.");
    private static final List<String> DEFAULT_CLASS_CONTAINS = List.of("mcbot");
    private static final List<String> DEFAULT_NAME_PREFIXES = List.of();

    private final MinecraftBot plugin;
    private final java.util.Set<UUID> discoveredBots = ConcurrentHashMap.newKeySet();
    private final java.util.Set<UUID> luckPermsQueued = ConcurrentHashMap.newKeySet();
    private final java.util.Set<UUID> luckPermsLoaded = ConcurrentHashMap.newKeySet();

    private WrapperTask scannerTask;
    private boolean scannerRunning;

    private boolean enabled;
    private boolean markAsNpc;
    private boolean preloadLuckPerms;
    private boolean clearFireTicks;
    private boolean cancelCombust;
    private boolean cancelFireDamage;
    private boolean debug;
    private long scanIntervalTicks;
    private String npcMetadataKey;
    private String ownMetadataKey;
    private List<String> classPrefixes;
    private List<String> classContains;
    private List<String> exactNames;
    private List<String> namePrefixes;

    public BotGuardCompatibilityListener(MinecraftBot plugin) {
        this.plugin = plugin;
        reloadLocalConfig();
    }

    public void reloadLocalConfig() {
        FileConfiguration config = plugin.getConfig();
        this.enabled = config.getBoolean("bot.guard.enabled", true);
        this.markAsNpc = config.getBoolean("bot.guard.compatibility.mark-as-npc-for-vault", true);
        this.preloadLuckPerms = config.getBoolean("bot.guard.compatibility.preload-luckperms-users", true);
        this.clearFireTicks = config.getBoolean("bot.guard.protection.clear-fire-ticks", true);
        this.cancelCombust = config.getBoolean("bot.guard.protection.cancel-combust-events", true);
        this.cancelFireDamage = config.getBoolean("bot.guard.protection.cancel-fire-damage", true);
        this.debug = config.getBoolean("bot.guard.debug", false);
        this.scanIntervalTicks = Math.max(5L, config.getLong("bot.guard.scanner.interval-ticks", 20L));
        this.npcMetadataKey = nonBlank(config.getString("bot.guard.compatibility.npc-metadata-key"), "NPC");
        this.ownMetadataKey = nonBlank(config.getString("bot.guard.compatibility.own-metadata-key"), "MinecraftBotGuard");
        this.classPrefixes = readList(config, "bot.guard.detection.class-prefixes", DEFAULT_CLASS_PREFIXES, true);
        this.classContains = readList(config, "bot.guard.detection.class-contains", DEFAULT_CLASS_CONTAINS, true);
        this.exactNames = readList(config, "bot.guard.detection.exact-names", defaultExactNames(config), false);
        this.namePrefixes = readList(config, "bot.guard.detection.name-prefixes", DEFAULT_NAME_PREFIXES, true);
    }

    public void startScanner() {
        stopScanner();
        if (!enabled || plugin.getWrapperManager() == null) {
            return;
        }

        scannerRunning = true;
        scheduleNextScan(1L);
    }

    public void stopScanner() {
        scannerRunning = false;
        if (scannerTask != null) {
            scannerTask.cancel();
            scannerTask = null;
        }
    }

    public void markBot(Entity entity) {
        if (!enabled || entity == null) {
            return;
        }

        UUID uuid = entity.getUniqueId();
        boolean newlyDiscovered = discoveredBots.add(uuid);
        entity.setMetadata(ownMetadataKey, new FixedMetadataValue(plugin, true));

        if (markAsNpc) {
            entity.setMetadata(npcMetadataKey, new FixedMetadataValue(plugin, true));
        }
        if (clearFireTicks) {
            entity.setFireTicks(0);
        }

        if (newlyDiscovered) {
            debug("detected bot " + entity.getName() + " (" + uuid + ")");
        }

        preloadLuckPerms(entity);
    }

    public void forgetBot(UUID uuid) {
        if (uuid == null) {
            return;
        }
        discoveredBots.remove(uuid);
        luckPermsLoaded.remove(uuid);
        luckPermsQueued.remove(uuid);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onEntitySpawn(EntitySpawnEvent event) {
        markIfTrainingBot(event.getEntity());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onPlayerJoin(PlayerJoinEvent event) {
        markIfTrainingBot(event.getPlayer());
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
        if (isTrainingBot(event.getEntity()) && event.getCombuster() instanceof org.bukkit.entity.Player) {
            markBotFireAspect(event.getEntity());
            return;
        }
        protectFromCombust(event.getEntity(), event);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onDamage(EntityDamageEvent event) {
        if (!enabled || !cancelFireDamage || !isProtectedFireCause(event.getCause())) {
            return;
        }

        Entity entity = event.getEntity();
        if (!isTrainingBot(entity)) {
            return;
        }

        if (hasActiveBotFireAspect(entity)) {
            return;
        }

        markBot(entity);
        if (clearFireTicks) {
            entity.setFireTicks(0);
        }
        event.setCancelled(true);
    }

    private void markIfTrainingBot(Entity entity) {
        if (isTrainingBot(entity)) {
            markBot(entity);
        }
    }

    private void protectFromCombust(Entity entity, EntityCombustEvent event) {
        if (!enabled || !isTrainingBot(entity)) {
            return;
        }

        if (hasActiveBotFireAspect(entity)) {
            return;
        }

        markBot(entity);
        if (clearFireTicks) {
            entity.setFireTicks(0);
        }
        if (cancelCombust) {
            event.setCancelled(true);
        }
    }

    private boolean isTrainingBot(Entity entity) {
        if (!enabled || entity == null) {
            return false;
        }
        if (hasTruthyMetadata(entity, ownMetadataKey)) {
            return true;
        }
        if (isRegisteredBotEntity(entity)) {
            return true;
        }

        String bukkitClassName = entity.getClass().getName().toLowerCase(Locale.ROOT);
        String handleClassName = getHandleClassName(entity).toLowerCase(Locale.ROOT);
        if (matchesClassDetection(bukkitClassName, handleClassName)) {
            return true;
        }

        String entityName = entity.getName();
        if (entityName == null) {
            return false;
        }

        for (String exactName : exactNames) {
            if (entityName.equalsIgnoreCase(exactName)) {
                return true;
            }
        }

        String lowerName = entityName.toLowerCase(Locale.ROOT);
        for (String namePrefix : namePrefixes) {
            if (lowerName.startsWith(namePrefix)) {
                return true;
            }
        }

        return false;
    }

    private void markBotFireAspect(Entity entity) {
        if (entity == null) {
            return;
        }
        entity.setMetadata(
                AttackExecutor.BOT_FIRE_ASPECT_METADATA,
                new FixedMetadataValue(plugin, System.currentTimeMillis() + 5500L)
        );
    }

    private boolean hasActiveBotFireAspect(Entity entity) {
        if (entity == null) {
            return false;
        }
        long now = System.currentTimeMillis();
        for (MetadataValue value : entity.getMetadata(AttackExecutor.BOT_FIRE_ASPECT_METADATA)) {
            if (value.getOwningPlugin() == plugin && value.asLong() > now) {
                return true;
            }
        }
        entity.removeMetadata(AttackExecutor.BOT_FIRE_ASPECT_METADATA, plugin);
        return false;
    }

    private boolean matchesClassDetection(String bukkitClassName, String handleClassName) {
        for (String classPrefix : classPrefixes) {
            if (bukkitClassName.startsWith(classPrefix) || handleClassName.startsWith(classPrefix)) {
                return true;
            }
        }

        for (String classPart : classContains) {
            if (bukkitClassName.contains(classPart) || handleClassName.contains(classPart)) {
                return true;
            }
        }

        return false;
    }

    private boolean isRegisteredBotEntity(Entity entity) {
        BotRegistry registry = plugin.getBotRegistry();
        if (registry == null) {
            return false;
        }

        UUID entityUuid = entity.getUniqueId();
        for (ITrainingBot bot : registry.getAllBots().values()) {
            if (bot != null && bot.asPlayer() != null && entityUuid.equals(bot.asPlayer().getUUID())) {
                return true;
            }
        }
        return false;
    }

    private void preloadLuckPerms(Entity entity) {
        if (!preloadLuckPerms) {
            return;
        }

        UUID uuid = entity.getUniqueId();
        if (luckPermsLoaded.contains(uuid) || !luckPermsQueued.add(uuid)) {
            return;
        }

        Plugin luckPermsPlugin = Bukkit.getPluginManager().getPlugin("LuckPerms");
        if (luckPermsPlugin == null || !luckPermsPlugin.isEnabled()) {
            luckPermsQueued.remove(uuid);
            return;
        }

        String entityName = entity.getName();
        try {
            CompletableFuture<?> loadFuture = loadLuckPermsUser(luckPermsPlugin, uuid);
            if (loadFuture == null) {
                luckPermsQueued.remove(uuid);
                return;
            }

            loadFuture.whenComplete((ignored, error) -> {
                luckPermsQueued.remove(uuid);
                if (error == null) {
                    luckPermsLoaded.add(uuid);
                    debug("LuckPerms user preloaded for " + entityName + " (" + uuid + ")");
                    return;
                }
                debug("LuckPerms preload failed for " + entityName + " (" + uuid + "): " + error.getMessage());
            });
        } catch (ReflectiveOperationException | LinkageError | IllegalStateException error) {
            luckPermsQueued.remove(uuid);
            debug("LuckPerms preload unavailable for " + entityName + " (" + uuid + "): " + error.getMessage());
        }
    }

    private CompletableFuture<?> loadLuckPermsUser(Plugin luckPermsPlugin, UUID uuid) throws ReflectiveOperationException {
        ClassLoader luckPermsClassLoader = luckPermsPlugin.getClass().getClassLoader();
        Class<?> providerClass = Class.forName("net.luckperms.api.LuckPermsProvider", true, luckPermsClassLoader);
        Object luckPerms = providerClass.getMethod("get").invoke(null);
        Object userManager = luckPerms.getClass().getMethod("getUserManager").invoke(luckPerms);
        Object loadFuture = userManager.getClass().getMethod("loadUser", UUID.class).invoke(userManager, uuid);
        return loadFuture instanceof CompletableFuture<?> completableFuture ? completableFuture : null;
    }

    private boolean hasTruthyMetadata(Entity entity, String metadataKey) {
        if (metadataKey == null || metadataKey.isBlank()) {
            return false;
        }

        for (MetadataValue value : entity.getMetadata(metadataKey)) {
            if (value.asBoolean()) {
                return true;
            }
        }
        return false;
    }

    private String getHandleClassName(Entity entity) {
        try {
            Method getHandle = entity.getClass().getMethod("getHandle");
            Object handle = getHandle.invoke(entity);
            return handle == null ? "" : handle.getClass().getName();
        } catch (ReflectiveOperationException ignored) {
            return "";
        }
    }

    private boolean isProtectedFireCause(EntityDamageEvent.DamageCause cause) {
        return cause == EntityDamageEvent.DamageCause.FIRE
                || cause == EntityDamageEvent.DamageCause.FIRE_TICK
                || cause == EntityDamageEvent.DamageCause.LAVA
                || cause == EntityDamageEvent.DamageCause.HOT_FLOOR;
    }

    private void scheduleNextScan(long delayTicks) {
        if (!scannerRunning || plugin.getWrapperManager() == null) {
            return;
        }

        scannerTask = plugin.getWrapperManager().active().runSyncLater(() -> {
            if (!scannerRunning) {
                return;
            }
            scanAllEntities();
            scheduleNextScan(scanIntervalTicks);
        }, delayTicks);
    }

    private void scanAllEntities() {
        if (!enabled) {
            return;
        }

        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                markIfTrainingBot(entity);
            }
        }
    }

    private List<String> readList(FileConfiguration config, String path, List<String> fallback, boolean lowerCase) {
        List<String> rawValues = config.contains(path) ? config.getStringList(path) : fallback;
        List<String> values = new ArrayList<>();
        for (String rawValue : rawValues) {
            if (rawValue == null || rawValue.isBlank()) {
                continue;
            }
            String value = rawValue.trim();
            values.add(lowerCase ? value.toLowerCase(Locale.ROOT) : value);
        }
        return values;
    }

    private List<String> defaultExactNames(FileConfiguration config) {
        List<String> names = new ArrayList<>();
        names.add("MinecraftBot");

        String configuredName = config.getString("bot.name");
        if (configuredName != null && !configuredName.isBlank() && names.stream().noneMatch(configuredName::equalsIgnoreCase)) {
            names.add(configuredName.trim());
        }
        return names;
    }

    private String nonBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private void debug(String message) {
        if (debug) {
            plugin.getLogger().info("[BotGuard] " + message);
        }
    }
}
