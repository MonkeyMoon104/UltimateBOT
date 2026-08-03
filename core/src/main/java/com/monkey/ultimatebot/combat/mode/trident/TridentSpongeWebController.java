package com.monkey.ultimatebot.combat.mode.trident;

import com.monkey.ultimatebot.combat.mode.runtime.CombatModeContext;
import com.monkey.ultimatebot.combat.mode.shared.CobwebCombatAwareness;
import com.monkey.ultimatebot.combat.mode.shared.WebTrapPlanner;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import net.minecraft.world.entity.LivingEntity;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.util.Vector;

final class TridentSpongeWebController {
    private final Set<WebTrapPlanner.Position> placed = new LinkedHashSet<>();
    private boolean webReady;

    boolean tick(CombatModeContext context, LivingEntity target, int phaseTicks) {
        if (phaseTicks == 1) {
            webReady = placeWeb(context, target);
        } else if (webReady && phaseTicks >= 3 && phaseTicks <= 9 && phaseTicks % 2 == 1) {
            placeSponge(context, target);
        }
        return (!webReady && phaseTicks >= 3) || phaseTicks >= 11;
    }

    void reset() {
        placed.clear();
        webReady = false;
    }

    private boolean placeWeb(CombatModeContext context, LivingEntity target) {
        context.inventory().switchToSlot(TridentLoadout.WEB_SLOT);
        if (CobwebCombatAwareness.inspect(target).inside()) {
            return true;
        }
        org.bukkit.entity.Entity bukkitTarget = target.getBukkitEntity();
        Location targetLocation = Objects.requireNonNull(bukkitTarget.getLocation(), "target location");
        for (Location candidate : WebTrapPlanner.plan(
                targetLocation,
                bukkitTarget.getVelocity(),
                bukkitTarget.isOnGround(),
                targetLocation.getDirection(),
                context.random())) {
            if (!context.canPlaceCombatBlock(candidate, Material.COBWEB)) {
                continue;
            }
            if (context.placeCombatBlock(candidate, Material.COBWEB, TridentLoadout.WEB_SLOT)) {
                placed.add(WebTrapPlanner.Position.from(candidate));
                context.actions().swingMainHand();
                return true;
            }
        }
        return false;
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
