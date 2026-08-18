package com.monkey.ultimatebot.bot;

import com.monkey.ultimatebot.common.model.EquipmentSlotKind;
import com.monkey.ultimatebot.compat.ItemStackAccess;

import com.monkey.ultimatebot.common.util.ImmutableCollections;

import com.monkey.ultimatebot.api.model.configuration.BotEquipmentSlot;
import com.monkey.ultimatebot.api.model.configuration.BotEquipmentSlotMode;
import com.monkey.ultimatebot.api.model.configuration.BotEquipmentSlotSetting;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.compat.EquipmentSlotAccess;
import com.monkey.ultimatebot.nms.NMSBridgeManager;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.Nullable;

/** Enforces fixed item and fixed-empty equipment settings after bot AI actions. */
public final class BotEquipmentPolicy {
    private BotEquipmentPolicy() {}

    public static boolean enforce(ITrainingBot bot, BotOptions options) {
        Objects.requireNonNull(bot, "bot");
        Objects.requireNonNull(options, "options");
        Map<EquipmentSlotKind, ItemStack> changes = new EnumMap<>(EquipmentSlotKind.class);

        for (Map.Entry<BotEquipmentSlot, BotEquipmentSlotSetting> entry :
                options.getEquipmentSlotSettings().entrySet()) {
            BotEquipmentSlotSetting setting = entry.getValue();
            if (setting.mode() == BotEquipmentSlotMode.DEFAULT) {
                continue;
            }

            EquipmentSlotKind bukkitSlot = toBukkitSlot(entry.getKey());
            if (bukkitSlot == null) {
                continue;
            }
            ItemStack desired = setting.mode() == BotEquipmentSlotMode.EMPTY
                    ? ItemStackAccess.empty()
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
            case MAIN_HAND:
                int selected = bot.getBotAI().getInventoryController().getCurrentSlot();
                                bot.getBotAI().getInventoryController().switchToSlot(selected);
                break;
            case OFF_HAND:
                bot.getBotAI().manageTotem();
                break;
            case HEAD:
            case CHEST:
            case LEGS:
            case FEET:
                EquipmentSlotKind bukkitSlot = toBukkitSlot(slot);
                if (bukkitSlot == null) {
                    break;
                }
                                ItemStack configured = options.getArmor().get(bukkitSlot);
                                ItemStack restored;
                                if (configured == null) {
                                    restored = ItemStackAccess.empty();
                                } else {
                                    ItemStack item = configured.clone();
                                    com.monkey.ultimatebot.utils.equipment.BotEquipmentUtils.applyArmorEnchants(
                                            item, options.getBlast().getOrDefault(bukkitSlot, false));
                                    restored = item;
                                }
                                bot.setItem(bukkitSlot, restored.clone());
                                broadcast(bot, ImmutableCollections.mapOf(bukkitSlot, restored.clone()));
                break;
        }
        throw new IllegalStateException("Unexpected switch value");
    }

    private static void broadcast(ITrainingBot bot, Map<EquipmentSlotKind, ItemStack> changes) {
        if (changes.isEmpty()) {
            return;
        }
        NMSBridgeManager.get().broadcastEquipment(bot, changes);
    }

    private static @Nullable EquipmentSlotKind toBukkitSlot(BotEquipmentSlot slot) {
                switch (slot) {
            case MAIN_HAND:
                return EquipmentSlotKind.HAND;
            case OFF_HAND:
                return EquipmentSlotAccess.offHand();
            case HEAD:
                return EquipmentSlotKind.HEAD;
            case CHEST:
                return EquipmentSlotKind.CHEST;
            case LEGS:
                return EquipmentSlotKind.LEGS;
            case FEET:
                return EquipmentSlotKind.FEET;
            default:
                throw new IllegalStateException("Unexpected equipment slot: " + slot);
        }
    }
}
