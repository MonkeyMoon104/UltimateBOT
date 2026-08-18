package com.monkey.ultimatebot.sdk;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.monkey.ultimatebot.common.model.AddonInfo;
import com.monkey.ultimatebot.common.model.BlastProtectionSettings;
import com.monkey.ultimatebot.common.model.BotArmorTier;
import com.monkey.ultimatebot.common.model.BotSource;
import com.monkey.ultimatebot.common.model.BotTargetMode;
import com.monkey.ultimatebot.common.model.BrainDefinition;
import com.monkey.ultimatebot.common.model.BrainKey;
import com.monkey.ultimatebot.common.model.CombatMode;
import com.monkey.ultimatebot.common.model.CombatModeDefinition;
import com.monkey.ultimatebot.common.model.CombatTuning;
import com.monkey.ultimatebot.common.model.DifficultyTier;
import com.monkey.ultimatebot.common.model.PlatformInfo;
import com.monkey.ultimatebot.sdk.model.request.ArmorRequest;
import com.monkey.ultimatebot.sdk.model.request.AutoTargetRequest;
import com.monkey.ultimatebot.sdk.model.request.BotEquipmentSlotRequest;
import com.monkey.ultimatebot.sdk.model.request.BotSpawnRequest;
import com.monkey.ultimatebot.sdk.model.request.BrainRequest;
import com.monkey.ultimatebot.sdk.model.request.CombatModeRequest;
import com.monkey.ultimatebot.sdk.model.request.DifficultyRequest;
import com.monkey.ultimatebot.sdk.model.request.IdleWanderRequest;
import com.monkey.ultimatebot.sdk.model.request.KillMessageRequest;
import com.monkey.ultimatebot.sdk.model.request.TargetModeRequest;
import com.monkey.ultimatebot.sdk.model.request.ToggleRequest;
import com.monkey.ultimatebot.sdk.model.request.TotemCountRequest;
import com.monkey.ultimatebot.sdk.model.request.UuidSetRequest;
import com.monkey.ultimatebot.sdk.model.response.BotCountResponse;
import com.monkey.ultimatebot.sdk.model.response.BotOperationResponse;
import com.monkey.ultimatebot.sdk.model.response.BotSnapshotResponse;
import com.monkey.ultimatebot.sdk.model.type.SdkBotEquipmentSlot;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * Java-only remote client for UltimateBot servers with remote API enabled.
 *
 * <p>This SDK is safe to use with {@code implementation}; it does not depend on Bukkit,
 * Paper or the in-server {@code api} classes.</p>
 */
public final class UltimateBotClient implements AutoCloseable {

    private static final TypeReference<List<BotSnapshotResponse>> BOT_LIST_TYPE = new TypeReference<>() {};
    private static final TypeReference<List<CombatModeDefinition>> COMBAT_MODE_LIST_TYPE = new TypeReference<>() {};
    private static final TypeReference<List<BrainDefinition>> BRAIN_LIST_TYPE = new TypeReference<>() {};
    private static final TypeReference<List<AddonInfo>> ADDON_LIST_TYPE = new TypeReference<>() {};

    private final URI baseUri;
    private final String token;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final UltimateBotEventBus eventBus;

    private UltimateBotClient(Builder builder) {
        this.baseUri = normalizeBaseUri(builder.baseUri);
        this.token = Objects.requireNonNull(builder.token, "token");
        this.objectMapper = builder.objectMapper == null
                ? new ObjectMapper()
                        .findAndRegisterModules()
                        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                : builder.objectMapper;
        this.httpClient = builder.httpClient == null
                ? HttpClient.newBuilder().connectTimeout(builder.timeout).build()
                : builder.httpClient;
        this.eventBus = new UltimateBotEventBus(this.baseUri, this.token, this.httpClient, this.objectMapper);
    }

    public static Builder builder() {
        return new Builder();
    }

    public BotOperationResponse health() {
        return send("GET", "/health", null, BotOperationResponse.class);
    }

    /** Returns the loaded Minecraft version and NMS feature flags for the remote server. */
    public PlatformInfo platform() {
        return send("GET", "/platform", null, PlatformInfo.class);
    }

