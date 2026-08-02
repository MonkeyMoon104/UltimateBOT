package com.monkey.ultimatebot.combat.mode;

import com.monkey.ultimatebot.bot.ai.controllers.inventory.BotInventoryController;
import com.monkey.ultimatebot.common.model.CombatMode;
import java.util.UUID;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import org.bukkit.Location;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.jspecify.annotations.Nullable;

final class CrystalPvPStrategy extends AbstractCombatModeStrategy {
    private @Nullable UUID mobCrystalId;
    private int mobCrystalTicks;

    CrystalPvPStrategy() {
        super(
                CombatMode.CRYSTAL,
                ModeKit.builder()
                        .slot(BotInventoryController.SWORD_SLOT, Items.NETHERITE_SWORD)
                        .slot(BotInventoryController.ENDERPEARL_SLOT, Items.ENDER_PEARL, 16)
                        .slot(BotInventoryController.OBSIDIAN_SLOT, Items.OBSIDIAN, 64)
                        .slot(BotInventoryController.CRYSTAL_SLOT, Items.END_CRYSTAL, 64)
                        .slot(BotInventoryController.ANCHOR_SLOT, Items.RESPAWN_ANCHOR, 64)
                        .slot(BotInventoryController.GLOW_SLOT, Items.GLOWSTONE, 64)
                        .slot(BotInventoryController.GOLDEN_APPLE_SLOT, Items.ENCHANTED_GOLDEN_APPLE, 64)
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
        context.aimAt(target);
        context.crystal().setEnabled(context.options().isExplosions());
        if (target instanceof Player playerTarget) {
            context.legacyCombat(playerTarget);
            return;
        }
        if (tickMobCrystal(context)) {
            context.retreat(target, 5.0D);
            return;
        }
        double distance = context.distanceTo(target);
        if (context.options().isExplosions() && distance <= 6.0D && specialActionReady()) {
            Location location = target.getBukkitEntity().getLocation().add(0.0D, 0.5D, 0.0D);
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
        Entity entity = org.bukkit.Bukkit.getEntity(mobCrystalId);
        if (!(entity instanceof EnderCrystal crystal) || !crystal.isValid()) {
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
        location.getWorld()
                .createExplosion(
                        location, 6.0F, false, context.options().isExplosionBlockDamage(), context.bukkitBot());
        return true;
    }
}
