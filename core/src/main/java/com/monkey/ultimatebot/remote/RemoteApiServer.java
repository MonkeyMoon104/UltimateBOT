package com.monkey.ultimatebot.remote;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.api.UltimateBotAPI;
import com.monkey.ultimatebot.api.event.base.BotEventSource;
import com.monkey.ultimatebot.api.managers.IBotManager;
import com.monkey.ultimatebot.api.model.configuration.BotArmorType;
import com.monkey.ultimatebot.api.model.configuration.BotEquipmentSlot;
import com.monkey.ultimatebot.api.model.configuration.BotEquipmentSlotMode;
import com.monkey.ultimatebot.api.model.configuration.BotEquipmentSlotSetting;
import com.monkey.ultimatebot.api.model.configuration.BotMode;
import com.monkey.ultimatebot.api.model.configuration.BotSettings;
import com.monkey.ultimatebot.api.model.configuration.DifficultyLevel;
import com.monkey.ultimatebot.api.model.runtime.BotLocation;
import com.monkey.ultimatebot.api.model.runtime.BotOperationResult;
import com.monkey.ultimatebot.api.model.runtime.BotSnapshot;
import com.monkey.ultimatebot.api.model.runtime.BotSpawnRequest;
import com.monkey.ultimatebot.common.model.CombatMode;
import com.monkey.ultimatebot.common.model.CombatTuning;
import com.monkey.ultimatebot.common.util.EnumValues;
import com.monkey.ultimatebot.event.BotEventSourceContext;
import com.monkey.ultimatebot.metrics.BotMetrics;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executors;
import org.bukkit.Bukkit;
import org.jspecify.annotations.Nullable;

public final class RemoteApiServer {

    private static final String DEFAULT_BASE_PATH = "/ultimatebot/api/v1";
    private static final String DEFAULT_HOST = "127.0.0.1";
    private static final int DEFAULT_PORT = 8765;

    private final UltimateBot plugin;
    private final UltimateBotAPI api;
    private final ObjectMapper objectMapper;
    private @Nullable HttpServer server;
    private String token = "";
    private String basePath = DEFAULT_BASE_PATH;
    private @Nullable RemoteEventStream eventStream;

    public RemoteApiServer(UltimateBot plugin, UltimateBotAPI api) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.api = Objects.requireNonNull(api, "api");
        this.objectMapper = new ObjectMapper()
                .findAndRegisterModules()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public void start() {
        if (!plugin.getConfig().getBoolean("remote-api.enabled", false)) {
            return;
        }

        token = plugin.getConfig().getString("remote-api.token", "");
        if (token == null || token.isBlank() || "change-me".equalsIgnoreCase(token)) {
            plugin.getLogger()
                    .warning(
                            "Remote API is enabled but remote-api.token is not configured. Remote API will not start.");
            return;
        }

        String host = plugin.getConfig().getString("remote-api.host", DEFAULT_HOST);
        int port = plugin.getConfig().getInt("remote-api.port", DEFAULT_PORT);
        basePath = normalizeBasePath(plugin.getConfig().getString("remote-api.base-path", DEFAULT_BASE_PATH));

        try {
            HttpServer createdServer = HttpServer.create(new InetSocketAddress(host, port), 0);
            RemoteEventStream createdEventStream = new RemoteEventStream(plugin, objectMapper);
            server = createdServer;
            eventStream = createdEventStream;
            createdServer.createContext(basePath, this::handle);
            createdServer.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
            createdServer.start();
            plugin.getLogger().info("Remote API started on http://" + host + ":" + port + basePath);
        } catch (IOException ex) {
            if (eventStream != null) {
                eventStream.close();
                eventStream = null;
            }
            plugin.getLogger().warning("Remote API failed to start: " + ex.getMessage());
            server = null;
        }
    }

    public void stop() {
        if (eventStream != null) {
            eventStream.close();
            eventStream = null;
        }
        if (server != null) {
            server.stop(0);
            server = null;
            plugin.getLogger().info("Remote API stopped.");
        }
    }

