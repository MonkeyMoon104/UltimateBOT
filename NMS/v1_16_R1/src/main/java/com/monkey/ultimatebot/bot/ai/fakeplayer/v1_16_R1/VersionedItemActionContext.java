package com.monkey.ultimatebot.bot.ai.fakeplayer.v1_16_R1;

import net.minecraft.server.v1_16_R1.EntityHuman;
import net.minecraft.server.v1_16_R1.EnumHand;
import net.minecraft.server.v1_16_R1.ItemActionContext;
import net.minecraft.server.v1_16_R1.ItemStack;
import net.minecraft.server.v1_16_R1.MovingObjectPositionBlock;
import net.minecraft.server.v1_16_R1.World;
import org.jspecify.annotations.NullUnmarked;

/**
 * 1.16.1 {@link ItemActionContext} ctor is protected. Subclass so the bridge can place rails /
 * crystals without a public constructor (1.16.3+ made it public).
 */
@NullUnmarked
public final class VersionedItemActionContext extends ItemActionContext {

    public VersionedItemActionContext(
            World world,
            EntityHuman player,
            EnumHand hand,
            ItemStack stack,
            MovingObjectPositionBlock hit) {
        super(world, player, hand, stack, hit);
    }
}
