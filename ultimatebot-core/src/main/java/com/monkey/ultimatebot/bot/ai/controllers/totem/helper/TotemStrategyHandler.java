package com.monkey.ultimatebot.bot.ai.controllers.totem.helper;

import com.monkey.ultimatebot.bot.ai.controllers.totem.helper.interf.ITotemInventoryManager;
import com.monkey.ultimatebot.bot.ai.controllers.totem.helper.interf.ITotemStrategyHandler;
import net.minecraft.world.entity.EquipmentSlot;

public class TotemStrategyHandler implements ITotemStrategyHandler {
    private final ITotemInventoryManager inventoryManager;

    public TotemStrategyHandler(ITotemInventoryManager inventoryManager) {
        this.inventoryManager = inventoryManager;
    }

    @Override
    public void handleUnlimitedTotems(TotemEquipmentState equipmentState, boolean isCombat) {
        if (isCombat) {
            if (!equipmentState.hasOffhandTotem()) {
                inventoryManager.equipTotem(EquipmentSlot.OFFHAND);
            }
            if (equipmentState.hasMainhandTotem()) {
                inventoryManager.removeTotem(EquipmentSlot.MAINHAND);
            }
        } else {
            if (equipmentState.getEquippedTotems() < 2) {
                if (!equipmentState.hasOffhandTotem()) {
                    inventoryManager.equipTotem(EquipmentSlot.OFFHAND);
                } else if (!equipmentState.hasMainhandTotem()) {
                    inventoryManager.equipTotem(EquipmentSlot.MAINHAND);
                }
            }
        }
    }

    @Override
    public void handleOneTotem(TotemEquipmentState equipmentState, boolean isCombat) {
        if (isCombat) {
            if (!equipmentState.hasOffhandTotem()) {
                inventoryManager.equipTotem(EquipmentSlot.OFFHAND);
            }
            if (equipmentState.hasMainhandTotem()) {
                inventoryManager.removeTotem(EquipmentSlot.MAINHAND);
            }
        } else {
            if (equipmentState.getEquippedTotems() == 0) {
                inventoryManager.equipTotem(EquipmentSlot.OFFHAND);
            } else if (equipmentState.getEquippedTotems() == 2) {
                inventoryManager.removeTotem(EquipmentSlot.MAINHAND);
            } else if (equipmentState.getEquippedTotems() == 1
                    && !equipmentState.hasOffhandTotem()
                    && equipmentState.hasMainhandTotem()) {
                inventoryManager.equipTotem(EquipmentSlot.OFFHAND);
                inventoryManager.removeTotem(EquipmentSlot.MAINHAND);
            }
        }
    }

    @Override
    public void handleMultipleTotems(int totemCount, TotemEquipmentState equipmentState, boolean isCombat) {
        if (isCombat) {
            if (!equipmentState.hasOffhandTotem()) {
                inventoryManager.equipTotem(EquipmentSlot.OFFHAND);
            }
            if (equipmentState.hasMainhandTotem()) {
                inventoryManager.removeTotem(EquipmentSlot.MAINHAND);
            }
        } else {
            int neededTotems = Math.min(2, totemCount) - equipmentState.getEquippedTotems();

            if (neededTotems > 0) {
                if (!equipmentState.hasOffhandTotem()) {
                    inventoryManager.equipTotem(EquipmentSlot.OFFHAND);
                    neededTotems--;
                }
                if (neededTotems > 0 && !equipmentState.hasMainhandTotem()) {
                    inventoryManager.equipTotem(EquipmentSlot.MAINHAND);
                }
            }
        }
    }

    @Override
    public void handleNoTotems(TotemEquipmentState equipmentState) {
        if (equipmentState.hasOffhandTotem()) {
            inventoryManager.removeTotem(EquipmentSlot.OFFHAND);
        }
        if (equipmentState.hasMainhandTotem()) {
            inventoryManager.removeTotem(EquipmentSlot.MAINHAND);
        }
    }
}
