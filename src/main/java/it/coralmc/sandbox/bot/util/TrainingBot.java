package it.coralmc.sandbox.bot.util;

import com.mojang.authlib.GameProfile;
import it.coralmc.sandbox.bot.ai.BotAI;
import it.coralmc.sandbox.utils.ChatColorUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.bukkit.event.entity.EntityDamageEvent;

public class TrainingBot extends Player {

    private org.bukkit.entity.Player targetPlayer;
    private boolean follow;
    private final BotAI botAI;
    public TrainingBot(Level level, BlockPos pos, float yRot, GameProfile gameProfile, org.bukkit.entity.Player targetPlayer, boolean follow) {
        super(level, pos, yRot, gameProfile);
        this.targetPlayer = targetPlayer;
        this.follow = follow;
        this.botAI = new BotAI(this);
    }

    @Override
    public void tick() {
        super.tick();

        if (follow) {
            botAI.tick(targetPlayer);
        }
    }

    @Override
    public void die(DamageSource cause) {
        super.die(cause);

        if (targetPlayer != null && targetPlayer.isOnline()) {
            String msg = it.coralmc.sandbox.SandboxBot.getInstance().getConfig()
                    .getString("messages.dead-bot-msg", "You have killed the bot!");
            targetPlayer.sendMessage(ChatColorUtils.translate(msg));
        }

        this.discard();
    }

    @Override
    public boolean isSpectator() {
        return false;
    }

    @Override
    public boolean isCreative() {
        return false;
    }

    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
        super.setItemSlot(slot, stack);
    }

    @Override
    protected boolean actuallyHurt(ServerLevel level, DamageSource source, float amount, EntityDamageEvent event) {
        boolean result = super.actuallyHurt(level, source, amount, event);

        if (result) {
            this.botAI.setKnockbackCooldown(10);
        }

        return result;
    }

}
