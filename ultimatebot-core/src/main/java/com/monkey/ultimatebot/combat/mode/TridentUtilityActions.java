package com.monkey.ultimatebot.combat.mode;

import com.monkey.ultimatebot.bot.ai.controllers.inventory.BotInventoryController;
import java.util.Objects;
import net.minecraft.world.entity.LivingEntity;
import org.bukkit.Location;
import org.bukkit.Material;
import org.jspecify.annotations.Nullable;

final class TridentUtilityActions {
    private static final int WATER_SLOT = BotInventoryController.TOTEM_SLOT;
    private static final int WEB_SLOT = BotInventoryController.OBSIDIAN_SLOT;
    private static final int SPONGE_SLOT = BotInventoryController.CRYSTAL_SLOT;

    private @Nullable Location waterLocation;
    private @Nullable Location spongeLocation;

    void reset() {
        waterLocation = null;
        spongeLocation = null;
    }

    void selectWater(CombatModeContext context) {
        context.inventory().switchToSlot(WATER_SLOT);
    }

    void placeWater(CombatModeContext context) {
        if (!context.inventory().consumeItem(WATER_SLOT)) {
            return;
        }
        Location placement = Objects.requireNonNull(context.bukkitBot().getLocation(), "bot location")
                .getBlock()
                .getLocation();
        if (context.placeTemporaryBlock(placement, Material.WATER)) {
            waterLocation = placement;
        }
    }

    boolean isWaterPrepared() {
        return waterLocation != null;
    }

    void placeWeb(CombatModeContext context, LivingEntity target) {
        context.inventory().switchToSlot(WEB_SLOT);
        Location targetBlock = Objects.requireNonNull(target.getBukkitEntity().getLocation(), "target location")
                .getBlock()
                .getLocation();
        if (context.canPlaceCombatBlock(targetBlock, Material.COBWEB)
                && context.placeCombatBlock(targetBlock, Material.COBWEB, WEB_SLOT)) {
            context.actions().swingMainHand();
        }
    }

    void placeSponge(CombatModeContext context, LivingEntity target) {
        context.inventory().switchToSlot(SPONGE_SLOT);
        Location targetBlock = Objects.requireNonNull(target.getBukkitEntity().getLocation(), "target location")
                .getBlock()
                .getLocation();
        Location adjacent = targetBlock.clone().add(1.0D, 0.0D, 0.0D);
        if (context.inventory().consumeItem(SPONGE_SLOT) && context.placeTemporaryBlock(adjacent, Material.SPONGE)) {
            spongeLocation = adjacent;
            context.actions().swingMainHand();
        }
    }

    void restoreWater(CombatModeContext context) {
        if (waterLocation != null) {
            context.restoreTemporaryBlock(waterLocation);
            waterLocation = null;
        }
    }

    void restoreSponge(CombatModeContext context) {
        if (spongeLocation != null) {
            context.restoreTemporaryBlock(spongeLocation);
            spongeLocation = null;
        }
    }
}
