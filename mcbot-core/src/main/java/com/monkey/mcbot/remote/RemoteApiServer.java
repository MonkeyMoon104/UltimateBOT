package com.monkey.mcbot.remote;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.monkey.mcbot.MinecraftBot;
import com.monkey.mcbot.api.MinecraftBotAPI;
import com.monkey.mcbot.api.managers.IBotManager;
import com.monkey.mcbot.api.model.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.Executors;

public final class RemoteApiServer {

    private static final String DEFAULT_BASE_PATH = "/mcbot/api/v1";
    private static final String DEFAULT_HOST = "127.0.0.1";
    private static final int DEFAULT_PORT = 8765;

    private final MinecraftBot plugin;
    private final MinecraftBotAPI api;
    private final ObjectMapper objectMapper;
    private HttpServer server;
    private String token;
    private String basePath;

    public RemoteApiServer(MinecraftBot plugin, MinecraftBotAPI api) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.api = Objects.requireNonNull(api, "api");
        this.objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public void start() {
        if (!plugin.getConfig().getBoolean("remote-api.enabled", false)) {
            return;
        }

        token = plugin.getConfig().getString("remote-api.token", "");
        if (token == null || token.isBlank() || "change-me".equalsIgnoreCase(token)) {
            plugin.getLogger().warning("Remote API is enabled but remote-api.token is not configured. Remote API will not start.");
            return;
        }

        String host = plugin.getConfig().getString("remote-api.host", DEFAULT_HOST);
        int port = plugin.getConfig().getInt("remote-api.port", DEFAULT_PORT);
        basePath = normalizeBasePath(plugin.getConfig().getString("remote-api.base-path", DEFAULT_BASE_PATH));

        try {
            server = HttpServer.create(new InetSocketAddress(host, port), 0);
            server.createContext(basePath, this::handle);
            server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
            server.start();
            plugin.getLogger().info("Remote API started on http://" + host + ":" + port + basePath);
        } catch (IOException ex) {
            plugin.getLogger().warning("Remote API failed to start: " + ex.getMessage());
            server = null;
        }
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            server = null;
            plugin.getLogger().info("Remote API stopped.");
        }
    }

    private void handle(HttpExchange exchange) throws IOException {
        try {
            if (!isAuthorized(exchange)) {
                writeJson(exchange, 401, RemoteOperationResponse.failure("Unauthorized"));
                return;
            }

            String relativePath = relativePath(exchange);
            String method = exchange.getRequestMethod();

            if ("GET".equals(method) && "/health".equals(relativePath)) {
                writeJson(exchange, 200, RemoteOperationResponse.success("MinecraftBot remote API is online.", null));
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
            writeJson(exchange, 400, RemoteOperationResponse.failure(ex.getMessage()));
        } catch (Exception ex) {
            plugin.getLogger().warning("Remote API request failed: " + ex.getMessage());
            writeJson(exchange, 500, RemoteOperationResponse.failure("Internal remote API error."));
        } finally {
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
                .owner(payload.ownerUUID)
                .targets(payload.targetUUIDs == null ? List.of() : payload.targetUUIDs)
                .settings(settings)
                .build();
        BotOperationResult result = runSync(() -> api.getBotManager().spawn(request));
        writeJson(exchange, result.success() ? 200 : 400, RemoteOperationResponse.from(result));
    }

    private void handleBotMutation(HttpExchange exchange, String method, String relativePath) throws IOException {
        String[] parts = relativePath.substring(1).split("/");
        if (parts.length < 2 || !"bots".equals(parts[0])) {
            writeJson(exchange, 404, RemoteOperationResponse.failure("Endpoint not found."));
            return;
        }

        UUID requestedUUID = UUID.fromString(parts[1]);
        IBotManager manager = api.getBotManager();
        UUID ownerUUID = resolveOwnerUUID(manager, requestedUUID);

        if ("GET".equals(method) && parts.length == 2) {
            BotSnapshot snapshot = runSync(() -> ownerUUID != null ? manager.getBot(ownerUUID).orElse(null) : manager.getBotByBotUUID(requestedUUID).orElse(null));
            if (snapshot == null) {
                writeJson(exchange, 404, RemoteOperationResponse.failure("Bot not found."));
            } else {
                writeJson(exchange, 200, snapshot);
            }
            return;
        }

        if ("DELETE".equals(method) && parts.length == 2) {
            boolean removed = runSync(() -> ownerUUID != null ? manager.remove(ownerUUID) : manager.removeByBotUUID(requestedUUID));
            writeJson(exchange, removed ? 200 : 404, new RemoteOperationResponse(removed, removed ? "Bot removed." : "Bot not found.", null, removed ? 1 : 0));
            return;
        }

        if (!"PATCH".equals(method) || parts.length != 3) {
            writeJson(exchange, 404, RemoteOperationResponse.failure("Endpoint not found."));
            return;
        }

        TogglePayload payload = readJson(exchange, TogglePayload.class);
        boolean enabled = payload != null && Boolean.TRUE.equals(payload.enabled);
        boolean updated = switch (parts[2]) {
            case "crystal-pvp" -> runSync(() -> ownerUUID != null ? manager.updateCrystalPvp(ownerUUID, enabled) : manager.updateCrystalPvpByBotUUID(requestedUUID, enabled));
            case "explosions" -> runSync(() -> ownerUUID != null ? manager.updateExplosions(ownerUUID, enabled) : manager.updateExplosionsByBotUUID(requestedUUID, enabled));
            case "ender-pearls" -> runSync(() -> ownerUUID != null ? manager.updateEnderPearls(ownerUUID, enabled) : manager.updateEnderPearlsByBotUUID(requestedUUID, enabled));
            case "attack-bots" -> runSync(() -> ownerUUID != null ? manager.updateAttackBots(ownerUUID, enabled) : manager.updateAttackBotsByBotUUID(requestedUUID, enabled));
            default -> false;
        };
        BotSnapshot snapshot = runSync(() -> ownerUUID != null ? manager.getBot(ownerUUID).orElse(null) : manager.getBotByBotUUID(requestedUUID).orElse(null));
        writeJson(exchange, updated ? 200 : 400, new RemoteOperationResponse(updated, updated ? "Bot updated." : "Bot update failed.", snapshot, null));
    }

    private BotSettings buildSettings(EventBotSpawnPayload payload) {
        EventBotSpawnPayload safe = payload == null ? new EventBotSpawnPayload() : payload;
        BotSettings.BuildStep buildStep = applySkin(
                BotSettings.builder().setBotNameTemplate(defaultString(safe.botNameTemplate, plugin.getConfig().getString("bot.name", "MinecraftBot"))),
                safe.botSkin
        )
                .follow(defaultBoolean(safe.follow, true))
                .setChangeableFollow(defaultBoolean(safe.changeableFollow, true))
                .combat(defaultBoolean(safe.combat, true))
                .setChangeableCombat(defaultBoolean(safe.changeableCombat, true))
                .blastProtection(false, false, false, false)
                .setChangeableBlast(defaultBoolean(safe.changeableBlast, true))
                .armorValue(enumValue(BotArmorType.class, safe.minArmor, BotArmorType.LEATHER), enumValue(BotArmorType.class, safe.maxArmor, BotArmorType.NETHERITE))
                .armor(enumValue(BotArmorType.class, safe.armor, BotArmorType.NETHERITE))
                .setChangeableArmor(defaultBoolean(safe.changeableArmor, true))
                .totemValue(defaultInt(safe.minTotemCount, -1), defaultInt(safe.maxTotemCount, plugin.getConfig().getInt("bot.max-totem-event", 74)))
                .totemCount(defaultInt(safe.totemCount, -1))
                .setChangeableTotem(defaultBoolean(safe.changeableTotem, true))
                .rankValue(enumValue(BotRank.class, safe.minRank, BotRank.EASY), enumValue(BotRank.class, safe.maxRank, BotRank.GOD))
                .rank(enumValue(BotRank.class, safe.rank, BotRank.EASY))
                .setChangeableRank(defaultBoolean(safe.changeableRank, true));

        if (safe.spawnLocation != null) {
            buildStep.spawnLocation(new BotLocation(
                    safe.spawnLocation.worldName,
                    safe.spawnLocation.worldUUID,
                    safe.spawnLocation.x,
                    safe.spawnLocation.y,
                    safe.spawnLocation.z,
                    safe.spawnLocation.yaw,
                    safe.spawnLocation.pitch
            ));
        }

        buildStep.autoTarget(defaultBoolean(safe.autoTarget, true))
                .autoTargetRange(defaultDouble(safe.autoTargetRange, 16.0D))
                .attackBots(defaultBoolean(safe.attackBots, false))
                .respectWorldGuardPvp(defaultBoolean(safe.respectWorldGuardPvp, false))
                .stayAfterOwnerDeath(defaultBoolean(safe.stayAfterOwnerDeath, false))
                .idleWander(defaultBoolean(safe.idleWander, false))
                .idleWanderRadius(defaultDouble(safe.idleWanderRadius, 10.0D))
                .idleReturnDistance(defaultDouble(safe.idleReturnDistance, 24.0D))
                .idleReturnDelayMs(defaultLong(safe.idleReturnDelayMs, 8000L))
                .crystalPvp(defaultBoolean(safe.crystalPvp, true))
                .explosions(defaultBoolean(safe.explosions, true))
                .enderPearls(defaultBoolean(safe.enderPearls, true));

        if (Boolean.FALSE.equals(safe.killMessageEnabled)) {
            buildStep.disableKillMessage();
        } else if (safe.killMessage != null) {
            buildStep.killMessage(safe.killMessage);
        }

        return buildStep.build();
    }

    private UUID resolveOwnerUUID(IBotManager manager, UUID requestedUUID) {
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

    private BotSettings.FollowStep applySkin(BotSettings.BotSkinStep skinStep, String skin) {
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
        String headerToken = exchange.getRequestHeaders().getFirst("X-MinecraftBot-Token");
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

    private <T> T runSync(java.util.concurrent.Callable<T> callable) {
        if (Bukkit.isPrimaryThread()) {
            try {
                return callable.call();
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        }
        try {
            return Bukkit.getScheduler().callSyncMethod(plugin, callable).get();
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

    private static String defaultString(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private static boolean defaultBoolean(Boolean value, boolean fallback) {
        return value == null ? fallback : value;
    }

    private static int defaultInt(Integer value, int fallback) {
        return value == null ? fallback : value;
    }

    private static long defaultLong(Long value, long fallback) {
        return value == null ? fallback : value;
    }

    private static double defaultDouble(Double value, double fallback) {
        return value == null ? fallback : value;
    }

    private static <E extends Enum<E>> E enumValue(Class<E> type, String value, E fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return Enum.valueOf(type, value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return fallback;
        }
    }

    private record RemoteOperationResponse(boolean success, String message, BotSnapshot snapshot, Integer removedCount) {
        static RemoteOperationResponse success(String message, BotSnapshot snapshot) {
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
        public UUID ownerUUID;
        public List<UUID> targetUUIDs;
        public String botNameTemplate;
        public String botSkin;
        public Boolean follow;
        public Boolean changeableFollow;
        public Boolean combat;
        public Boolean changeableCombat;
        public Boolean changeableBlast;
        public Boolean changeableArmor;
        public Boolean changeableTotem;
        public Boolean changeableRank;
        public String armor;
        public String minArmor;
        public String maxArmor;
        public Integer totemCount;
        public Integer minTotemCount;
        public Integer maxTotemCount;
        public String rank;
        public String minRank;
        public String maxRank;
        public RemoteLocationPayload spawnLocation;
        public Boolean autoTarget;
        public Double autoTargetRange;
        public Boolean attackBots;
        public Boolean respectWorldGuardPvp;
        public Boolean stayAfterOwnerDeath;
        public Boolean idleWander;
        public Double idleWanderRadius;
        public Double idleReturnDistance;
        public Long idleReturnDelayMs;
        public Boolean crystalPvp;
        public Boolean explosions;
        public Boolean enderPearls;
        public Boolean killMessageEnabled;
        public String killMessage;
    }

    public static final class RemoteLocationPayload {
        public String worldName;
        public UUID worldUUID;
        public double x;
        public double y;
        public double z;
        public float yaw;
        public float pitch;
    }

    public static final class TogglePayload {
        public Boolean enabled;
    }
}
