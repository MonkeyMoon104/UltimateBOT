package com.monkey.ultimatebot.combat.mode;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;

final class CobwebCombatAwareness {
    private CobwebCombatAwareness() {}

    static Containment inspect(LivingEntity entity) {
        BlockPos feet = BlockPos.containing(entity.getX(), entity.getY() + 0.05D, entity.getZ());
        BlockPos head = BlockPos.containing(entity.getX(), entity.getY() + entity.getBbHeight() * 0.75D, entity.getZ());
        boolean feetWeb = isCobweb(entity, feet);
        boolean headWeb = isCobweb(entity, head);
        boolean inside = feetWeb || headWeb;
        return new Containment(inside, inside && nearlyExiting(entity.position(), entity.getDeltaMovement()));
    }

    static List<Location> occupiedWebs(LivingEntity entity) {
        Location base = entity.getBukkitEntity().getLocation().getBlock().getLocation();
        Location head = base.clone().add(0.0D, 1.0D, 0.0D);
        if (base.getBlock().getType() == org.bukkit.Material.COBWEB
                && head.getBlock().getType() == org.bukkit.Material.COBWEB) {
            return List.of(base, head);
        }
        if (base.getBlock().getType() == org.bukkit.Material.COBWEB) {
            return List.of(base);
        }
        return head.getBlock().getType() == org.bukkit.Material.COBWEB ? List.of(head) : List.of();
    }

    static boolean nearlyExiting(Vec3 position, Vec3 velocity) {
        double horizontalSpeed = velocity.horizontalDistance();
        if (horizontalSpeed < 0.003D) {
            return false;
        }
        double xFraction = position.x - Math.floor(position.x);
        double zFraction = position.z - Math.floor(position.z);
        double xExit = velocity.x > 0.0D ? 1.0D - xFraction : xFraction;
        double zExit = velocity.z > 0.0D ? 1.0D - zFraction : zFraction;
        double nearestExit =
                Math.min(Math.abs(velocity.x) > 0.001D ? xExit : 1.0D, Math.abs(velocity.z) > 0.001D ? zExit : 1.0D);
        return nearestExit <= 0.22D;
    }

    private static boolean isCobweb(LivingEntity entity, BlockPos position) {
        return entity.level().getBlockState(position).is(Blocks.COBWEB);
    }

    record Containment(boolean inside, boolean nearlyExiting) {}
}