    private void handle(HttpExchange exchange) throws IOException {
        long startedNanos = System.nanoTime();
        String method = exchange.getRequestMethod();
        String metricRoute = metricRoute(relativePath(exchange));
        BotMetrics metrics = plugin.getBotMetrics();
        try {
            if (!isAuthorized(exchange)) {
                writeJson(exchange, 401, RemoteOperationResponse.failure("Unauthorized"));
                return;
            }

            String relativePath = relativePath(exchange);
            if ("GET".equals(method) && "/health".equals(relativePath)) {
                writeJson(exchange, 200, RemoteOperationResponse.success("UltimateBot remote API is online.", null));
                return;
            }

            if ("GET".equals(method) && "/metrics".equals(relativePath)) {
                if (!metrics.isPrometheusEndpointEnabled()) {
                    writeJson(exchange, 404, RemoteOperationResponse.failure("Metrics endpoint is disabled."));
                } else {
                    writeText(exchange, 200, metrics.scrape());
                }
                return;
            }

            if ("GET".equals(method) && "/events".equals(relativePath)) {
                RemoteEventStream activeEventStream = eventStream;
                if (activeEventStream == null) {
                    writeJson(exchange, 503, RemoteOperationResponse.failure("Event stream is unavailable."));
                    return;
                }
                try {
                    activeEventStream.handle(exchange);
                } catch (IOException disconnected) {
                    return;
                }
                return;
            }

            if ("GET".equals(method) && "/bots".equals(relativePath)) {
                writeJson(exchange, 200, api.getBotRegistry().getAllBots().values());
                return;
            }

            if ("GET".equals(method) && "/bots/count".equals(relativePath)) {
                writeJson(exchange, 200, Map.of("count", api.getBotManager().getActiveBotCount()));
                return;
            }

            if ("POST".equals(method) && "/bots/event".equals(relativePath)) {
                handleEventSpawn(exchange);
                return;
            }

            if ("DELETE".equals(method) && "/bots".equals(relativePath)) {
                int removed = runSync(() -> api.getBotManager().removeAll());
                writeJson(exchange, 200, RemoteOperationResponse.removed("Removed all bots.", removed));
                return;
            }

            if (relativePath.startsWith("/bots/")) {
                handleBotMutation(exchange, method, relativePath);
                return;
            }

            writeJson(exchange, 404, RemoteOperationResponse.failure("Endpoint not found."));
        } catch (IllegalArgumentException ex) {
            writeJson(
                    exchange,
                    400,
                    RemoteOperationResponse.failure(
                            Objects.requireNonNullElse(ex.getMessage(), "Invalid remote API request")));
        } catch (Exception ex) {
            plugin.getLogger().warning("Remote API request failed: " + ex.getMessage());
            writeJson(exchange, 500, RemoteOperationResponse.failure("Internal remote API error."));
        } finally {
            metrics.recordRemoteRequest(
                    method, metricRoute, exchange.getResponseCode(), System.nanoTime() - startedNanos);
            exchange.close();
        }
    }

    private void handleEventSpawn(HttpExchange exchange) throws IOException {
        EventBotSpawnPayload payload = readJson(exchange, EventBotSpawnPayload.class);
        if (payload == null) {
            payload = new EventBotSpawnPayload();
        }
        BotSettings settings = buildSettings(payload);
        BotSpawnRequest request = BotSpawnRequest.builder(BotMode.EVENT)
                .botUUID(payload.botUUID)
                .owner(payload.ownerUUID)
                .targets(payload.targetUUIDs)
                .equipmentSlots(toEquipmentSettings(payload.equipmentSlots))
                .settings(settings)
                .build();
        BotOperationResult result = runSync(() -> api.getBotManager().spawn(request));
        writeJson(exchange, result.success() ? 200 : 400, RemoteOperationResponse.from(result));
    }

