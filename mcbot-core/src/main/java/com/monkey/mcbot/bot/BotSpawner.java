package com.monkey.mcbot.bot;

import com.mojang.authlib.GameProfile;
import com.monkey.mcbot.MinecraftBot;
import com.monkey.mcbot.api.model.BotLocation;
import com.monkey.mcbot.api.model.BotSkin;
import com.monkey.mcbot.api.model.BotSkinSource;
import com.monkey.mcbot.bot.ai.ITrainingBot;
import com.monkey.mcbot.logging.MinecraftBotLogging;
import com.monkey.mcbot.nms.NMSBridgeManager;
import com.monkey.mcbot.placeholders.PlaceholderApiSupport;
import com.monkey.mcbot.utils.EntityUtils;
import com.monkey.mcbot.utils.equipment.BotEquipmentUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class BotSpawner {

    private final MinecraftBot plugin;
    private final BotRegistry registry;

    public BotSpawner(MinecraftBot plugin, BotRegistry registry) {
        this.plugin = plugin;
        this.registry = registry;
    }

    public void spawn(Player viewer,
                      Player target,
                      Map<EquipmentSlot, ItemStack> armorMap,
                      Map<EquipmentSlot, Boolean> blastProtectionMap,
                      boolean follow,
                      int totem,
                      BotOptions botOptions) {

        if (viewer == null || !viewer.isOnline()) {
            return;
        }

        Player registryOwner = resolveRegistryOwner(viewer, botOptions);
        UUID registryOwnerUUID = resolveRegistryOwnerUUID(registryOwner, botOptions);
        Player resolvedTarget = resolveTargetPlayer(registryOwner, target, botOptions);

        if (registry.isBotSpawned(registryOwnerUUID)) {
            despawnByOwnerUUID(registryOwnerUUID);
        }

        UUID botUUID = UUID.randomUUID();
        botOptions.setOwnerUUID(registryOwnerUUID);
        if (botOptions.getBotType() == BotType.TEAM_ALLY && botOptions.getTeamOwnerUUIDs().isEmpty()) {
            botOptions.addTeamOwner(registryOwnerUUID);
        }

        FileConfiguration config = plugin.getConfig();
        String nameTemplate = resolveBotNameTemplate(config, botOptions);
        String botName = resolveBotName(nameTemplate, registryOwner, resolvedTarget, botOptions);
        GameProfile profile = resolveProfile(registryOwner, botUUID, botName, botOptions);

        Location spawnLocation = resolveSpawnLocation(registryOwner, botOptions);
        botOptions.setSpawnLocation(BotLocation.of(spawnLocation));
        ServerLevel world = ((CraftWorld) spawnLocation.getWorld()).getHandle();
        BlockPos spawnPos = BlockPos.containing(spawnLocation.getX(), spawnLocation.getY(), spawnLocation.getZ());

        ITrainingBot bot = NMSBridgeManager.get().createTrainingBot(
                world,
                spawnPos,
                0,
                profile,
                resolvedTarget,
                follow,
                plugin,
                plugin.getLangString("messages.dead-bot-msg", "You have killed the bot!"),
                plugin.getLangString("messages.dead-bot-event-msg"),
                botOptions
        );

        bot.setTotemCount(totem);
        NMSBridgeManager.get().addToProfileCache(bot.asPlayer());
        world.addFreshEntity(bot.asPlayer());
        plugin.markCompatibilityBot(bot.asPlayer().getBukkitEntity());
        bot.getBotAI().manageTotem();
        BotEquipmentUtils.applyEquipment(bot.asPlayer(), armorMap, blastProtectionMap);
        applyCustomEquipment(bot, botOptions);

        BotBroadcaster.broadcastSpawn(bot, armorMap, blastProtectionMap);
        registry.registerBot(registryOwnerUUID, bot);

        if (botOptions == null || botOptions.isEnderPearls()) {
            bot.getBotAI().getInventoryController().addEnderpearls(16);
        } else {
            bot.getBotAI().getInventoryController().setItem(
                    com.monkey.mcbot.bot.ai.controllers.inventory.BotInventoryController.ENDERPEARL_SLOT,
                    net.minecraft.world.item.ItemStack.EMPTY
            );
        }

        if (botOptions == null || botOptions.isCombat()) {
            bot.getBotAI().getInventoryController().switchToSword();
        } else if (botOptions.isEnderPearls()) {
            bot.getBotAI().getInventoryController().switchToEnderpearl();
        }

        if (botOptions != null && (!botOptions.isCrystalPvp() || !botOptions.isExplosions())) {
            bot.getBotAI().getCPVPController().setEnabled(false);
            clearExplosiveItems(bot);
        }
    }

    private UUID resolveRegistryOwnerUUID(Player registryOwner, BotOptions botOptions) {
        if (botOptions != null
                && botOptions.getCreationSource() == BotCreationSource.API
                && botOptions.getBotType() == BotType.EVENT
                && botOptions.getOwnerUUID() != null) {
            return botOptions.getOwnerUUID();
        }
        return registryOwner.getUniqueId();
    }

    private void clearExplosiveItems(ITrainingBot bot) {
        bot.getBotAI().getInventoryController().setItem(
                com.monkey.mcbot.bot.ai.controllers.inventory.BotInventoryController.CRYSTAL_SLOT,
                net.minecraft.world.item.ItemStack.EMPTY
        );
        bot.getBotAI().getInventoryController().setItem(
                com.monkey.mcbot.bot.ai.controllers.inventory.BotInventoryController.ANCHOR_SLOT,
                net.minecraft.world.item.ItemStack.EMPTY
        );
        bot.getBotAI().getInventoryController().setItem(
                com.monkey.mcbot.bot.ai.controllers.inventory.BotInventoryController.GLOW_SLOT,
                net.minecraft.world.item.ItemStack.EMPTY
        );
    }

    private Location resolveSpawnLocation(Player registryOwner, BotOptions botOptions) {
        if (botOptions != null && botOptions.getSpawnLocation() != null) {
            com.monkey.mcbot.api.model.BotLocation apiLocation = botOptions.getSpawnLocation();
            World world = apiLocation.worldUUID() == null ? null : Bukkit.getWorld(apiLocation.worldUUID());
            if (world == null && apiLocation.worldName() != null) {
                world = Bukkit.getWorld(apiLocation.worldName());
            }
            if (world != null) {
                return new Location(
                        world,
                        apiLocation.x(),
                        apiLocation.y(),
                        apiLocation.z(),
                        apiLocation.yaw(),
                        apiLocation.pitch()
                );
            }
        }

        Location loc = registryOwner.getLocation();
        Block block = loc.getWorld().getHighestBlockAt(loc);
        return new Location(loc.getWorld(), block.getX(), block.getY(), block.getZ(), loc.getYaw(), loc.getPitch());
    }

    private void applyCustomEquipment(ITrainingBot bot, BotOptions botOptions) {
        if (botOptions == null || botOptions.getEquipmentContents().isEmpty()) {
            return;
        }

        for (Map.Entry<Integer, ItemStack> entry : botOptions.getEquipmentContents().entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                continue;
            }
            net.minecraft.world.item.ItemStack nmsItem = org.bukkit.craftbukkit.inventory.CraftItemStack.asNMSCopy(entry.getValue());
            bot.getBotAI().getInventoryController().setItem(entry.getKey(), nmsItem);
        }
    }

    private Player resolveTargetPlayer(Player registryOwner, Player target, BotOptions botOptions) {
        if (target != null && target.isOnline()) {
            return target;
        }

        if (botOptions != null && botOptions.getPreferredTargetUUID() != null) {
            Player preferred = Bukkit.getPlayer(botOptions.getPreferredTargetUUID());
            if (preferred != null && preferred.isOnline()) {
                return preferred;
            }
        }

        if (botOptions != null && !botOptions.getTargetUUIDs().isEmpty()) {
            for (UUID targetUUID : botOptions.getTargetUUIDs()) {
                Player candidate = Bukkit.getPlayer(targetUUID);
                if (candidate != null && candidate.isOnline()) {
                    return candidate;
                }
            }
        }

        return registryOwner;
    }

    private Player resolveRegistryOwner(Player viewer, BotOptions botOptions) {
        if (botOptions == null || botOptions.getBotType() != BotType.TEAM_ALLY) {
            return viewer;
        }

        UUID explicitOwner = botOptions.getOwnerUUID();
        if (explicitOwner != null && botOptions.isTeamOwner(explicitOwner)) {
            Player explicitOwnerPlayer = Bukkit.getPlayer(explicitOwner);
            if (explicitOwnerPlayer != null && explicitOwnerPlayer.isOnline()) {
                return explicitOwnerPlayer;
            }
        }

        for (UUID ownerUUID : botOptions.getTeamOwnerUUIDs()) {
            Player owner = Bukkit.getPlayer(ownerUUID);
            if (owner != null && owner.isOnline()) {
                return owner;
            }
        }

        return viewer;
    }

    private String resolveBotNameTemplate(FileConfiguration config, BotOptions botOptions) {
        String configured = config.getString("bot.name", "CrystalBot");
        if (botOptions == null || botOptions.getCreationSource() != BotCreationSource.API) {
            return configured;
        }

        String customTemplate = botOptions.getBotNameTemplate();
        return customTemplate == null || customTemplate.isBlank() ? configured : customTemplate;
    }

    private String resolveBotName(String nameTemplate, Player owner, Player target, BotOptions botOptions) {
        String template = nameTemplate == null || nameTemplate.isBlank() ? "CrystalBot" : nameTemplate;

        String firstOwnerName = owner.getName();
        if (botOptions != null && botOptions.getBotType() == BotType.TEAM_ALLY && !botOptions.getTeamOwnerUUIDs().isEmpty()) {
            Player firstTeamOwner = resolveFirstTeamOwnerPlayer(botOptions);
            if (firstTeamOwner != null) {
                firstOwnerName = firstTeamOwner.getName();
            }
        }

        String ownersCount = botOptions == null
                ? "1"
                : String.valueOf(Math.max(1, botOptions.getTeamOwnerUUIDs().isEmpty() ? 1 : botOptions.getTeamOwnerUUIDs().size()));

        String replaced = template
                .replace("%player%", owner.getName())
                .replace("%owner%", owner.getName())
                .replace("%owner_name%", owner.getName())
                .replace("%target%", target == null ? owner.getName() : target.getName())
                .replace("%first_owner%", firstOwnerName)
                .replace("%owners_count%", ownersCount)
                .replace("%mode%", botOptions == null ? BotType.SINGLE.name().toLowerCase(Locale.ROOT) : botOptions.getBotType().name().toLowerCase(Locale.ROOT));

        replaced = applyPlaceholderApiIfAvailable(owner, replaced);
        return sanitizeProfileName(replaced);
    }

    private GameProfile resolveProfile(Player owner, UUID botUUID, String botName, BotOptions botOptions) {
        if (botOptions == null || botOptions.getCreationSource() != BotCreationSource.API) {
            return BotFactory.createProfile(owner, botUUID, botName);
        }

        BotSkin skin = botOptions.getBotSkin();
        if (skin == null) {
            return BotFactory.createProfile(owner, botUUID, botName);
        }

        BotSkinSource source = skin.source();
        if (source == null) {
            return BotFactory.createProfile(owner, botUUID, botName);
        }

        return switch (source) {
            case RANDOM -> BotFactory.createRandomProfile(botUUID, botName);
            case OWNER -> BotFactory.createProfile(owner, botUUID, botName);
            case FIRST_TEAM_OWNER -> {
                Player firstOwner = resolveFirstTeamOwnerPlayer(botOptions);
                if (firstOwner != null && firstOwner.isOnline()) {
                    yield BotFactory.createProfile(firstOwner, botUUID, botName);
                }
                yield BotFactory.createProfile(owner, botUUID, botName);
            }
            case PLAYER_REFERENCE -> {
                Player referenced = resolvePlayerReference(skin.playerReference());
                if (referenced != null && referenced.isOnline()) {
                    yield BotFactory.createProfile(referenced, botUUID, botName);
                }
                yield BotFactory.createProfile(owner, botUUID, botName);
            }
            case TEXTURE_VALUE -> BotFactory.createProfileWithTexture(
                    botUUID,
                    botName,
                    skin.textureValue(),
                    skin.textureSignature()
            );
            case TEXTURE_URL -> BotFactory.createProfileWithTexture(
                    botUUID,
                    botName,
                    buildTextureValueFromUrl(skin.textureUrl()),
                    null
            );
        };
    }

    private Player resolveFirstTeamOwnerPlayer(BotOptions botOptions) {
        if (botOptions == null) {
            return null;
        }

        for (UUID ownerUUID : botOptions.getTeamOwnerUUIDs()) {
            Player owner = Bukkit.getPlayer(ownerUUID);
            if (owner != null && owner.isOnline()) {
                return owner;
            }
        }
        return null;
    }

    private Player resolvePlayerReference(String reference) {
        if (reference == null || reference.isBlank()) {
            return null;
        }

        Player exact = Bukkit.getPlayerExact(reference);
        if (exact != null && exact.isOnline()) {
            return exact;
        }

        try {
            UUID uuid = UUID.fromString(reference);
            Player byUuid = Bukkit.getPlayer(uuid);
            if (byUuid != null && byUuid.isOnline()) {
                return byUuid;
            }
        } catch (IllegalArgumentException ignored) {
        }

        Player fuzzy = Bukkit.getPlayer(reference);
        if (fuzzy != null && fuzzy.isOnline()) {
            return fuzzy;
        }

        return null;
    }

    private String applyPlaceholderApiIfAvailable(Player owner, String input) {
        return PlaceholderApiSupport.apply(owner, input);
    }

    private static String sanitizeProfileName(String candidate) {
        if (candidate == null || candidate.isBlank()) {
            return "CrystalBot";
        }

        String noSectionColors = candidate.replaceAll("(?i)\\u00A7[0-9A-FK-ORX]", "");
        String noAmpersandColors = noSectionColors.replaceAll("(?i)&[0-9A-FK-ORX]", "");
        String safe = noAmpersandColors.replaceAll("[^A-Za-z0-9_]", "_");
        if (safe.isBlank()) {
            safe = "CrystalBot";
        }
        return safe.length() > 16 ? safe.substring(0, 16) : safe;
    }

    private static String buildTextureValueFromUrl(String textureUrl) {
        if (textureUrl == null || textureUrl.isBlank()) {
            return null;
        }

        String payload = "{\"textures\":{\"SKIN\":{\"url\":\"" + textureUrl + "\"}}}";
        return Base64.getEncoder().encodeToString(payload.getBytes(StandardCharsets.UTF_8));
    }

    public void despawn(Player owner) {
        if (owner == null) return;

        UUID ownerUUID = owner.getUniqueId();
        UUID botUUID = registry.getBotUUID(ownerUUID);
        if (botUUID == null) return;

        ServerPlayer handle = ((CraftPlayer) owner).getHandle();
        ServerLevel world = NMSBridgeManager.get().getServerLevel(handle);
        boolean removed = EntityUtils.removeEntity(world, botUUID);
        cleanupDespawnState(ownerUUID, botUUID, removed);
    }

    public void despawnByOwnerUUID(UUID ownerUUID) {
        if (ownerUUID == null) {
            return;
        }

        UUID botUUID = registry.getBotUUID(ownerUUID);
        if (botUUID == null) {
            return;
        }

        ITrainingBot bot = registry.getBot(ownerUUID);
        if (bot != null && bot.asPlayer().level() instanceof ServerLevel world) {
            boolean removed = EntityUtils.removeEntity(world, botUUID);
            cleanupDespawnState(ownerUUID, botUUID, removed);
            return;
        }

        Player owner = Bukkit.getPlayer(ownerUUID);
        if (owner != null) {
            despawn(owner);
            return;
        }

        boolean removed = EntityUtils.removeEntityInLoadedWorlds(botUUID);
        cleanupDespawnState(ownerUUID, botUUID, removed);
    }

    public void despawnAll() {
        registry.getAllBots().forEach((ownerUUID, bot) -> {
            Player owner = Bukkit.getPlayer(ownerUUID);
            if (owner != null && owner.isOnline()) {
                despawn(owner);
            } else {
                UUID botUUID = bot != null && bot.asPlayer() != null ? bot.asPlayer().getUUID() : registry.getBotUUID(ownerUUID);
                boolean removed = botUUID != null && EntityUtils.removeEntityInLoadedWorlds(botUUID);
                cleanupDespawnState(ownerUUID, botUUID, removed);
            }
        });
    }

    public void despawnInWorld(Player owner, org.bukkit.World fromWorld) {
        UUID ownerUUID = owner.getUniqueId();
        UUID botUUID = registry.getBotUUID(ownerUUID);
        if (botUUID == null) return;

        ServerLevel world = ((org.bukkit.craftbukkit.CraftWorld) fromWorld).getHandle();
        boolean removed = EntityUtils.removeEntity(world, botUUID);
        cleanupDespawnState(ownerUUID, botUUID, removed);
    }

    private void cleanupDespawnState(UUID ownerUUID, UUID botUUID, boolean removed) {
        if (!removed && botUUID != null) {
            MinecraftBotLogging.warn(plugin.getLogger(), "Bot", "Despawn cleanup could not find entity " + botUUID + " for owner " + ownerUUID);
        }

        if (botUUID != null) {
            NMSBridgeManager.get().removeFromProfileCache(botUUID);
            plugin.forgetCompatibilityBot(botUUID);
        }

        registry.removeBot(ownerUUID);
    }
}
