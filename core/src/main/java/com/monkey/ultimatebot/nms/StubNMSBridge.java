package com.monkey.ultimatebot.nms;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.bot.BotOptions;
import com.monkey.ultimatebot.bot.BotType;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.common.model.PlatformCapability;
import com.monkey.ultimatebot.protocol.BotProfileData;
import com.monkey.ultimatebot.utils.ChatColorUtils;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jspecify.annotations.Nullable;

/**
 * Placeholder bridge for Minecraft revisions without a real NMS module yet (e.g. 1.9–1.16.3, plain
 * 1.8 / 1.8.3). Fake-player NMS is not implemented; bot spawn / full GUI stay disabled.
 *
 * <p>Platform capabilities still reflect the server era so combat-mode catalogs can filter correctly
 * while {@link #isBotRuntimeSupported()} remains {@code false}.
 *
 * <p>Bookend runtimes {@code v1_8_R3} (1.8.4–1.8.8) and {@code v1_16_R3} (1.16.4–1.16.5) use real
 * bridges instead.
 */
public final class StubNMSBridge implements INMSBridge {

    private static final String UNSUPPORTED =
            "UltimateBot fake-player runtime is not implemented for this Minecraft revision yet.";

    private final Set<PlatformCapability> capabilities;

    /** Empty capability stub (unknown / unmapped version). */
    public StubNMSBridge() {
        this(PlatformCapability.none());
    }

    public StubNMSBridge(Set<PlatformCapability> capabilities) {
        Set<PlatformCapability> checked = Objects.requireNonNull(capabilities, "capabilities");
        this.capabilities =
                checked.isEmpty()
                        ? PlatformCapability.none()
                        : Collections.unmodifiableSet(EnumSet.copyOf(checked));
    }

    /** Stub bridge whose capabilities match the given Minecraft version string. */
    public static StubNMSBridge forVersion(String version) {
        return new StubNMSBridge(PlatformCapability.forMinecraftVersion(version));
    }

    @Override
    public Set<PlatformCapability> capabilities() {
        return capabilities;
    }

    @Override
    public boolean isBotRuntimeSupported() {
        return false;
    }

    @Override
    public ITrainingBot createTrainingBot(
            Location spawn,
            BotProfileData profile,
            Player targetPlayer,
            boolean follow,
            UltimateBot plugin,
            String deadBotMessage,
            String deadBotEventMessage,
            BotOptions botOptions) {
        throw new UnsupportedOperationException(UNSUPPORTED);
    }

    @Override
    public void openBotGui(Player player, UltimateBot plugin, BotType botType) {
        player.sendMessage(ChatColorUtils.translate("&c" + UNSUPPORTED));
    }

    @Override
    public void registerBotEntity(ITrainingBot bot) {}

    @Override
    public void addToProfileCache(ITrainingBot bot) {}

    @Override
    public void removeFromProfileCache(UUID botUUID) {}

    @Override
    public void moveBot(Player bot, double x, double y, double z) {}

    @Override
    public BotProfileData copyProfileWithTextures(Player viewer, UUID botUUID, String botName) {
        return new BotProfileData(botUUID, botName, Collections.<BotProfileData.Texture>emptyList());
    }

    @Override
    public BotProfileData createProfileWithTexture(
            UUID botUUID, String botName, String textureValue, @Nullable String textureSignature) {
        BotProfileData.Texture texture =
                new BotProfileData.Texture("textures", textureValue, textureSignature);
        return new BotProfileData(botUUID, botName, Collections.singletonList(texture));
    }

    @Override
    public String getProfileName(BotProfileData profile) {
        return profile.name();
    }

    @Override
    public boolean actuallyHurt(Player bot, float amount, EntityDamageEvent event) {
        return event == null || !event.isCancelled();
    }

    @Override
    public void hurt(LivingEntity target, @Nullable Player attacker, float amount, DamageKind kind) {}

    @Override
    public void explode(Location location, @Nullable Player cause, float power, boolean blockDamage) {}

    @Override
    public void playSound(Location location, String soundKey, String source, float volume, float pitch) {}

    @Override
    public void playSoundOnPlayer(Player player, String soundKey, float volume, float pitch) {}

    @Override
    public boolean useItemOnBlock(
            Player bot,
            ItemStack stack,
            Block clicked,
            BlockFace face,
            Location hitLocation,
            EquipmentSlot hand) {
        return false;
    }

    @Override
    public void throwEnderpearl(Player bot, Vector targetPos) {}

    @Override
    public void sendTabListAdd(Player viewer, ITrainingBot bot) {}

    @Override
    public void sendSpawnAndMeta(Player viewer, ITrainingBot bot) {}

    @Override
    public void sendEquipment(Player viewer, ITrainingBot bot, Map<EquipmentSlot, ItemStack> equipment) {}

    @Override
    public void broadcastEquipment(ITrainingBot bot, Map<EquipmentSlot, ItemStack> equipment) {}

    @Override
    public void broadcastMetadata(ITrainingBot bot) {}

    @Override
    public ItemStack getBotItem(ITrainingBot bot, EquipmentSlot slot) {
        return new ItemStack(org.bukkit.Material.AIR);
    }

    @Override
    public void setBotItem(ITrainingBot bot, EquipmentSlot slot, @Nullable ItemStack stack) {}

    @Override
    public void clearBotInventory(ITrainingBot bot) {}

    @Override
    public void beginUsingBotItem(ITrainingBot bot, EquipmentSlot hand) {}

    @Override
    public void stopUsingBotItem(ITrainingBot bot) {}

    @Override
    public boolean isBotOnGround(ITrainingBot bot) {
        return true;
    }

    @Override
    public void setBotOnGround(ITrainingBot bot, boolean onGround) {}

    @Override
    public Vector getBotVelocity(ITrainingBot bot) {
        return new Vector(0, 0, 0);
    }

    @Override
    public void setBotVelocity(ITrainingBot bot, Vector velocity) {}

    @Override
    public float getBotFallDistance(ITrainingBot bot) {
        return 0F;
    }

    @Override
    public void setBotFallDistance(ITrainingBot bot, float fallDistance) {}

    @Override
    public void attackTarget(ITrainingBot bot, LivingEntity target) {}
}