    private void handleBotMutation(HttpExchange exchange, String method, String relativePath) throws IOException {
        String[] parts = relativePath.substring(1).split("/", -1);
        if (parts.length < 2 || !"bots".equals(parts[0])) {
            writeJson(exchange, 404, RemoteOperationResponse.failure("Endpoint not found."));
            return;
        }

        UUID requestedUUID = UUID.fromString(parts[1]);
        IBotManager manager = api.getBotManager();
        UUID ownerUUID = resolveOwnerUUID(manager, requestedUUID);

        if ("GET".equals(method) && parts.length == 2) {
            BotSnapshot snapshot = runSync(() -> ownerUUID != null
                    ? manager.getBot(ownerUUID).orElse(null)
                    : manager.getBotByBotUUID(requestedUUID).orElse(null));
            if (snapshot == null) {
                writeJson(exchange, 404, RemoteOperationResponse.failure("Bot not found."));
            } else {
                writeJson(exchange, 200, snapshot);
            }
            return;
        }

        if ("DELETE".equals(method) && parts.length == 2) {
            boolean removed = runSync(
                    () -> ownerUUID != null ? manager.remove(ownerUUID) : manager.removeByBotUUID(requestedUUID));
            writeJson(
                    exchange,
                    removed ? 200 : 404,
                    new RemoteOperationResponse(
                            removed, removed ? "Bot removed." : "Bot not found.", null, removed ? 1 : 0));
            return;
        }

        if ("PUT".equals(method) && parts.length == 4 && "equipment".equals(parts[2])) {
            BotEquipmentSlot slot = EnumValues.parse(BotEquipmentSlot.class, parts[3], null);
            EquipmentSlotPayload payload = readJson(exchange, EquipmentSlotPayload.class);
            BotEquipmentSlotSetting setting = toEquipmentSetting(payload);
            boolean updated = slot != null
                    && setting != null
                    && runSync(() -> ownerUUID != null
                            ? manager.updateEquipmentSlot(ownerUUID, slot, setting)
                            : manager.updateEquipmentSlotByBotUUID(requestedUUID, slot, setting));
            BotSnapshot snapshot = runSync(() -> ownerUUID != null
                    ? manager.getBot(ownerUUID).orElse(null)
                    : manager.getBotByBotUUID(requestedUUID).orElse(null));
            writeJson(
                    exchange,
                    updated ? 200 : 400,
                    new RemoteOperationResponse(
                            updated,
                            updated ? "Equipment slot updated." : "Invalid equipment slot setting.",
                            snapshot,
                            null));
            return;
        }

        if ("DELETE".equals(method) && parts.length == 3 && "combat-tuning".equals(parts[2])) {
            boolean updated = runSync(() -> ownerUUID != null
                    ? manager.resetCombatTuning(ownerUUID)
                    : manager.resetCombatTuningByBotUUID(requestedUUID));
            writeMutationResult(exchange, manager, requestedUUID, ownerUUID, updated, "Combat tuning reset failed.");
            return;
        }

        if (!"PATCH".equals(method) || parts.length != 3) {
            writeJson(exchange, 404, RemoteOperationResponse.failure("Endpoint not found."));
            return;
        }

        if ("target-mode".equals(parts[2])) {
            TargetModePayload payload = readJson(exchange, TargetModePayload.class);
            com.monkey.ultimatebot.api.model.configuration.BotTargetMode mode = payload == null
                    ? null
                    : EnumValues.parse(
                            com.monkey.ultimatebot.api.model.configuration.BotTargetMode.class,
                            payload.targetMode,
                            null);
            boolean updated = mode != null
                    && runSync(() -> ownerUUID != null
                            ? manager.updateTargetMode(ownerUUID, mode)
                            : manager.updateTargetModeByBotUUID(requestedUUID, mode));
            BotSnapshot snapshot = runSync(() -> ownerUUID != null
                    ? manager.getBot(ownerUUID).orElse(null)
                    : manager.getBotByBotUUID(requestedUUID).orElse(null));
            writeJson(
                    exchange,
                    updated ? 200 : 400,
                    new RemoteOperationResponse(
                            updated, updated ? "Bot updated." : "Invalid target mode.", snapshot, null));
            return;
        }

        if ("combat-mode".equals(parts[2])) {
            CombatModePayload payload = readJson(exchange, CombatModePayload.class);
            CombatMode mode = payload == null ? null : EnumValues.parse(CombatMode.class, payload.combatMode, null);
            boolean updated = mode != null
                    && runSync(() -> ownerUUID != null
                            ? manager.updateCombatMode(ownerUUID, mode)
                            : manager.updateCombatModeByBotUUID(requestedUUID, mode));
            writeMutationResult(exchange, manager, requestedUUID, ownerUUID, updated, "Invalid combat mode.");
            return;
        }

        if ("combat-tuning".equals(parts[2])) {
            CombatTuning tuning = readJson(exchange, CombatTuning.class);
            boolean updated = tuning != null
                    && runSync(() -> ownerUUID != null
                            ? manager.updateCombatTuning(ownerUUID, tuning)
                            : manager.updateCombatTuningByBotUUID(requestedUUID, tuning));
            writeMutationResult(exchange, manager, requestedUUID, ownerUUID, updated, "Invalid combat tuning.");
            return;
        }

        TogglePayload payload = readJson(exchange, TogglePayload.class);
        boolean enabled = payload != null && Boolean.TRUE.equals(payload.enabled);
        boolean updated =
                switch (parts[2]) {
                    case "crystal-pvp" ->
                        runSync(() -> ownerUUID != null
                                ? manager.updateCrystalPvp(ownerUUID, enabled)
                                : manager.updateCrystalPvpByBotUUID(requestedUUID, enabled));
                    case "explosions" ->
                        runSync(() -> ownerUUID != null
                                ? manager.updateExplosions(ownerUUID, enabled)
                                : manager.updateExplosionsByBotUUID(requestedUUID, enabled));
                    case "explosion-block-damage" ->
                        runSync(() -> ownerUUID != null
                                ? manager.updateExplosionBlockDamage(ownerUUID, enabled)
                                : manager.updateExplosionBlockDamageByBotUUID(requestedUUID, enabled));
                    case "ender-pearls" ->
                        runSync(() -> ownerUUID != null
                                ? manager.updateEnderPearls(ownerUUID, enabled)
                                : manager.updateEnderPearlsByBotUUID(requestedUUID, enabled));
                    case "healing" ->
                        runSync(() -> ownerUUID != null
                                ? manager.updateHealing(ownerUUID, enabled)
                                : manager.updateHealingByBotUUID(requestedUUID, enabled));
                    case "attack-bots" ->
                        runSync(() -> ownerUUID != null
                                ? manager.updateAttackBots(ownerUUID, enabled)
                                : manager.updateAttackBotsByBotUUID(requestedUUID, enabled));
                    default -> false;
                };
        BotSnapshot snapshot = runSync(() -> ownerUUID != null
                ? manager.getBot(ownerUUID).orElse(null)
                : manager.getBotByBotUUID(requestedUUID).orElse(null));
        writeJson(
                exchange,
                updated ? 200 : 400,
                new RemoteOperationResponse(updated, updated ? "Bot updated." : "Bot update failed.", snapshot, null));
    }