    /** Returns the reconnecting remote event bus. */
    public UltimateBotEventBus events() {
        return eventBus;
    }

    public List<BotSnapshotResponse> listBots() {
        return send("GET", "/bots", null, BOT_LIST_TYPE);
    }

    /** Returns the server's current active-bot count. */
    public int activeBotCount() {
        return send("GET", "/bots/count", null, BotCountResponse.class).count();
    }

    /** Returns every server combat mode with enabled state and difficulty profiles. */
    public List<CombatModeDefinition> listCombatModes() {
        return send("GET", "/combat-modes", null, COMBAT_MODE_LIST_TYPE);
    }

    /** Returns one complete server combat-mode definition. */
    public CombatModeDefinition getCombatMode(CombatMode combatMode) {
        return send(
                "GET",
                "/combat-modes/"
                        + Objects.requireNonNull(combatMode, "combatMode").key(),
                null,
                CombatModeDefinition.class);
    }

    /** Returns all custom brains currently installed on the server. */
    public List<BrainDefinition> listBrains() {
        return send("GET", "/brains", null, BRAIN_LIST_TYPE);
    }

    /** Returns one installed custom brain definition. */
    public BrainDefinition getBrain(BrainKey brainKey) {
        return send(
                "GET", "/brains/" + Objects.requireNonNull(brainKey, "brainKey").key(), null, BrainDefinition.class);
    }

    /** Returns all addons discovered by the server-side addon engine. */
    public List<AddonInfo> listAddons() {
        return send("GET", "/addons", null, ADDON_LIST_TYPE);
    }

    public BotSnapshotResponse getBot(UUID ownerOrBotUUID) {
        return send(
                "GET",
                "/bots/" + Objects.requireNonNull(ownerOrBotUUID, "ownerOrBotUUID"),
                null,
                BotSnapshotResponse.class);
    }

    /** Spawns any supported bot type using the complete remote configuration contract. */
    public BotOperationResponse spawnBot(BotSpawnRequest request) {
        return send("POST", "/bots", Objects.requireNonNull(request, "request"), BotOperationResponse.class);
    }

    /** Updates a persistent equipment slot using either an owner UUID or bot UUID. */
    public BotOperationResponse updateEquipmentSlot(
            UUID ownerOrBotUUID, SdkBotEquipmentSlot slot, BotEquipmentSlotRequest setting) {
        return send(
                "PUT",
                "/bots/" + Objects.requireNonNull(ownerOrBotUUID, "ownerOrBotUUID")
                        + "/equipment/"
                        + Objects.requireNonNull(slot, "slot").name(),
                Objects.requireNonNull(setting, "setting"),
                BotOperationResponse.class);
    }

    public BotOperationResponse remove(UUID ownerUUID) {
        return send(
                "DELETE", "/bots/" + Objects.requireNonNull(ownerUUID, "ownerUUID"), null, BotOperationResponse.class);
    }

    public BotOperationResponse removeByBotUUID(UUID botUUID) {
        return send("DELETE", "/bots/" + Objects.requireNonNull(botUUID, "botUUID"), null, BotOperationResponse.class);
    }

    public BotOperationResponse removeAll() {
        return send("DELETE", "/bots", null, BotOperationResponse.class);
    }

    /** Removes every bot created through the requested source. */
    public BotOperationResponse removeBySource(BotSource source) {
        return send(
                "DELETE",
                "/bots/source/" + Objects.requireNonNull(source, "source").name(),
                null,
                BotOperationResponse.class);
    }

    public BotOperationResponse updateTotems(UUID ownerOrBotUUID, int totemCount) {
        return patch(ownerOrBotUUID, "totems", new TotemCountRequest(totemCount));
    }

    public BotOperationResponse updateFollow(UUID ownerOrBotUUID, boolean enabled) {
        return toggle(ownerOrBotUUID, "follow", enabled);
    }

    public BotOperationResponse updateCombat(UUID ownerOrBotUUID, boolean enabled) {
        return toggle(ownerOrBotUUID, "combat", enabled);
    }

