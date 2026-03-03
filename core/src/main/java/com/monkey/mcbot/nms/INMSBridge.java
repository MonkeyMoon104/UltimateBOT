package com.monkey.mcbot.nms;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.bukkit.event.entity.EntityDamageEvent;

public interface INMSBridge {

    void hurtEntity(Player target, ServerLevel level, DamageSource source, float amount);

    boolean actuallyHurt(Player bot, ServerLevel level, DamageSource source, float amount, EntityDamageEvent event);

    void explode(Level level, Player cause, double x, double y, double z, float power);

    void playSound(net.minecraft.world.level.Level level,
                   net.minecraft.core.BlockPos pos,
                   net.minecraft.sounds.SoundEvent sound,
                   net.minecraft.sounds.SoundSource source,
                   float volume, float pitch);
}