    private void writeMutationResult(
            HttpExchange exchange,
            IBotManager manager,
            UUID requestedUUID,
            @Nullable UUID ownerUUID,
            boolean updated,
            String failureMessage)
            throws IOException {
        BotSnapshot snapshot = runSync(() -> ownerUUID != null
                ? manager.getBot(ownerUUID).orElse(null)
                : manager.getBotByBotUUID(requestedUUID).orElse(null));
        writeJson(
                exchange,
                updated ? 200 : 400,
                new RemoteOperationResponse(updated, updated ? "Bot updated." : failureMessage, snapshot, null));
    }

    private BotSettings buildSettings(EventBotSpawnPayload payload) {
        EventBotSpawnPayload safe = payload == null ? new EventBotSpawnPayload() : payload;
        BotSettings.BuildStep buildStep = applySkin(
                        BotSettings.builder()
                                .setBotNameTemplate(defaultString(
                                        safe.botNameTemplate, plugin.getConfig().getString("bot.name", "UltimateBot"))),
                        safe.botSkin)
                .follow(defaultBoolean(safe.follow, true))
                .setChangeableFollow(defaultBoolean(safe.changeableFollow, true))
                .combat(defaultBoolean(safe.combat, true))
                .setChangeableCombat(defaultBoolean(safe.changeableCombat, true))
                .blastProtection(false, false, false, false)
                .setChangeableBlast(defaultBoolean(safe.changeableBlast, true))
                .armorValue(
                        EnumValues.parseOrDefault(BotArmorType.class, safe.minArmor, BotArmorType.LEATHER),
                        EnumValues.parseOrDefault(BotArmorType.class, safe.maxArmor, BotArmorType.NETHERITE))
                .armor(EnumValues.parseOrDefault(BotArmorType.class, safe.armor, BotArmorType.NETHERITE))
                .setChangeableArmor(defaultBoolean(safe.changeableArmor, true))
                .totemValue(
                        defaultInt(safe.minTotemCount, -1),
                        defaultInt(safe.maxTotemCount, plugin.getConfig().getInt("bot.max-totem-event", 74)))
                .totemCount(defaultInt(safe.totemCount, -1))
                .setChangeableTotem(defaultBoolean(safe.changeableTotem, true))
                .difficultyValue(
                        EnumValues.parseOrDefault(DifficultyLevel.class, safe.minDifficulty, DifficultyLevel.EASY),
                        EnumValues.parseOrDefault(DifficultyLevel.class, safe.maxDifficulty, DifficultyLevel.GOD))
                .difficulty(EnumValues.parseOrDefault(DifficultyLevel.class, safe.difficulty, DifficultyLevel.EASY))
                .setChangeableDifficulty(defaultBoolean(safe.changeableDifficulty, true));

        if (safe.spawnLocation != null) {
            buildStep.spawnLocation(new BotLocation(
                    safe.spawnLocation.worldName,
                    safe.spawnLocation.worldUUID,
                    safe.spawnLocation.x,
                    safe.spawnLocation.y,
                    safe.spawnLocation.z,
                    safe.spawnLocation.yaw,
                    safe.spawnLocation.pitch));
        }

        buildStep
                .autoTarget(defaultBoolean(safe.autoTarget, true))
                .autoTargetRange(defaultDouble(safe.autoTargetRange, 16.0D))
                .attackBots(defaultBoolean(safe.attackBots, false))
                .targetMode(EnumValues.parseOrDefault(
                        com.monkey.ultimatebot.api.model.configuration.BotTargetMode.class,
                        safe.targetMode,
                        com.monkey.ultimatebot.api.model.configuration.BotTargetMode.PLAYERS))
                .combatMode(EnumValues.parseOrDefault(CombatMode.class, safe.combatMode, CombatMode.SWORD))
                .combatTuning(safe.combatTuning)
                .changeableCombatMode(defaultBoolean(safe.changeableCombatMode, true))
                .respectWorldGuardPvp(defaultBoolean(safe.respectWorldGuardPvp, false))
                .stayAfterOwnerDeath(defaultBoolean(safe.stayAfterOwnerDeath, false))
                .idleWander(defaultBoolean(safe.idleWander, false))
                .idleWanderRadius(defaultDouble(safe.idleWanderRadius, 10.0D))
                .idleReturnDistance(defaultDouble(safe.idleReturnDistance, 24.0D))
                .idleReturnDelayMs(defaultLong(safe.idleReturnDelayMs, 8000L))
                .crystalPvp(defaultBoolean(safe.crystalPvp, true))
                .explosions(defaultBoolean(safe.explosions, true))
                .explosionBlockDamage(defaultBoolean(safe.explosionBlockDamage, false))
                .enderPearls(defaultBoolean(safe.enderPearls, true))
                .healing(defaultBoolean(safe.healing, plugin.getConfig().getBoolean("bot.combat.healing", true)));

        if (Boolean.FALSE.equals(safe.killMessageEnabled)) {
            buildStep.disableKillMessage();
        } else if (safe.killMessage != null) {
            buildStep.killMessage(safe.killMessage);
        }

        return buildStep.build();
    }

