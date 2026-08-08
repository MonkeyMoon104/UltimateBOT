package com.monkey.ultimatebot.nms;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.bot.BotOptions;
import com.monkey.ultimatebot.bot.BotType;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.common.model.PlatformCapability;
import com.monkey.ultimatebot.gui.NewBotGUI;
import com.monkey.ultimatebot.protocol.BotProfileData;
import java.util.Map;
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
 * Version-specific native bridge. Signatures are Bukkit-only so {@code core} can stay free of
 * {@code net.minecraft} types while NMS modules keep the real implementations.
 */
public interface INMSBridge {

    Set<PlatformCapability> capabilities();

    default boolean supports(PlatformCapability capability) {
        return capabilities().contains(capability);
    }

    ITrainingBot createTrainingBot(
            Location spawn,
            BotProfileData profile,
            Player targetPlayer,
            boolean follow,
            UltimateBot plugin,
            String deadBotMessage,
            String deadBotEventMessage,
            BotOptions botOptions);

    void registerBotEntity(ITrainingBot bot);

    void addToProfileCache(ITrainingBot bot);

    void removeFromProfileCache(UUID botUUID);

    void moveBot(Player bot, double x, double y, double z);

    BotProfileData copyProfileWithTextures(Player viewer, UUID botUUID, String botName);

    BotProfileData createProfileWithTexture(
            UUID botUUID, String botName, String textureValue, @Nullable String textureSignature);

    String getProfileName(BotProfileData profile);

    boolean actuallyHurt(Player bot, float amount, EntityDamageEvent event);

    void hurt(LivingEntity target, @Nullable Player attacker, float amount, DamageKind kind);

    void explode(Location location, @Nullable Player cause, float power, boolean blockDamage);

    void playSound(Location location, String soundKey, String source, float volume, float pitch);

    void playSoundOnPlayer(Player player, String soundKey, float volume, float pitch);

    boolean useItemOnBlock(
            Player bot,
            ItemStack stack,
            Block clicked,
            BlockFace face,
            Location hitLocation,
            EquipmentSlot hand);

    void throwEnderpearl(Player bot, Vector targetPos);

    void sendTabListAdd(Player viewer, ITrainingBot bot);

    void sendSpawnAndMeta(Player viewer, ITrainingBot bot);

    void sendEquipment(Player viewer, ITrainingBot bot, Map<EquipmentSlot, ItemStack> equipment);

    void broadcastEquipment(ITrainingBot bot, Map<EquipmentSlot, ItemStack> equipment);

    void broadcastMetadata(ITrainingBot bot);

    default void openBotGui(Player player, UltimateBot plugin, BotType botType) {
        new NewBotGUI(player, plugin, botType).open();
    }
}
