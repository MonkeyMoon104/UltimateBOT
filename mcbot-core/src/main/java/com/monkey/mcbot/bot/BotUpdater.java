package com.monkey.mcbot.bot;

import com.monkey.mcbot.bot.ai.ITrainingBot;
import com.monkey.mcbot.utils.equipment.BotEquipmentUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BotUpdater {

    private final BotRegistry registry;

    public BotUpdater(BotRegistry registry) {
        this.registry = java.util.Objects.requireNonNull(registry, "registry");
    }

    public void updateArmor(
            UUID ownerUUID,
            Map<org.bukkit.inventory.EquipmentSlot, org.bukkit.inventory.ItemStack> armorMap,
            Map<org.bukkit.inventory.EquipmentSlot, Boolean> blastProtectionMap) {
        BotEquipmentUtils.updateBotArmor(ownerUUID, armorMap, blastProtectionMap, registry);
    }

    public void updateArmor(
            UUID ownerUUID, Map<org.bukkit.inventory.EquipmentSlot, org.bukkit.inventory.ItemStack> armorMap) {
        updateArmor(ownerUUID, armorMap, new HashMap<>());
    }

    private ITrainingBot getBot(UUID ownerUUID) {
        return registry.getBot(ownerUUID);
    }

    public void updateTotem(UUID ownerUUID, int totemCount) {
        ITrainingBot bot = getBot(ownerUUID);
        if (bot != null) {
            bot.setTotemCount(totemCount);
            bot.getBotAI().manageTotem();
        }
    }

    public void updateFollow(UUID ownerUUID, boolean follow) {
        ITrainingBot bot = getBot(ownerUUID);
        if (bot != null) {
            bot.setFollow(follow);
        }
    }

    public void updateCombat(UUID ownerUUID, boolean combat) {
        ITrainingBot bot = getBot(ownerUUID);
        if (bot != null) {
            bot.setCombat(combat);
        }
    }

    public void updateInventorySlot(UUID ownerUUID, int slot, org.bukkit.inventory.ItemStack item) {
        ITrainingBot bot = getBot(ownerUUID);
        if (bot != null) {
            net.minecraft.world.item.ItemStack nmsItem =
                    org.bukkit.craftbukkit.inventory.CraftItemStack.asNMSCopy(item);
            bot.getBotAI().getInventoryController().setItem(slot, nmsItem);
        }
    }

    public void switchBotSlot(UUID ownerUUID, int slot) {
        ITrainingBot bot = getBot(ownerUUID);
        if (bot != null) {
            bot.getBotAI().getInventoryController().switchToSlot(slot);
        }
    }

    public void addEnderpearls(UUID ownerUUID, int count) {
        ITrainingBot bot = getBot(ownerUUID);
        if (bot != null) {
            bot.getBotAI().getInventoryController().addEnderpearls(count);
        }
    }

    public int getBotEnderpearlCount(UUID ownerUUID) {
        ITrainingBot bot = getBot(ownerUUID);
        if (bot != null) {
            return bot.getBotAI().getInventoryController().getItemCount(net.minecraft.world.item.Items.ENDER_PEARL);
        }
        return 0;
    }

    public boolean getSwordSlot(UUID ownerUUID) {
        ITrainingBot bot = getBot(ownerUUID);
        if (bot != null) {
            return bot.getBotAI().getInventoryController().isHoldingSword();
        }
        return false;
    }

    public boolean getEpearlSlot(UUID ownerUUID) {
        ITrainingBot bot = getBot(ownerUUID);
        if (bot != null) {
            return bot.getBotAI().getInventoryController().isHoldingEnderpearl();
        }
        return false;
    }
}
