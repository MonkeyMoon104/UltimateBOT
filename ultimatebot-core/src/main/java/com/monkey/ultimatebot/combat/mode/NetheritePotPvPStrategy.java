package com.monkey.ultimatebot.combat.mode;

import com.monkey.ultimatebot.bot.ai.controllers.inventory.BotInventoryController;
import com.monkey.ultimatebot.common.model.CombatMode;
import java.util.Objects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

final class NetheritePotPvPStrategy extends AbstractCombatModeStrategy {
    NetheritePotPvPStrategy() {
        super(
                CombatMode.NETHERITE_POT,
                ModeKit.builder()
                        .slot(BotInventoryController.SWORD_SLOT, Items.NETHERITE_SWORD)
                        .slot(BotInventoryController.ENDERPEARL_SLOT, Items.SPLASH_POTION, 64)
                        .slot(BotInventoryController.GOLDEN_APPLE_SLOT, Items.ENDER_PEARL, 16)
                        .build());
    }

    @Override
    protected void execute(CombatModeContext context, LivingEntity target) {
        context.aimAt(target);
        if (context.healthRatio() <= context.tuning().healingHealthRatio()
                && specialActionReady()
                && context.inventory().consumeItem(BotInventoryController.ENDERPEARL_SLOT)) {
            org.bukkit.entity.Player player = context.bukkitBot();
            Location location = Objects.requireNonNull(player.getLocation(), "bot location");
            context.inventory().switchToSlot(BotInventoryController.ENDERPEARL_SLOT);
            player.addPotionEffect(new PotionEffect(PotionEffectType.INSTANT_HEALTH, 1, 1, false, false, false));
            player.getWorld().spawnParticle(Particle.INSTANT_EFFECT, location, 18, 0.4D, 0.5D, 0.4D, 0.1D);
            player.getWorld().playSound(location, Sound.ENTITY_SPLASH_POTION_BREAK, 0.8F, 1.0F);
            delaySpecialAction(context);
            context.retreat(target, 4.5D);
            return;
        }
        meleeOrMove(context, target, BotInventoryController.SWORD_SLOT);
    }
}
