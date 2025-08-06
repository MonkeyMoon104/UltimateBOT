package it.coralmc.sandbox.bot.ai;

import it.coralmc.sandbox.SandboxBot;
import it.coralmc.sandbox.bot.util.TrainingBot;
import it.coralmc.sandbox.utils.chatcolor.ChatColorUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.bukkit.craftbukkit.entity.CraftPlayer;

public class BotAI {

    private final Player bot;
    private final Level level;
    private int knockbackCooldown = 0;
    private double[] diversionDirection = null;
    private int diversionTicks = 0;
    private boolean warnedOutOfTotems = false;


    public BotAI(Player bot) {
        this.bot = bot;
        this.level = bot.level();
    }

    public void setKnockbackCooldown(int ticks) {
        this.knockbackCooldown = ticks;
    }

    public void tick(org.bukkit.entity.Player targetBukkitPlayer) {
        if (targetBukkitPlayer == null || targetBukkitPlayer.isDead()) return;

        if (knockbackCooldown > 0) {
            knockbackCooldown--;
            return;
        }

        Player target = ((CraftPlayer) targetBukkitPlayer).getHandle();

        double targetX = target.getX();
        double targetY = target.getY();
        double targetZ = target.getZ();

        double botX = bot.getX();
        double botY = bot.getY();
        double botZ = bot.getZ();

        double dx = targetX - botX;
        double dy = targetY - botY;
        double dz = targetZ - botZ;

        double dist = bot.distanceTo(target);
        if (dist <= 1.5) return;

        float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        bot.setYRot(yaw);
        bot.yHeadRot = yaw;
        bot.yBodyRot = yaw;

        double length = Math.sqrt(dx * dx + dz * dz);
        if (length == 0) return;

        dx = dx / length;
        dz = dz / length;

        double speed = 0.25;
        double moveX = dx * speed;
        double moveZ = dz * speed;

        BlockPos front = BlockPos.containing(botX + dx, botY, botZ + dz);
        BlockPos above = front.above();
        BlockPos above2 = above.above();

        boolean frontBlocked = !level.getBlockState(front).getCollisionShape(level, front).isEmpty();
        boolean aboveClear = level.getBlockState(above).getCollisionShape(level, above).isEmpty();
        boolean above2Clear = level.getBlockState(above2).getCollisionShape(level, above2).isEmpty();

        boolean canStepUp = frontBlocked && aboveClear;
        boolean tooHigh = frontBlocked && !aboveClear && !above2Clear;

        if (tooHigh) {
            if (diversionTicks <= 0 || diversionDirection == null) {
                diversionDirection = findAlternativeDirection(dx, dz, 6);
                diversionTicks = 10;
            }

            if (diversionDirection != null) {
                diversionTicks--;
                double altDx = diversionDirection[0];
                double altDz = diversionDirection[1];
                bot.setDeltaMovement(altDx * speed, bot.getDeltaMovement().y, altDz * speed);
            } else {
                bot.setDeltaMovement(0, bot.getDeltaMovement().y, 0);
            }
            diversionDirection = null;
            diversionTicks = 0;
            return;
        }


        if (canStepUp && bot.onGround()) {
            bot.setDeltaMovement(bot.getDeltaMovement().x, 0.42, bot.getDeltaMovement().z);
        }

        bot.setDeltaMovement(moveX, bot.getDeltaMovement().y, moveZ);
    }

    private double[] findAlternativeDirection(double dx, double dz, int maxTries) {
        double angle = Math.atan2(dz, dx);

        for (int i = 1; i <= maxTries; i++) {
            double offset = Math.toRadians(15 * i);

            for (int sign : new int[]{1, -1}) {
                double newAngle = angle + offset * sign;

                double newDx = Math.cos(newAngle);
                double newDz = Math.sin(newAngle);

                BlockPos checkPos = BlockPos.containing(bot.getX() + newDx, bot.getY(), bot.getZ() + newDz);
                BlockPos checkAbove = checkPos.above();
                BlockPos checkAbove2 = checkAbove.above();

                boolean frontClear = level.getBlockState(checkPos).getCollisionShape(level, checkPos).isEmpty();
                boolean aboveClear = level.getBlockState(checkAbove).getCollisionShape(level, checkAbove).isEmpty();
                boolean above2Clear = level.getBlockState(checkAbove2).getCollisionShape(level, checkAbove2).isEmpty();

                if (frontClear && aboveClear && above2Clear) {
                    return new double[]{newDx, newDz};
                }
            }
        }

        return null;
    }

    public void manageTotem() {
        ItemStack offhand = bot.getItemBySlot(EquipmentSlot.OFFHAND);

        if (offhand == null || offhand.isEmpty() || !offhand.is(Items.TOTEM_OF_UNDYING)) {
            for (int i = 0; i < bot.getInventory().items.size(); i++) {
                ItemStack stack = bot.getInventory().items.get(i);
                if (stack != null && !stack.isEmpty() && stack.is(Items.TOTEM_OF_UNDYING)) {
                    bot.getInventory().items.set(i, ItemStack.EMPTY);
                    bot.setItemSlot(EquipmentSlot.OFFHAND, stack);
                    warnedOutOfTotems = false;
                    return;
                }
            }

            if (!warnedOutOfTotems && bot instanceof TrainingBot trainingBot) {
                var player = trainingBot.getTargetPlayer();
                if (player != null && player.isOnline()) {
                    String msg = SandboxBot.getInstance().getConfig()
                            .getString("bot.totem-finish", "[%botname%] Running out of totems");

                    String botName = SandboxBot.getInstance().getConfig().getString("bot.name", "CrystalBot");
                    msg = msg.replace("%botname%", botName);

                    player.sendMessage(ChatColorUtils.translate(msg));
                }
                warnedOutOfTotems = true;
            }
        }
    }

}
