package com.monkey.ultimatebot.bot.ai.controllers.totem.helper;

import com.monkey.ultimatebot.bot.ai.controllers.totem.helper.interf.ITotemInventoryManager;
import com.monkey.ultimatebot.bot.ai.controllers.totem.helper.interf.ITotemStrategyHandler;
import org.bukkit.inventory.EquipmentSlot;

public class TotemStrategyHandler implements ITotemStrategyHandler {
    private final ITotemInventoryManager inventoryManager;

    public TotemStrategyHandler(ITotemInventoryManager inventoryManager) {
        this.inventoryManager = inventoryManager;
    }

    @Override
    public void handleUnlimitedTotems(TotemEquipmentState equipmentState, boolean isCombat) {
        if (isCombat) {
            if (!equipmentState.hasOffhandTotem()) {
                inventoryManager.equipTotem(EquipmentSlot.OFF_HAND);
            }
            if (equipmentState.hasMainhandTotem()) {
                inventoryManager.removeTotem(EquipmentSlot.HAND);
            }
        } else {
            if (equipmentState.getEquippedTotems() < 2) {
                if (!equipmentState.hasOffhandTotem()) {
                    inventoryManager.equipTotem(EquipmentSlot.OFF_HAND);
                } else if (!equipmentState.hasMainhandTotem()) {
                    inventoryManager.equipTotem(EquipmentSlot.HAND);
                }
            }
        }
    }

    @Override
    public void handleOneTotem(TotemEquipmentState equipmentState, boolean isCombat) {
        if (isCombat) {
            if (!equipmentState.hasOffhandTotem()) {
                inventoryManager.equipTotem(EquipmentSlot.OFF_HAND);
            }
            if (equipmentState.hasMainhandTotem()) {
                inventoryManager.removeTotem(EquipmentSlot.HAND);
            }
        } else {
            if (equipmentState.getEquippedTotems() == 0) {
                inventoryManager.equipTotem(EquipmentSlot.OFF_HAND);
            } else if (equipmentState.getEquippedTotems() == 2) {
                inventoryManager.removeTotem(EquipmentSlot.HAND);
            } else if (equipmentState.getEquippedTotems() == 1
                    && !equipmentState.hasOffhandTotem()
                    && equipmentState.hasMainhandTotem()) {
                inventoryManager.equipTotem(EquipmentSlot.OFF_HAND);
                inventoryManager.removeTotem(EquipmentSlot.HAND);
            }
        }
    }

    @Override
    public void handleMultipleTotems(int totemCount, TotemEquipmentState equipmentState, boolean isCombat) {
        if (isCombat) {
            if (!equipmentState.hasOffhandTotem()) {
                inventoryManager.equipTotem(EquipmentSlot.OFF_HAND);
            }
            if (equipmentState.hasMainhandTotem()) {
                inventoryManager.removeTotem(EquipmentSlot.HAND);
            }
        } else {
            int neededTotems = Math.min(2, totemCount) - equipmentState.getEquippedTotems();

            if (neededTotems > 0) {
                if (!equipmentState.hasOffhandTotem()) {
                    inventoryManager.equipTotem(EquipmentSlot.OFF_HAND);
                    neededTotems--;
                }
                if (neededTotems > 0 && !equipmentState.hasMainhandTotem()) {
                    inventoryManager.equipTotem(EquipmentSlot.HAND);
                }
            }
        }
    }

    @Override
    public void handleNoTotems(TotemEquipmentState equipmentState) {
        if (equipmentState.hasOffhandTotem()) {
            inventoryManager.removeTotem(EquipmentSlot.OFF_HAND);
        }
        if (equipmentState.hasMainhandTotem()) {
            inventoryManager.removeTotem(EquipmentSlot.HAND);
        }
    }
}
