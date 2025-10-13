package it.coralmc.sandbox.bot.ai;

import com.mojang.authlib.GameProfile;
import it.coralmc.sandbox.SandboxTraining;
import it.coralmc.sandbox.bot.BotOptions;
import it.coralmc.sandbox.bot.ai.fakeplayer.BotCraftPlayer;
import it.coralmc.sandbox.bot.ai.controllers.brain.BotBrainController;
import it.coralmc.sandbox.bot.ai.services.BotDeathService;
import it.coralmc.sandbox.bot.ai.services.BotEquipmentService;
import it.coralmc.sandbox.bot.ai.services.TotemTrackerService;
import it.coralmc.sandbox.utils.armor.PlayerOptions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.event.entity.EntityDamageEvent;

public class TrainingBot extends Player {

    private final SandboxTraining plugin;
    private final PlayerOptions playerOptions;
    private final BotCraftPlayer craftEntity;
    private final BotBrainController brainController;
    private final TotemTrackerService totemTracker;
    private final BotDeathService deathHandler;
    private final BotEquipmentService equipmentHandler;

    public TrainingBot(Level level,
                       BlockPos pos,
                       float yRot,
                       GameProfile gameProfile,
                       org.bukkit.entity.Player targetPlayer,
                       boolean follow,
                       SandboxTraining plugin,
                       String deadBotMessage,
                       BotOptions botOptions) {

        super(level, pos, yRot, gameProfile);

        this.setNoGravity(false);
        this.setOnGround(false);

        this.plugin = plugin;
        this.playerOptions = plugin.getPlayerOptions();
        this.craftEntity = new BotCraftPlayer(this);

        this.brainController = new BotBrainController(this, plugin, targetPlayer, follow, botOptions);
        this.totemTracker = new TotemTrackerService(this);
        this.deathHandler = new BotDeathService(this, plugin, playerOptions, deadBotMessage);
        this.equipmentHandler = new BotEquipmentService(this);
        getBotAI().getTeleportController().setTarget(targetPlayer);
    }

    @Override
    public void tick() {
        try {
            super.tick();
        } catch (ClassCastException ignored) {

        }
        craftEntity.setHandle(this);

        if (isCombat()) {
            if (getBotAI().getHealController().isHealing()) {
                getBotAI().getHealController().updateHealAction();
                totemTracker.onTick();
                return;
            }

            if (getBotAI().getHealController().shouldHeal()) {
                getBotAI().getHealController().handleDamageReceived();
                totemTracker.onTick();

                if (getBotAI().getHealController().isHealing()) {
                    return;
                }
            }
        }

        brainController.onTick();
        totemTracker.onTick();
    }

    @Override
    public void die(DamageSource cause) {
        super.die(cause);
        deathHandler.handleDeath(cause);
    }

    @Override
    protected boolean actuallyHurt(ServerLevel level, DamageSource source, float amount, EntityDamageEvent event) {
        boolean result = equipmentHandler.handleDamage(level, source, amount, event);

        if (isCombat()) {

            getBotAI().getMovementController().onDamageReceived();

            net.minecraft.world.entity.Entity attacker = source.getEntity();
            if (attacker instanceof Player nmsPlayer && nmsPlayer.getUUID().equals(getTargetPlayer().getUniqueId())) {
                if (!source.is(DamageTypes.IN_FIRE) && !source.is(DamageTypes.ON_FIRE) && !source.is(DamageTypes.LAVA)) {
                    if (source.isCritical()) {
                        getBotAI().getEnderpearlController().onDamageReceived();
                    }
                }
            }

            if (getBotAI().getTeleportController().isSuffocating()) {
                getBotAI().getTeleportController().handleSuffocationDamage();
            }

            if (!getBotAI().getHealController().isHealing()) {
                getBotAI().getHealController().handleDamageReceived();
            } else {
                getBotAI().getMovementController().setUnderFire(true);
                getBotAI().getMovementController().emergencyEvade();
            }

            if (getBotAI().getMovementController().isStuckInPlace()) {
                getBotAI().getMovementController().forceUnstick(((org.bukkit.craftbukkit.entity.CraftPlayer) getTargetPlayer()).getHandle());
            }
        }

        return result;
    }


    @Override public boolean isSpectator() { return false; }
    @Override public boolean isCreative() { return false; }
    @Override public boolean canPickUpLoot() { return false; }
    @Override public boolean canBeCollidedWith() { return false; }
    @Override public boolean isPushable() { return false; }

    @Override
    public void playerTouch(Player player) {
        if (player != this) {
            super.playerTouch(player);
        }
    }

    @Override
    public CraftHumanEntity getBukkitEntity() {
        craftEntity.setHandle(this);
        return craftEntity;
    }

    @Override
    public void aiStep() {
        try {
            super.aiStep();
        } catch (ClassCastException ignored) {}
    }

    @Override
    public void completeUsingItem() {
        try {
            super.completeUsingItem();
        } catch (ClassCastException e) {
            if (this.getUseItem().getItem() == net.minecraft.world.item.Items.GOLDEN_APPLE) {
                getBotAI().getHealController().applyEffect();
            }
            this.releaseUsingItem();
        }
    }

    public BotBrainController getBrainController() { return brainController; }
    public TotemTrackerService getTotemTracker() { return totemTracker; }
    public org.bukkit.entity.Player getTargetPlayer() { return brainController.getTargetPlayer(); }
    public void setTotemCount(int count) {
        this.totemTracker.setTotemCount(count);
    }
    public int getTotemCount() {
        return this.totemTracker.getTotemCount();
    }
    public void setFollow(boolean follow) {
        this.brainController.setFollow(follow);
    }
    public boolean isFollow() {
        return this.brainController.isFollow();
    }

    public void setCombat(boolean combat) {
        this.brainController.setCombat(combat);
    }
    public boolean isCombat() {
        return this.brainController.isCombat();
    }
    public BotAI getBotAI() {
        return this.brainController.getBotAI();
    }
    public boolean callSuperActuallyHurt(ServerLevel level, DamageSource source, float amount, EntityDamageEvent event) {
        return super.actuallyHurt(level, source, amount, event);
    }
    public SandboxTraining getPlugin() {
        return this.plugin;
    }
}