    private Map<BotEquipmentSlot, BotEquipmentSlotSetting> toEquipmentSettings(
            @Nullable Map<String, EquipmentSlotPayload> payloads) {
        if (payloads == null || payloads.isEmpty()) {
            return Map.of();
        }
        EnumMap<BotEquipmentSlot, BotEquipmentSlotSetting> settings = new EnumMap<>(BotEquipmentSlot.class);
        for (Map.Entry<String, EquipmentSlotPayload> entry : payloads.entrySet()) {
            BotEquipmentSlot slot = EnumValues.parse(BotEquipmentSlot.class, entry.getKey(), null);
            BotEquipmentSlotSetting setting = toEquipmentSetting(entry.getValue());
            if (slot == null || setting == null) {
                throw new IllegalArgumentException("Invalid equipment slot setting: " + entry.getKey());
            }
            if (setting.mode() != BotEquipmentSlotMode.DEFAULT) {
                settings.put(slot, setting);
            }
        }
        return Map.copyOf(settings);
    }

    private @Nullable BotEquipmentSlotSetting toEquipmentSetting(@Nullable EquipmentSlotPayload payload) {
        if (payload == null) {
            return null;
        }
        BotEquipmentSlotMode mode = EnumValues.parse(BotEquipmentSlotMode.class, payload.mode, null);
        if (mode == null) {
            return null;
        }
        return switch (mode) {
            case DEFAULT -> BotEquipmentSlotSetting.defaultSlot();
            case EMPTY -> BotEquipmentSlotSetting.empty();
            case ITEM -> {
                org.bukkit.Material material = org.bukkit.Material.matchMaterial(defaultString(payload.material, ""));
                int amount = defaultInt(payload.amount, 1);
                if (material == null || material.isAir() || amount < 1 || amount > material.getMaxStackSize()) {
                    throw new IllegalArgumentException("ITEM mode requires a valid material and stack amount");
                }
                yield BotEquipmentSlotSetting.item(new org.bukkit.inventory.ItemStack(material, amount));
            }
        };
    }