    public BotOperationResponse updateBlastProtection(UUID ownerOrBotUUID, boolean enabled) {
        return updateBlastProtection(ownerOrBotUUID, BlastProtectionSettings.all(enabled));
    }

    public BotOperationResponse updateBlastProtection(UUID ownerOrBotUUID, BlastProtectionSettings blastProtection) {
        return patch(ownerOrBotUUID, "blast-protection", Objects.requireNonNull(blastProtection, "blastProtection"));
    }

    public BotOperationResponse updateDifficulty(UUID ownerOrBotUUID, DifficultyTier difficulty) {
        return patch(
                ownerOrBotUUID, "difficulty", new DifficultyRequest(Objects.requireNonNull(difficulty, "difficulty")));
    }

    public BotOperationResponse updateArmor(UUID ownerOrBotUUID, BotArmorTier armor) {
        return patch(ownerOrBotUUID, "armor", new ArmorRequest(Objects.requireNonNull(armor, "armor")));
    }

    public BotOperationResponse updateAutoTarget(UUID ownerOrBotUUID, boolean enabled, double range) {
        return patch(ownerOrBotUUID, "auto-target", new AutoTargetRequest(enabled, range));
    }

    public BotOperationResponse updateWorldGuardPvpRespect(UUID ownerOrBotUUID, boolean enabled) {
        return toggle(ownerOrBotUUID, "world-guard-pvp", enabled);
    }

    public BotOperationResponse updateStayAfterOwnerDeath(UUID ownerOrBotUUID, boolean enabled) {
        return toggle(ownerOrBotUUID, "stay-after-owner-death", enabled);
    }

    public BotOperationResponse updateIdleWander(
            UUID ownerOrBotUUID, boolean enabled, double radius, double returnDistance, long returnDelayMs) {
        return patch(
                ownerOrBotUUID, "idle-wander", new IdleWanderRequest(enabled, radius, returnDistance, returnDelayMs));
    }

    public BotOperationResponse updateTargets(UUID ownerOrBotUUID, Set<UUID> targetUUIDs) {
        return patch(ownerOrBotUUID, "targets", new UuidSetRequest(targetUUIDs));
    }

    public BotOperationResponse updateTeamOwners(UUID ownerOrBotUUID, Set<UUID> teamOwnerUUIDs) {
        return patch(ownerOrBotUUID, "team-owners", new UuidSetRequest(teamOwnerUUIDs));
    }

    public BotOperationResponse updateKillMessage(UUID ownerOrBotUUID, String killMessage) {
        return patch(ownerOrBotUUID, "kill-message", new KillMessageRequest(killMessage));
    }

    public BotOperationResponse disableKillMessage(UUID ownerOrBotUUID) {
        Objects.requireNonNull(ownerOrBotUUID, "ownerOrBotUUID");
        return send("DELETE", "/bots/" + ownerOrBotUUID + "/kill-message", null, BotOperationResponse.class);
    }

    public BotOperationResponse updateCrystalPvp(UUID ownerUUID, boolean enabled) {
        return toggle(ownerUUID, "crystal-pvp", enabled);
    }

    public BotOperationResponse updateCrystalPvpByBotUUID(UUID botUUID, boolean enabled) {
        return toggle(Objects.requireNonNull(botUUID, "botUUID"), "crystal-pvp", enabled);
    }

    public BotOperationResponse updateExplosions(UUID ownerUUID, boolean enabled) {
        return toggle(ownerUUID, "explosions", enabled);
    }

    public BotOperationResponse updateExplosionsByBotUUID(UUID botUUID, boolean enabled) {
        return toggle(Objects.requireNonNull(botUUID, "botUUID"), "explosions", enabled);
    }

    public BotOperationResponse updateExplosionBlockDamage(UUID ownerUUID, boolean enabled) {
        return toggle(ownerUUID, "explosion-block-damage", enabled);
    }

    public BotOperationResponse updateExplosionBlockDamageByBotUUID(UUID botUUID, boolean enabled) {
        return toggle(Objects.requireNonNull(botUUID, "botUUID"), "explosion-block-damage", enabled);
    }

    public BotOperationResponse updateEnderPearls(UUID ownerUUID, boolean enabled) {
        return toggle(ownerUUID, "ender-pearls", enabled);
    }

