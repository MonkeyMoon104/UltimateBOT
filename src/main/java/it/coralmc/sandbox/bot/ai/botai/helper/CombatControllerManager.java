package it.coralmc.sandbox.bot.ai.botai.helper;

import it.coralmc.sandbox.bot.ai.controllers.cpvp.BotCPVPController;
import it.coralmc.sandbox.bot.ai.controllers.rapvp.BotRAPVPController;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;

public class CombatControllerManager {

    public enum ControllerPriority {
        RAPVP_PRIMARY_CPVP_SECONDARY,
        CPVP_PRIMARY_RAPVP_SECONDARY
    }

    private final Player bot;
    private final BotRAPVPController rapvpController;
    private final BotCPVPController cpvpController;

    private ControllerPriority currentPriority = ControllerPriority.RAPVP_PRIMARY_CPVP_SECONDARY;
    private long lastPriorityCheck = 0;
    private static final long PRIORITY_CHECK_INTERVAL = 1000;

    public CombatControllerManager(Player bot, BotRAPVPController rapvpController, BotCPVPController cpvpController) {
        this.bot = bot;
        this.rapvpController = rapvpController;
        this.cpvpController = cpvpController;
    }

    public void tick(Player target) {
        updateControllerPriority();

        switch (currentPriority) {
            case RAPVP_PRIMARY_CPVP_SECONDARY -> {
                if (rapvpController.isActive()) {
                    rapvpController.tick();
                } else {
                    cpvpController.tick(target);
                }
            }
            case CPVP_PRIMARY_RAPVP_SECONDARY -> {
                if (cpvpController.isDoingCrystalAction()) {
                    cpvpController.tick(target);
                } else {
                    if (rapvpController.isActive()) {
                        rapvpController.tick();
                    } else {
                        cpvpController.tick(target);
                    }
                }
            }
        }
    }

    private void updateControllerPriority() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastPriorityCheck < PRIORITY_CHECK_INTERVAL) {
            return;
        }

        ControllerPriority newPriority = isNearBedrock() ?
                ControllerPriority.CPVP_PRIMARY_RAPVP_SECONDARY :
                ControllerPriority.RAPVP_PRIMARY_CPVP_SECONDARY;

        if (newPriority != currentPriority) {
            System.out.println("[DEBUG] Controller priority changed from " + currentPriority + " to " + newPriority);
            currentPriority = newPriority;

            if (newPriority == ControllerPriority.CPVP_PRIMARY_RAPVP_SECONDARY && rapvpController.isActive()) {
                rapvpController.disable();
            }
        }

        lastPriorityCheck = currentTime;
    }

    private boolean isNearBedrock() {
        BlockPos botPos = bot.blockPosition();
        int y = botPos.getY();

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                for (int yOffset = -1; yOffset <= 1; yOffset++) {
                    BlockPos checkPos = new BlockPos(botPos.getX() + x, y + yOffset, botPos.getZ() + z);
                    if (bot.level().getBlockState(checkPos).getBlock() == Blocks.BEDROCK) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public ControllerPriority getCurrentPriority() {
        return currentPriority;
    }

    public boolean isPrimaryRAPVP() {
        return currentPriority == ControllerPriority.RAPVP_PRIMARY_CPVP_SECONDARY;
    }

    public boolean isPrimaryCPVP() {
        return currentPriority == ControllerPriority.CPVP_PRIMARY_RAPVP_SECONDARY;
    }
}