    private @Nullable UUID resolveOwnerUUID(IBotManager manager, @Nullable UUID requestedUUID) {
        if (requestedUUID == null) {
            return null;
        }
        if (manager.getBot(requestedUUID).isPresent()) {
            return requestedUUID;
        }
        return manager.getBotByBotUUID(requestedUUID)
                .map(BotSnapshot::ownerUUID)
                .orElse(null);
    }

    private BotSettings.FollowStep applySkin(BotSettings.BotSkinStep skinStep, @Nullable String skin) {
        if ("OWNER".equalsIgnoreCase(skin)) {
            return skinStep.setBotSkinOwner();
        }
        return skinStep.setBotSkinRandom();
    }

    private boolean isAuthorized(HttpExchange exchange) {
        String authorization = exchange.getRequestHeaders().getFirst("Authorization");
        if (authorization != null && authorization.equals("Bearer " + token)) {
            return true;
        }
        String headerToken = exchange.getRequestHeaders().getFirst("X-UltimateBot-Token");
        return token.equals(headerToken);
    }

    private String relativePath(HttpExchange exchange) {
        String path = exchange.getRequestURI().getPath();
        String relative = path.length() <= basePath.length() ? "/" : path.substring(basePath.length());
        return relative.isBlank() ? "/" : relative;
    }

    private <T> T readJson(HttpExchange exchange, Class<T> type) throws IOException {
        try (InputStream input = exchange.getRequestBody()) {
            return objectMapper.readValue(input, type);
        }
    }

    private void writeJson(HttpExchange exchange, int status, Object body) throws IOException {
        byte[] response = objectMapper.writeValueAsBytes(body);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(status, response.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(response);
        }
    }

    private void writeText(HttpExchange exchange, int status, String body) throws IOException {
        byte[] response = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; version=0.0.4; charset=UTF-8");
        exchange.sendResponseHeaders(status, response.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(response);
        }
    }

