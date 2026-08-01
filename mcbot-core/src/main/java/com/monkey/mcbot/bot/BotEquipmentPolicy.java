package com.monkey.mcbot.bot;

import com.mojang.datafixers.util.Pair;
import com.monkey.mcbot.api.model.BotEquipmentSlot;
import com.monkey.mcbot.api.model.BotEquipmentSlotMode;
import com.monkey.mcbot.api.model.BotEquipmentSlotSetting;
import com.monkey.mcbot.bot.ai.ITrainingBot;
import com.monkey.mcbot.nms.NMSBridgeManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;

/** Enforces fixed item and fixed-empty equipment settings after bot AI actions. */
public final class BotEquipmentPolicy {
    private BotEquipmentPolicy() {}

    public static boolean enforce(ITrainingBot bot, BotOptions options) {
        Objects.requireNonNull(bot, "bot");
        Objects.requireNonNull(options, "options");
        List<Pair<EquipmentSlot, ItemStack>> changes = new ArrayList<>();

        for (Map.Entry<BotEquipmentSlot, BotEquipmentSlotSetting> entry :
                options.getEquipmentSlotSettings().entrySet()) {
            BotEquipmentSlotSetting setting = entry.getValue();
            if (setting.mode() == BotEquipmentSlotMode.DEFAULT) {
                continue;
            }

            EquipmentSlot nmsSlot = toNmsSlot(entry.getKey());
            ItemStack desired = setting.mode() == BotEquipmentSlotMode.EMPTY
                    ? ItemStack.EMPTY
                    : CraftItemStack.asNMSCopy(Objects.requireNonNull(setting.item(), "item"));
            ItemStack current = bot.asPlayer().getItemBySlot(nmsSlot);
            if (ItemStack.matches(current, desired)) {
                continue;
            }

            bot.asPlayer().setItemSlot(nmsSlot, desired.copy());
            changes.add(Pair.of(nmsSlot, desired.copy()));
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
                org.bukkit.inventory.EquipmentSlot bukkitSlot = toBukkitSlot(slot);
                org.bukkit.inventory.ItemStack configured = options.getArmor().get(bukkitSlot);
                ItemStack restored;
                if (configured == null) {
                    restored = ItemStack.EMPTY;
                } else {
                    org.bukkit.inventory.ItemStack item = configured.clone();
                    com.monkey.mcbot.utils.equipment.BotEquipmentUtils.applyArmorEnchants(
                            item, options.getBlast().getOrDefault(bukkitSlot, false));
                    restored = CraftItemStack.asNMSCopy(item);
                }
                EquipmentSlot nmsSlot = toNmsSlot(slot);
                bot.asPlayer().setItemSlot(nmsSlot, restored.copy());
                broadcast(bot, List.of(Pair.of(nmsSlot, restored.copy())));
            }
        }
    }

    private static void broadcast(ITrainingBot bot, List<Pair<EquipmentSlot, ItemStack>> changes) {
        if (changes.isEmpty()) {
            return;
        }
        ClientboundSetEquipmentPacket packet =
                NMSBridgeManager.get().createEquipmentPacket(bot.asPlayer().getId(), changes);
        org.bukkit.World botWorld = bot.asPlayer().getBukkitEntity().getWorld();
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (!viewer.getWorld().getUID().equals(botWorld.getUID())) {
                continue;
            }
            ServerPlayer handle = ((CraftPlayer) viewer).getHandle();
            handle.connection.send(packet);
        }
    }

    private static EquipmentSlot toNmsSlot(BotEquipmentSlot slot) {
        return switch (slot) {
            case MAIN_HAND -> EquipmentSlot.MAINHAND;
            case OFF_HAND -> EquipmentSlot.OFFHAND;
            case HEAD -> EquipmentSlot.HEAD;
            case CHEST -> EquipmentSlot.CHEST;
            case LEGS -> EquipmentSlot.LEGS;
            case FEET -> EquipmentSlot.FEET;
        };
    }

    private static org.bukkit.inventory.EquipmentSlot toBukkitSlot(BotEquipmentSlot slot) {
        return switch (slot) {
            case MAIN_HAND -> org.bukkit.inventory.EquipmentSlot.HAND;
            case OFF_HAND -> org.bukkit.inventory.EquipmentSlot.OFF_HAND;
            case HEAD -> org.bukkit.inventory.EquipmentSlot.HEAD;
            case CHEST -> org.bukkit.inventory.EquipmentSlot.CHEST;
            case LEGS -> org.bukkit.inventory.EquipmentSlot.LEGS;
            case FEET -> org.bukkit.inventory.EquipmentSlot.FEET;
        };
    }
}