    public BotOperationResponse updateEnderPearlsByBotUUID(UUID botUUID, boolean enabled) {
        return toggle(Objects.requireNonNull(botUUID, "botUUID"), "ender-pearls", enabled);
    }

    public BotOperationResponse updateHealing(UUID ownerUUID, boolean enabled) {
        return toggle(ownerUUID, "healing", enabled);
    }

    public BotOperationResponse updateHealingByBotUUID(UUID botUUID, boolean enabled) {
        return toggle(Objects.requireNonNull(botUUID, "botUUID"), "healing", enabled);
    }

    public BotOperationResponse updateAttackBots(UUID ownerUUID, boolean enabled) {
        return toggle(ownerUUID, "attack-bots", enabled);
    }

    public BotOperationResponse updateAttackBotsByBotUUID(UUID botUUID, boolean enabled) {
        return toggle(Objects.requireNonNull(botUUID, "botUUID"), "attack-bots", enabled);
    }

    public BotOperationResponse updateTargetMode(UUID ownerUUID, BotTargetMode targetMode) {
        return targetMode(ownerUUID, targetMode);
    }

    public BotOperationResponse updateTargetModeByBotUUID(UUID botUUID, BotTargetMode targetMode) {
        return targetMode(botUUID, targetMode);
    }

    public BotOperationResponse updateCombatMode(UUID ownerUUID, CombatMode combatMode) {
        return combatMode(ownerUUID, combatMode);
    }

    public BotOperationResponse updateCombatModeByBotUUID(UUID botUUID, CombatMode combatMode) {
        return combatMode(botUUID, combatMode);
    }

    public BotOperationResponse updateCombatTuning(UUID ownerOrBotUUID, CombatTuning combatTuning) {
        Objects.requireNonNull(ownerOrBotUUID, "ownerOrBotUUID");
        return send(
                "PATCH",
                "/bots/" + ownerOrBotUUID + "/combat-tuning",
                Objects.requireNonNull(combatTuning, "combatTuning"),
                BotOperationResponse.class);
    }

    public BotOperationResponse resetCombatTuning(UUID ownerOrBotUUID) {
        Objects.requireNonNull(ownerOrBotUUID, "ownerOrBotUUID");
        return send("DELETE", "/bots/" + ownerOrBotUUID + "/combat-tuning", null, BotOperationResponse.class);
    }

    /** Assigns a custom brain to the addressed owner or runtime bot UUID. */
    public BotOperationResponse updateBrain(UUID ownerOrBotUUID, BrainKey brainKey) {
        return send(
                "PATCH",
                "/bots/" + Objects.requireNonNull(ownerOrBotUUID, "ownerOrBotUUID") + "/brain",
                new BrainRequest(Objects.requireNonNull(brainKey, "brainKey")),
                BotOperationResponse.class);
    }

    /** Restores the mode-provided or built-in brain. */
    public BotOperationResponse resetBrain(UUID ownerOrBotUUID) {
        return send(
                "DELETE",
                "/bots/" + Objects.requireNonNull(ownerOrBotUUID, "ownerOrBotUUID") + "/brain",
                null,
                BotOperationResponse.class);
    }

    private BotOperationResponse combatMode(UUID ownerOrBotUUID, CombatMode combatMode) {
        Objects.requireNonNull(ownerOrBotUUID, "ownerOrBotUUID");
        return send(
                "PATCH",
                "/bots/" + ownerOrBotUUID + "/combat-mode",
                new CombatModeRequest(Objects.requireNonNull(combatMode, "combatMode")),
                BotOperationResponse.class);
    }

    private BotOperationResponse targetMode(UUID ownerOrBotUUID, BotTargetMode targetMode) {
        Objects.requireNonNull(ownerOrBotUUID, "ownerOrBotUUID");
        Objects.requireNonNull(targetMode, "targetMode");
        return send(
                "PATCH",
                "/bots/" + ownerOrBotUUID + "/target-mode",
                new TargetModeRequest(targetMode),
                BotOperationResponse.class);
    }