    private <T> T runSync(java.util.concurrent.Callable<T> callable) {
        if (Bukkit.isPrimaryThread()) {
            try {
                return BotEventSourceContext.call(BotEventSource.REMOTE_API, callable);
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        }
        try {
            return Bukkit.getScheduler()
                    .callSyncMethod(plugin, () -> BotEventSourceContext.call(BotEventSource.REMOTE_API, callable))
                    .get();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static String normalizeBasePath(String configured) {
        String path = configured == null || configured.isBlank() ? DEFAULT_BASE_PATH : configured.trim();
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return path.endsWith("/") && path.length() > 1 ? path.substring(0, path.length() - 1) : path;
    }

    private static String metricRoute(String relativePath) {
        if (relativePath.startsWith("/bots/")) {
            String[] parts = relativePath.substring(1).split("/", -1);
            if (parts.length < 3) {
                return "/bots/{bot}";
            }
            return switch (parts[2]) {
                case "equipment" -> "/bots/{bot}/equipment/{slot}";
                case "target-mode",
                        "crystal-pvp",
                        "explosions",
                        "explosion-block-damage",
                        "ender-pearls",
                        "healing",
                        "attack-bots" -> "/bots/{bot}/" + parts[2];
                default -> "/bots/{bot}/unmatched";
            };
        }
        return switch (relativePath) {
            case "/health", "/metrics", "/events", "/bots", "/bots/count", "/bots/event" -> relativePath;
            default -> "/unmatched";
        };
    }

    private static String defaultString(@Nullable String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private static boolean defaultBoolean(@Nullable Boolean value, boolean fallback) {
        return value == null ? fallback : value;
    }

    private static int defaultInt(@Nullable Integer value, int fallback) {
        return value == null ? fallback : value;
    }

    private static long defaultLong(@Nullable Long value, long fallback) {
        return value == null ? fallback : value;
    }

    private static double defaultDouble(@Nullable Double value, double fallback) {
        return value == null ? fallback : value;
    }

    private record RemoteOperationResponse(
            boolean success,
            String message,
            @Nullable BotSnapshot snapshot,
            @Nullable Integer removedCount) {
        static RemoteOperationResponse success(String message, @Nullable BotSnapshot snapshot) {
            return new RemoteOperationResponse(true, message, snapshot, null);
        }

        static RemoteOperationResponse failure(String message) {
            return new RemoteOperationResponse(false, message, null, null);
        }

        static RemoteOperationResponse removed(String message, int removedCount) {
            return new RemoteOperationResponse(true, message, null, removedCount);
        }

        static RemoteOperationResponse from(BotOperationResult result) {
            return new RemoteOperationResponse(result.success(), result.message(), result.snapshot(), null);
        }
    }

    public static final class EventBotSpawnPayload {
        public @Nullable UUID ownerUUID;
        public @Nullable UUID botUUID;
        public @Nullable List<UUID> targetUUIDs;
        public @Nullable String botNameTemplate;
        public @Nullable String botSkin;
        public @Nullable Boolean follow;
        public @Nullable Boolean changeableFollow;
        public @Nullable Boolean combat;
        public @Nullable Boolean changeableCombat;
        public @Nullable Boolean changeableBlast;
        public @Nullable Boolean changeableArmor;
        public @Nullable Boolean changeableTotem;
        public @Nullable Boolean changeableDifficulty;
        public @Nullable Boolean changeableCombatMode;
        public @Nullable String armor;
        public @Nullable String minArmor;
        public @Nullable String maxArmor;
        public @Nullable Integer totemCount;
        public @Nullable Integer minTotemCount;
        public @Nullable Integer maxTotemCount;
        public @Nullable String difficulty;
        public @Nullable String minDifficulty;
        public @Nullable String maxDifficulty;
        public @Nullable String combatMode;
        public @Nullable CombatTuning combatTuning;
        public @Nullable RemoteLocationPayload spawnLocation;
        public @Nullable Boolean autoTarget;
        public @Nullable Double autoTargetRange;
        public @Nullable Boolean attackBots;
        public @Nullable String targetMode;
        public @Nullable Boolean respectWorldGuardPvp;
        public @Nullable Boolean stayAfterOwnerDeath;
        public @Nullable Boolean idleWander;
        public @Nullable Double idleWanderRadius;
        public @Nullable Double idleReturnDistance;
        public @Nullable Long idleReturnDelayMs;
        public @Nullable Boolean crystalPvp;
        public @Nullable Boolean explosions;
        public @Nullable Boolean explosionBlockDamage;
        public @Nullable Boolean enderPearls;
        public @Nullable Boolean healing;
        public @Nullable Boolean killMessageEnabled;
        public @Nullable String killMessage;
        public @Nullable Map<String, EquipmentSlotPayload> equipmentSlots;
    }

    public static final class EquipmentSlotPayload {
        public @Nullable String mode;
        public @Nullable String material;
        public @Nullable Integer amount;
    }

    public static final class TargetModePayload {
        public @Nullable String targetMode;
    }

    public static final class CombatModePayload {
        public @Nullable String combatMode;
    }

    public static final class RemoteLocationPayload {
        public @Nullable String worldName;
        public @Nullable UUID worldUUID;
        public double x;
        public double y;
        public double z;
        public float yaw;
        public float pitch;
    }

    public static final class TogglePayload {
        public @Nullable Boolean enabled;
    }
}
