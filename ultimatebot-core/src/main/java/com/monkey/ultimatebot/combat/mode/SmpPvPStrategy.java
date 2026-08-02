package com.monkey.ultimatebot.combat.mode;

import com.monkey.ultimatebot.bot.ai.controllers.inventory.BotInventoryController;
import com.monkey.ultimatebot.common.model.CombatMode;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;

final class SmpPvPStrategy extends AbstractCombatModeStrategy {
    private static final int AXE_SLOT = BotInventoryController.TOTEM_SLOT;

    SmpPvPStrategy() {
        super(
                CombatMode.SMP,
                ModeKit.builder()
                        .slot(BotInventoryController.SWORD_SLOT, Items.NETHERITE_SWORD)
                        .slot(BotInventoryController.ENDERPEARL_SLOT, Items.BOW)
                        .slot(AXE_SLOT, Items.NETHERITE_AXE)
                        .slot(BotInventoryController.OBSIDIAN_SLOT, Items.SHIELD)
                        .slot(BotInventoryController.GOLDEN_APPLE_SLOT, Items.GOLDEN_APPLE, 64)
                        .build());
    }

    @Override
    protected void execute(CombatModeContext context, LivingEntity target) {
        context.aimAt(target);
        double distance = context.distanceTo(target);
        if (distance >= 7.0D && distance <= 26.0D && specialActionReady()) {
            context.inventory().switchToSlot(BotInventoryController.ENDERPEARL_SLOT);
            context.projectiles().fireArrow(target, context.tuning().aimAccuracy());
            delaySpecialAction(context);
            return;
        }
        int weaponSlot = context.random().nextDouble() < 0.32D ? AXE_SLOT : BotInventoryController.SWORD_SLOT;
        meleeOrMove(context, target, weaponSlot);
    }
}
