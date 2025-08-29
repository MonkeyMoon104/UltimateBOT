package it.coralmc.sandbox.bot.ai;

import com.mojang.authlib.GameProfile;
import it.coralmc.sandbox.SandboxTraining;
import it.coralmc.sandbox.bot.BotOptions;
import it.coralmc.sandbox.bot.ai.crazy.BotCraftPlayer;
import it.coralmc.sandbox.bot.ai.handlers.BotAIController;
import it.coralmc.sandbox.bot.ai.handlers.BotDeathHandler;
import it.coralmc.sandbox.bot.ai.handlers.BotEquipmentHandler;
import it.coralmc.sandbox.bot.ai.handlers.TotemTracker;
import it.coralmc.sandbox.utils.armor.PlayerOptions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.event.entity.EntityDamageEvent;

public class TrainingBot extends Player {

    private final SandboxTraining plugin;
    private final PlayerOptions playerOptions;
    private final BotCraftPlayer craftEntity;

    private final BotAIController aiController;
    private final TotemTracker totemTracker;
    private final BotDeathHandler deathHandler;
    private final BotEquipmentHandler equipmentHandler;

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

        this.aiController = new BotAIController(this, plugin, targetPlayer, follow, botOptions);
        this.totemTracker = new TotemTracker(this);
        this.deathHandler = new BotDeathHandler(this, plugin, playerOptions, deadBotMessage);
        this.equipmentHandler = new BotEquipmentHandler(this);
    }

    @Override
    public void tick() {
        super.tick();
        craftEntity.setHandle(this);

        aiController.onTick();
        totemTracker.onTick();
    }

    @Override
    public void die(DamageSource cause) {
        super.die(cause);
        deathHandler.handleDeath(cause);
    }

    @Override
    protected boolean actuallyHurt(ServerLevel level, DamageSource source, float amount, EntityDamageEvent event) {
        getBotAI().getEnderpearlController().onDamageReceived();
        return equipmentHandler.handleDamage(level, source, amount, event);
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

    public BotAIController getAiController() { return aiController; }
    public TotemTracker getTotemTracker() { return totemTracker; }
    public org.bukkit.entity.Player getTargetPlayer() { return aiController.getTargetPlayer(); }
    public void setTotemCount(int count) {
        this.totemTracker.setTotemCount(count);
    }
    public int getTotemCount() {
        return this.totemTracker.getTotemCount();
    }
    public void setFollow(boolean follow) {
        this.aiController.setFollow(follow);
    }
    public boolean isFollow() {
        return this.aiController.isFollow();
    }

    public void setCombat(boolean combat) {
        this.aiController.setCombat(combat);
    }
    public boolean isCombat() {
        return this.aiController.isCombat();
    }
    public it.coralmc.sandbox.bot.ai.BotAI getBotAI() {
        return this.aiController.getBotAI();
    }
    public boolean callSuperActuallyHurt(ServerLevel level, DamageSource source, float amount, EntityDamageEvent event) {
        return super.actuallyHurt(level, source, amount, event);
    }
    public SandboxTraining getPlugin() {
        return this.plugin;
    }
}
