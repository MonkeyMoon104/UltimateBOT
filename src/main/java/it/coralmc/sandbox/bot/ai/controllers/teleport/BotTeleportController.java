package it.coralmc.sandbox.bot.ai.controllers.teleport;

import it.coralmc.sandbox.bot.ai.controllers.teleport.helper.*;
import it.coralmc.sandbox.bot.ai.controllers.teleport.helper.inter.ITeleportStrategy;
import it.coralmc.sandbox.bot.ai.controllers.teleport.helper.inter.ITeleportValidator;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.bukkit.craftbukkit.entity.CraftPlayer;

public class BotTeleportController {

    private final Player bot;
    private final ITeleportValidator validator;

    private boolean teleporting = false;
    private org.bukkit.entity.Player currentTarget;

    private int suffocationCount = 0;
    private long lastSuffocationTime = 0;
    private static final long SUFFOCATION_RESET_TIME = 5000;
    private static final int MAX_SUFFOCATION_BEFORE_TELEPORT = 2;

    public BotTeleportController(Player bot) {
        this.bot = bot;
        this.validator = new BasicTeleportValidator();
    }

    public boolean teleportTo(Vec3 pos) {
        if (teleporting || pos == null) return false;
        teleporting = true;
        bot.teleportTo(pos.x, pos.y, pos.z);
        teleporting = false;
        return true;
    }

    public boolean teleportBeside(org.bukkit.entity.Player target) {
        if (target == null) return false;
        Player nmsTarget = ((CraftPlayer) target).getHandle();

        ITeleportStrategy strategy = new BesideTargetStrategy(1.5);
        Vec3 pos = strategy.findTeleportPosition(bot, nmsTarget);
        return teleportTo(pos);
    }

    public boolean teleportSafeNear(org.bukkit.entity.Player target) {
        if (target == null) return false;
        Player nmsTarget = ((CraftPlayer) target).getHandle();

        ITeleportStrategy strategy = new SafeTeleportStrategy(validator);
        Vec3 pos = strategy.findTeleportPosition(bot, nmsTarget);
        if (pos == null) return false;
        return teleportTo(pos);
    }

    public boolean teleportNearTarget() {
        if (currentTarget == null || !currentTarget.isOnline()) return false;
        return teleportSafeNear(currentTarget);
    }

    public void handleSuffocationDamage() {
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastSuffocationTime > SUFFOCATION_RESET_TIME) {
            suffocationCount = 0;
        }

        suffocationCount++;
        lastSuffocationTime = currentTime;

        if (suffocationCount >= MAX_SUFFOCATION_BEFORE_TELEPORT) {
            teleportNearTarget();
            suffocationCount = 0;
        }
    }

    public int getSuffocationCount() {
        return suffocationCount;
    }

    public long getLastSuffocationTime() {
        return lastSuffocationTime;
    }

    public void resetSuffocationCounter() {
        suffocationCount = 0;
        lastSuffocationTime = 0;
    }

    public boolean isSuffocating() {
        return validator.isSuffocationDamage(bot);
    }

    public boolean isTeleporting() {
        return teleporting;
    }

    public void setTarget(org.bukkit.entity.Player target) {
        this.currentTarget = target;
    }

    public org.bukkit.entity.Player getCurrentTarget() {
        return currentTarget;
    }
}
