package com.monkey.ultimatebot.combat.mode.strategy;

import com.monkey.ultimatebot.bot.ai.controllers.inventory.BotInventoryController;
import com.monkey.ultimatebot.combat.mode.runtime.AbstractCombatModeStrategy;
import com.monkey.ultimatebot.combat.mode.runtime.CombatModeContext;
import com.monkey.ultimatebot.combat.mode.runtime.ModeKit;
import com.monkey.ultimatebot.common.model.CombatMode;
import com.monkey.ultimatebot.nms.NMSBridgeManager;
import com.monkey.ultimatebot.compat.EntityLookupAccess;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.Location;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

public final class CrystalPvPStrategy extends AbstractCombatModeStrategy {
    private @Nullable UUID mobCrystalId;
    private int mobCrystalTicks;

    public CrystalPvPStrategy() {
        super(
                CombatMode.CRYSTAL,
                ModeKit.builder()
                        .slot(BotInventoryController.SWORD_SLOT, "NETHERITE_SWORD", Material.DIAMOND_SWORD, 1)
                        .slot(BotInventoryController.ENDERPEARL_SLOT, Material.ENDER_PEARL, 16)
                        .slot(BotInventoryController.OBSIDIAN_SLOT, Material.OBSIDIAN, 64)
                        .slot(BotInventoryController.CRYSTAL_SLOT, "END_CRYSTAL", Material.GHAST_TEAR, 64)
                        .slot(BotInventoryController.ANCHOR_SLOT, "RESPAWN_ANCHOR", Material.OBSIDIAN, 64)
                        .slot(BotInventoryController.GLOW_SLOT, Material.GLOWSTONE, 64)
                        .slot(BotInventoryController.GOLDEN_APPLE_SLOT, "ENCHANTED_GOLDEN_APPLE", Material.GOLDEN_APPLE, 64)
                        .build());
    }

    @Override
    public void enter(CombatModeContext context) {
        super.enter(context);
        mobCrystalId = null;
        mobCrystalTicks = 0;
    }

    @Override
    protected void execute(CombatModeContext context, LivingEntity target) {
        context.motion().aimAt(target);
        context.crystal().setEnabled(context.options().isExplosions());
        if (target instanceof Player) { Player playerTarget = (Player) target;
            context.legacyCombat(playerTarget);
            return;
        }
        if (tickMobCrystal(context)) {
            context.motion().retreat(target, 5.0D);
            return;
        }
        double distance = context.motion().distanceTo(target);
        if (context.options().isExplosions()
                && distance <= 6.0D
                && specialActionReady()
                && context.inventory().consumeItem(BotInventoryController.CRYSTAL_SLOT)) {
            Location location = target.getLocation().add(0.0D, 0.5D, 0.0D);
            EnderCrystal crystal = context.entities().track(location.getWorld().spawn(location, EnderCrystal.class));
            crystal.setShowingBottom(false);
            mobCrystalId = crystal.getUniqueId();
            mobCrystalTicks = 5;
            delaySpecialAction(context);
            return;
        }
        meleeOrMove(context, target, BotInventoryController.SWORD_SLOT);
    }

    private boolean tickMobCrystal(CombatModeContext context) {
        if (mobCrystalId == null) {
            return false;
        }
        Entity entity = EntityLookupAccess.get(mobCrystalId);
        if (!(entity instanceof EnderCrystal)) {
            mobCrystalId = null;
            return false;
        }
        EnderCrystal crystal = (EnderCrystal) entity;
        if (!crystal.isValid()) {
            mobCrystalId = null;
            return false;
        }
        if (--mobCrystalTicks > 0) {
            return true;
        }
        Location location = crystal.getLocation();
        UUID crystalId = mobCrystalId;
        mobCrystalId = null;
        context.entities().remove(crystalId);
        NMSBridgeManager.get()
                .explode(location, context.bukkitBot(), 6.0F, context.options().canExplosionDamageBlocks());
        return true;
    }
}
