package com.monkey.ultimatebot.combat.mode;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import net.minecraft.world.entity.LivingEntity;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.util.Vector;

final class TridentSpongeWebController {
    private final Set<WebTrapPlanner.Position> placed = new LinkedHashSet<>();

    boolean tick(CombatModeContext context, LivingEntity target, int phaseTicks) {
        if (phaseTicks == 1) {
            placeWeb(context, target);
        } else if (phaseTicks >= 3 && phaseTicks <= 9 && phaseTicks % 2 == 1) {
            placeSponge(context, target);
        }
        return phaseTicks >= 11;
    }

    void reset() {
        placed.clear();
    }

    private void placeWeb(CombatModeContext context, LivingEntity target) {
        context.inventory().switchToSlot(TridentLoadout.WEB_SLOT);
        Location targetBlock = targetBlock(target);
        if (targetBlock.getBlock().getType() == Material.COBWEB
                || context.placeCombatBlock(targetBlock, Material.COBWEB, TridentLoadout.WEB_SLOT)) {
            placed.add(WebTrapPlanner.Position.from(targetBlock));
            context.actions().swingMainHand();
        }
    }

    private void placeSponge(CombatModeContext context, LivingEntity target) {
        context.inventory().switchToSlot(TridentLoadout.SPONGE_SLOT);
        Location base = targetBlock(target);
        Vector direction = target.getBukkitEntity().getVelocity().setY(0.0D);
        if (direction.lengthSquared() < 0.0025D) {
            direction = base.getDirection().setY(0.0D);
        }
        if (direction.lengthSquared() < 0.0025D) {
            direction = new Vector(1.0D, 0.0D, 0.0D);
        }
        direction.normalize();
        Vector side = new Vector(-direction.getZ(), 0.0D, direction.getX());
        for (Location candidate : java.util.List.of(
                base.clone().add(side),
                base.clone().subtract(side),
                base.clone().add(direction),
                base.clone().subtract(direction))) {
            Location blockLocation = candidate.getBlock().getLocation();
            WebTrapPlanner.Position position = WebTrapPlanner.Position.from(blockLocation);
            if (placed.contains(position) || !context.canPlaceCombatBlock(blockLocation, Material.SPONGE)) {
                continue;
            }
            if (context.placeCombatBlock(blockLocation, Material.SPONGE, TridentLoadout.SPONGE_SLOT)) {
                placed.add(position);
                context.actions().swingMainHand();
                return;
            }
        }
    }

    private static Location targetBlock(LivingEntity target) {
        return Objects.requireNonNull(target.getBukkitEntity().getLocation(), "target location")
                .getBlock()
                .getLocation();
    }
}