    private BotOperationResponse toggle(UUID ownerUUID, String field, boolean enabled) {
        Objects.requireNonNull(ownerUUID, "ownerUUID");
        return send(
                "PATCH", "/bots/" + ownerUUID + "/" + field, new ToggleRequest(enabled), BotOperationResponse.class);
    }

    private BotOperationResponse patch(UUID ownerOrBotUUID, String field, Object request) {
        Objects.requireNonNull(ownerOrBotUUID, "ownerOrBotUUID");
        return send(
                "PATCH",
                "/bots/" + ownerOrBotUUID + "/" + Objects.requireNonNull(field, "field"),
                Objects.requireNonNull(request, "request"),
                BotOperationResponse.class);
    }

    private <T> T send(String method, String path, @Nullable Object body, Class<T> responseType) {
        try {
            HttpResponse<String> response =
                    httpClient.send(buildRequest(method, path, body), HttpResponse.BodyHandlers.ofString());
            return decodeResponse(response, responseType);
        } catch (IOException ex) {
            throw new UltimateBotClientException("Remote API request failed: " + ex.getMessage(), ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new UltimateBotClientException("Remote API request interrupted", ex);
        }
    }

    private <T> T send(String method, String path, @Nullable Object body, TypeReference<T> responseType) {
        try {
            HttpResponse<String> response =
                    httpClient.send(buildRequest(method, path, body), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new UltimateBotClientException(
                        "Remote API returned HTTP " + response.statusCode() + ": " + response.body());
            }
            return objectMapper.readValue(response.body(), responseType);
        } catch (IOException ex) {
            throw new UltimateBotClientException("Remote API request failed: " + ex.getMessage(), ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new UltimateBotClientException("Remote API request interrupted", ex);
        }
    }

    private HttpRequest buildRequest(String method, String path, @Nullable Object body) throws IOException {
        HttpRequest.Builder builder = HttpRequest.newBuilder(baseUri.resolve(trimLeadingSlash(path)))
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/json");

        if (body == null) {
            builder.method(method, HttpRequest.BodyPublishers.noBody());
        } else {
            builder.header("Content-Type", "application/json");
            builder.method(method, HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)));
        }
        return builder.build();
    }

    private <T> T decodeResponse(HttpResponse<String> response, Class<T> responseType) throws IOException {
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new UltimateBotClientException(
                    "Remote API returned HTTP " + response.statusCode() + ": " + response.body());
        }
        return objectMapper.readValue(response.body(), responseType);
    }

    private static URI normalizeBaseUri(URI baseUri) {
        Objects.requireNonNull(baseUri, "baseUri");
        String raw = baseUri.toString();
        return URI.create(raw.endsWith("/") ? raw : raw + "/");
    }

    private static String trimLeadingSlash(String path) {
        return path.startsWith("/") ? path.substring(1) : path;
    }

    @Override
    public void close() {
        eventBus.close();
        httpClient.close();
    }

    public static final class Builder {
        private URI baseUri = URI.create("http://127.0.0.1:8765/ultimatebot/api/v1/");
        private @Nullable String token;
        private Duration timeout = Duration.ofSeconds(5);
        private @Nullable HttpClient httpClient;
        private @Nullable ObjectMapper objectMapper;

        private Builder() {}

        public Builder baseUri(String baseUri) {
            this.baseUri = URI.create(Objects.requireNonNull(baseUri, "baseUri"));
            return this;
        }

        public Builder baseUri(URI baseUri) {
            this.baseUri = Objects.requireNonNull(baseUri, "baseUri");
            return this;
        }

        public Builder token(String token) {
            this.token = Objects.requireNonNull(token, "token");
            return this;
        }

        public Builder timeout(Duration timeout) {
            this.timeout = Objects.requireNonNull(timeout, "timeout");
            return this;
        }

        public Builder httpClient(@Nullable HttpClient httpClient) {
            this.httpClient = httpClient;
            return this;
        }

        public Builder objectMapper(@Nullable ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
            return this;
        }

        public UltimateBotClient build() {
            if (token == null || token.trim().isEmpty()) {
                throw new IllegalArgumentException("token is required");
            }
            return new UltimateBotClient(this);
        }
    }
}
