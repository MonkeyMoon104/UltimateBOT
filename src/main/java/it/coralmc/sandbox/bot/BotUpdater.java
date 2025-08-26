package it.coralmc.sandbox.bot;

import it.coralmc.sandbox.bot.ai.TrainingBot;
import it.coralmc.sandbox.utils.equipment.BotEquipmentUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BotUpdater {

    private final BotRegistry registry;

    public BotUpdater(BotRegistry registry) {
        this.registry = registry;
    }

    public void updateArmor(UUID ownerUUID,
                            Map<org.bukkit.inventory.EquipmentSlot, org.bukkit.inventory.ItemStack> armorMap,
                            Map<org.bukkit.inventory.EquipmentSlot, Boolean> blastProtectionMap) {
        BotEquipmentUtils.updateBotArmor(ownerUUID, armorMap, blastProtectionMap, registry);
    }

    public void updateArmor(UUID ownerUUID,
                            Map<org.bukkit.inventory.EquipmentSlot, org.bukkit.inventory.ItemStack> armorMap) {
        updateArmor(ownerUUID, armorMap, new HashMap<>());
    }

    public void updateTotem(UUID ownerUUID, int totemCount) {
        TrainingBot bot = registry.getAllBots().get(ownerUUID);
        if (bot != null) {
            bot.setTotemCount(totemCount);
            bot.getBotAI().manageTotem();
        }
    }

    public void updateFollow(UUID ownerUUID, boolean follow) {
        TrainingBot bot = registry.getAllBots().get(ownerUUID);
        if (bot != null) {
            bot.setFollow(follow);
        }
    }

    public void updateInventorySlot(UUID ownerUUID, int slot, org.bukkit.inventory.ItemStack item) {
        TrainingBot bot = registry.getAllBots().get(ownerUUID);
        if (bot != null) {
            net.minecraft.world.item.ItemStack nmsItem = org.bukkit.craftbukkit.inventory.CraftItemStack.asNMSCopy(item);
            bot.getBotAI().getInventoryController().setItem(slot, nmsItem);
        }
    }

    public void switchBotSlot(UUID ownerUUID, int slot) {
        TrainingBot bot = registry.getAllBots().get(ownerUUID);
        if (bot != null) {
            bot.getBotAI().getInventoryController().switchToSlot(slot);
        }
    }

    public void addEnderpearls(UUID ownerUUID, int count) {
        TrainingBot bot = registry.getAllBots().get(ownerUUID);
        if (bot != null) {
            bot.getBotAI().getInventoryController().addEnderpearls(count);
        }
    }

    public int getBotEnderpearlCount(UUID ownerUUID) {
        TrainingBot bot = registry.getAllBots().get(ownerUUID);
        if (bot != null) {
            return bot.getBotAI().getInventoryController().getItemCount(net.minecraft.world.item.Items.ENDER_PEARL);
        }
        return 0;
    }
}
