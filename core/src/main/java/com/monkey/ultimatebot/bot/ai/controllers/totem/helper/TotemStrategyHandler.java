package com.monkey.ultimatebot.bot.ai.controllers.totem.helper;

import com.monkey.ultimatebot.bot.ai.controllers.totem.helper.interf.ITotemInventoryManager;
import com.monkey.ultimatebot.bot.ai.controllers.totem.helper.interf.ITotemStrategyHandler;
import com.monkey.ultimatebot.common.model.EquipmentSlotKind;
import com.monkey.ultimatebot.access.item.EquipmentSlotAccess;

public class TotemStrategyHandler implements ITotemStrategyHandler {
    private final ITotemInventoryManager inventoryManager;

    public TotemStrategyHandler(ITotemInventoryManager inventoryManager) {
        this.inventoryManager = inventoryManager;
    }

    @Override
    public void handleUnlimitedTotems(TotemEquipmentState equipmentState, boolean isCombat) {
        EquipmentSlotKind offHand = EquipmentSlotAccess.offHand();
        if (offHand == null) {
            return;
        }
        if (isCombat) {
            if (!equipmentState.hasOffhandTotem()) {
                inventoryManager.equipTotem(offHand);
            }
            if (equipmentState.hasMainhandTotem()) {
                inventoryManager.removeTotem(EquipmentSlotKind.HAND);
            }
        } else {
            if (equipmentState.getEquippedTotems() < 2) {
                if (!equipmentState.hasOffhandTotem()) {
                    inventoryManager.equipTotem(offHand);
                } else if (!equipmentState.hasMainhandTotem()) {
                    inventoryManager.equipTotem(EquipmentSlotKind.HAND);
                }
            }
        }
    }

    @Override
    public void handleOneTotem(TotemEquipmentState equipmentState, boolean isCombat) {
        EquipmentSlotKind offHand = EquipmentSlotAccess.offHand();
        if (offHand == null) {
            return;
        }
        if (isCombat) {
            if (!equipmentState.hasOffhandTotem()) {
                inventoryManager.equipTotem(offHand);
            }
            if (equipmentState.hasMainhandTotem()) {
                inventoryManager.removeTotem(EquipmentSlotKind.HAND);
            }
        } else {
            if (equipmentState.getEquippedTotems() == 0) {
                inventoryManager.equipTotem(offHand);
            } else if (equipmentState.getEquippedTotems() == 2) {
                inventoryManager.removeTotem(EquipmentSlotKind.HAND);
            } else if (equipmentState.getEquippedTotems() == 1
                    && !equipmentState.hasOffhandTotem()
                    && equipmentState.hasMainhandTotem()) {
                inventoryManager.equipTotem(offHand);
                inventoryManager.removeTotem(EquipmentSlotKind.HAND);
            }
        }
    }

    @Override
    public void handleMultipleTotems(int totemCount, TotemEquipmentState equipmentState, boolean isCombat) {
        EquipmentSlotKind offHand = EquipmentSlotAccess.offHand();
        if (offHand == null) {
            return;
        }
        if (isCombat) {
            if (!equipmentState.hasOffhandTotem()) {
                inventoryManager.equipTotem(offHand);
            }
            if (equipmentState.hasMainhandTotem()) {
                inventoryManager.removeTotem(EquipmentSlotKind.HAND);
            }
        } else {
            int neededTotems = Math.min(2, totemCount) - equipmentState.getEquippedTotems();

            if (neededTotems > 0) {
                if (!equipmentState.hasOffhandTotem()) {
                    inventoryManager.equipTotem(offHand);
                    neededTotems--;
                }
                if (neededTotems > 0 && !equipmentState.hasMainhandTotem()) {
                    inventoryManager.equipTotem(EquipmentSlotKind.HAND);
                }
            }
        }
    }

    @Override
    public void handleNoTotems(TotemEquipmentState equipmentState) {
        EquipmentSlotKind offHand = EquipmentSlotAccess.offHand();
        if (equipmentState.hasOffhandTotem() && offHand != null) {
            inventoryManager.removeTotem(offHand);
        }
        if (equipmentState.hasMainhandTotem()) {
            inventoryManager.removeTotem(EquipmentSlotKind.HAND);
        }
    }
}
