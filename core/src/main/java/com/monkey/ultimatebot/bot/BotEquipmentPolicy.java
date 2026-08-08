package com.monkey.ultimatebot.bot;

import com.monkey.ultimatebot.api.model.configuration.BotEquipmentSlot;
import com.monkey.ultimatebot.api.model.configuration.BotEquipmentSlotMode;
import com.monkey.ultimatebot.api.model.configuration.BotEquipmentSlotSetting;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.nms.NMSBridgeManager;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/** Enforces fixed item and fixed-empty equipment settings after bot AI actions. */
public final class BotEquipmentPolicy {
    private BotEquipmentPolicy() {}

    public static boolean enforce(ITrainingBot bot, BotOptions options) {
        Objects.requireNonNull(bot, "bot");
        Objects.requireNonNull(options, "options");
        Map<EquipmentSlot, ItemStack> changes = new EnumMap<>(EquipmentSlot.class);

        for (Map.Entry<BotEquipmentSlot, BotEquipmentSlotSetting> entry :
                options.getEquipmentSlotSettings().entrySet()) {
            BotEquipmentSlotSetting setting = entry.getValue();
            if (setting.mode() == BotEquipmentSlotMode.DEFAULT) {
                continue;
            }

            EquipmentSlot bukkitSlot = toBukkitSlot(entry.getKey());
            ItemStack desired = setting.mode() == BotEquipmentSlotMode.EMPTY
                    ? ItemStack.empty()
                    : Objects.requireNonNull(setting.item(), "item").clone();
            ItemStack current = bot.getItem(bukkitSlot);
            if (current != null && current.isSimilar(desired) && current.getAmount() == desired.getAmount()) {
                continue;
            }

            bot.setItem(bukkitSlot, desired.clone());
            changes.put(bukkitSlot, desired.clone());
        }

        broadcast(bot, changes);
        return !changes.isEmpty();
    }

    public static void restoreDefault(ITrainingBot bot, BotOptions options, BotEquipmentSlot slot) {
        Objects.requireNonNull(bot, "bot");
        Objects.requireNonNull(options, "options");
        Objects.requireNonNull(slot, "slot");
        switch (slot) {
            case MAIN_HAND -> {
                int selected = bot.getBotAI().getInventoryController().getCurrentSlot();
                bot.getBotAI().getInventoryController().switchToSlot(selected);
            }
            case OFF_HAND -> bot.getBotAI().manageTotem();
            case HEAD, CHEST, LEGS, FEET -> {
                EquipmentSlot bukkitSlot = toBukkitSlot(slot);
                ItemStack configured = options.getArmor().get(bukkitSlot);
                ItemStack restored;
                if (configured == null) {
                    restored = ItemStack.empty();
                } else {
                    ItemStack item = configured.clone();
                    com.monkey.ultimatebot.utils.equipment.BotEquipmentUtils.applyArmorEnchants(
                            item, options.getBlast().getOrDefault(bukkitSlot, false));
                    restored = item;
                }
                bot.setItem(bukkitSlot, restored.clone());
                broadcast(bot, Map.of(bukkitSlot, restored.clone()));
            }
        }
    }

    private static void broadcast(ITrainingBot bot, Map<EquipmentSlot, ItemStack> changes) {
        if (changes.isEmpty()) {
            return;
        }
        NMSBridgeManager.get().broadcastEquipment(bot, changes);
    }

    private static EquipmentSlot toBukkitSlot(BotEquipmentSlot slot) {
        return switch (slot) {
            case MAIN_HAND -> EquipmentSlot.HAND;
            case OFF_HAND -> EquipmentSlot.OFF_HAND;
            case HEAD -> EquipmentSlot.HEAD;
            case CHEST -> EquipmentSlot.CHEST;
            case LEGS -> EquipmentSlot.LEGS;
            case FEET -> EquipmentSlot.FEET;
        };
    }
}
