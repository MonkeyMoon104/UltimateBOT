package it.coralmc.sandbox.bot.ai.controllers;

import it.coralmc.sandbox.SandboxTraining;
import it.coralmc.sandbox.bot.ai.TrainingBot;
import it.coralmc.sandbox.utils.ChatColorUtils;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class BotTotemController {

    private final Player bot;
    private final SandboxTraining plugin;
    private boolean warnedOutOfTotems = false;

    public BotTotemController(Player bot, SandboxTraining plugin) {
        this.bot = bot;
        this.plugin = plugin;
    }

    public void manageTotem() {
        if (!(bot instanceof TrainingBot trainingBot)) return;

        ItemStack offhand = bot.getItemBySlot(EquipmentSlot.OFFHAND);
        ItemStack mainhand = bot.getItemBySlot(EquipmentSlot.MAINHAND);

        int totemCount = trainingBot.getTotemCount();

        boolean hasOffhandTotem = hasTotemInSlot(offhand);
        boolean hasMainhandTotem = hasTotemInSlot(mainhand);

        int equippedTotems = (hasOffhandTotem ? 1 : 0) + (hasMainhandTotem ? 1 : 0);

        switch (getTotemState(totemCount)) {
            case UNLIMITED -> handleUnlimitedTotems(equippedTotems, hasOffhandTotem, hasMainhandTotem);
            case NONE -> handleNoTotems(hasOffhandTotem, hasMainhandTotem, trainingBot);
            case ONE -> handleOneTotem(equippedTotems, hasOffhandTotem, hasMainhandTotem);
            case MULTIPLE -> handleMultipleTotems(totemCount, equippedTotems, hasOffhandTotem, hasMainhandTotem);
        }
    }

    public void onTotemUsed() {
        if (bot instanceof TrainingBot trainingBot) {
            int totemCount = trainingBot.getTotemCount();

            if (totemCount > 0) {
                trainingBot.setTotemCount(totemCount - 1);
            }
        }
    }

    private boolean hasTotemInSlot(ItemStack itemStack) {
        return itemStack != null && !itemStack.isEmpty() && itemStack.is(Items.TOTEM_OF_UNDYING);
    }

    private TotemState getTotemState(int totemCount) {
        if (totemCount == -1) return TotemState.UNLIMITED;
        if (totemCount == 0) return TotemState.NONE;
        if (totemCount == 1) return TotemState.ONE;
        return TotemState.MULTIPLE;
    }

    private void handleUnlimitedTotems(int equippedTotems, boolean hasOffhandTotem, boolean hasMainhandTotem) {
    /*
    if (equippedTotems < 2) {
        if (!hasOffhandTotem) {
            equipTotem(EquipmentSlot.OFFHAND);
        } else if (!hasMainhandTotem) {
            equipTotem(EquipmentSlot.MAINHAND);
        }
    }
    */

        if (!hasOffhandTotem) {
            equipTotem(EquipmentSlot.OFFHAND);
        }
        if (hasMainhandTotem) {
            removeTotem(EquipmentSlot.MAINHAND);
        }

        warnedOutOfTotems = false;
    }

    private void handleOneTotem(int equippedTotems, boolean hasOffhandTotem, boolean hasMainhandTotem) {
    /*
    if (equippedTotems == 0) {
        equipTotem(EquipmentSlot.OFFHAND);
    } else if (equippedTotems == 2) {
        removeTotem(EquipmentSlot.MAINHAND);
    } else if (equippedTotems == 1 && !hasOffhandTotem && hasMainhandTotem) {
        equipTotem(EquipmentSlot.OFFHAND);
        removeTotem(EquipmentSlot.MAINHAND);
    }
    */

        if (!hasOffhandTotem) {
            equipTotem(EquipmentSlot.OFFHAND);
        }
        if (hasMainhandTotem) {
            removeTotem(EquipmentSlot.MAINHAND);
        }

        warnedOutOfTotems = false;
    }

    private void handleMultipleTotems(int totemCount, int equippedTotems, boolean hasOffhandTotem, boolean hasMainhandTotem) {
    /*
    int neededTotems = Math.min(2, totemCount) - equippedTotems;

    if (neededTotems > 0) {
        if (!hasOffhandTotem) {
            equipTotem(EquipmentSlot.OFFHAND);
            neededTotems--;
        }
        if (neededTotems > 0 && !hasMainhandTotem) {
            equipTotem(EquipmentSlot.MAINHAND);
        }
    }
    */
        if (!hasOffhandTotem) {
            equipTotem(EquipmentSlot.OFFHAND);
        }
        if (hasMainhandTotem) {
            removeTotem(EquipmentSlot.MAINHAND);
        }

        warnedOutOfTotems = false;
    }


    private void handleNoTotems(boolean hasOffhandTotem, boolean hasMainhandTotem, TrainingBot trainingBot) {
        if (hasOffhandTotem) {
            removeTotem(EquipmentSlot.OFFHAND);
        }
        if (hasMainhandTotem) {
            removeTotem(EquipmentSlot.MAINHAND);
        }

        if (!warnedOutOfTotems) {
            sendTotemWarning(trainingBot);
            warnedOutOfTotems = true;
        }
    }

    private void equipTotem(EquipmentSlot slot) {
        bot.setItemSlot(slot, new ItemStack(Items.TOTEM_OF_UNDYING));
    }

    private void removeTotem(EquipmentSlot slot) {
        bot.setItemSlot(slot, ItemStack.EMPTY);
    }

    private void sendTotemWarning(TrainingBot trainingBot) {
        var player = trainingBot.getTargetPlayer();
        if (player != null && player.isOnline()) {
            String msg = plugin.getConfig()
                    .getString("bot.totem-finish", "[%botname%] Running out of totems");
            String botName = plugin.getConfig().getString("bot.name", "CrystalBot");
            msg = msg.replace("%botname%", botName);
            player.sendMessage(ChatColorUtils.translate(msg));
        }
    }

    public int getEquippedTotemCount() {
        ItemStack offhand = bot.getItemBySlot(EquipmentSlot.OFFHAND);
        ItemStack mainhand = bot.getItemBySlot(EquipmentSlot.MAINHAND);

        return (hasTotemInSlot(offhand) ? 1 : 0) + (hasTotemInSlot(mainhand) ? 1 : 0);
    }

    public void forceEquipTotems(int count) {
        count = Math.max(0, Math.min(2, count));

        removeTotem(EquipmentSlot.OFFHAND);
        removeTotem(EquipmentSlot.MAINHAND);

        if (count >= 1) {
            equipTotem(EquipmentSlot.OFFHAND);
        }
        if (count >= 2) {
            equipTotem(EquipmentSlot.MAINHAND);
        }
    }

    private enum TotemState {
        UNLIMITED,
        NONE,
        ONE,
        MULTIPLE
    }
}