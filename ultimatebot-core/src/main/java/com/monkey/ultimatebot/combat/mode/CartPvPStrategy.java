package com.monkey.ultimatebot.combat.mode;

import com.monkey.ultimatebot.bot.ai.controllers.inventory.BotInventoryController;
import com.monkey.ultimatebot.common.model.CombatMode;
import java.util.UUID;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.minecart.ExplosiveMinecart;
import org.jspecify.annotations.Nullable;

final class CartPvPStrategy extends AbstractCombatModeStrategy {
    private static final int DETONATION_DELAY_TICKS = 12;

    private @Nullable UUID activeCartId;
    private int detonationTicks;

    CartPvPStrategy() {
        super(
                CombatMode.CART,
                ModeKit.builder()
                        .slot(BotInventoryController.SWORD_SLOT, Items.NETHERITE_SWORD)
                        .slot(BotInventoryController.OBSIDIAN_SLOT, Items.RAIL, 64)
                        .slot(BotInventoryController.CRYSTAL_SLOT, Items.TNT_MINECART, 64)
                        .build());
    }

    @Override
    public void enter(CombatModeContext context) {
        super.enter(context);
        activeCartId = null;
        detonationTicks = 0;
    }

    @Override
    protected void execute(CombatModeContext context, LivingEntity target) {
        context.aimAt(target);
        if (tickCart(context)) {
            context.retreat(target, 5.0D);
            return;
        }
        double distance = context.distanceTo(target);
        if (distance <= 6.0D
                && distance >= 2.0D
                && specialActionReady()
                && context.inventory().consumeItem(BotInventoryController.CRYSTAL_SLOT)) {
            Location location = target.getBukkitEntity().getLocation();
            ExplosiveMinecart cart =
                    context.entities().track(location.getWorld().spawn(location, ExplosiveMinecart.class));
            cart.setFuseTicks(-1);
            cart.setVelocity(target.getBukkitEntity().getVelocity().multiply(0.65D));
            activeCartId = cart.getUniqueId();
            detonationTicks = DETONATION_DELAY_TICKS;
            delaySpecialAction(context);
            return;
        }
        meleeOrMove(context, target, BotInventoryController.SWORD_SLOT);
    }

    private boolean tickCart(CombatModeContext context) {
        if (activeCartId == null) {
            return false;
        }
        Entity entity = org.bukkit.Bukkit.getEntity(activeCartId);
        if (!(entity instanceof ExplosiveMinecart cart) || !cart.isValid()) {
            activeCartId = null;
            return false;
        }
        if (--detonationTicks > 0) {
            return true;
        }
        Location location = cart.getLocation();
        UUID cartId = activeCartId;
        activeCartId = null;
        context.entities().remove(cartId);
        location.getWorld()
                .createExplosion(
                        location, 4.0F, false, context.options().isExplosionBlockDamage(), context.bukkitBot());
        return true;
    }
}
