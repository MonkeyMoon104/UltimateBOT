package com.monkey.ultimatebot.access.explosive;

import com.monkey.ultimatebot.nms.NMSBridgeManager;
import java.util.Objects;
import org.bukkit.entity.minecart.ExplosiveMinecart;

final class LegacyExplosiveMinecartOps implements ExplosiveMinecartOps {
    @Override
    public void setFuseTicks(ExplosiveMinecart cart, int ticks) {
        Objects.requireNonNull(cart, "cart");
        if (invokeConcreteSetFuseTicks(cart, ticks)) {
            return;
        }
        NMSBridgeManager.get().setExplosiveMinecartFuseTicks(cart, ticks);
    }

    private static boolean invokeConcreteSetFuseTicks(ExplosiveMinecart cart, int ticks) {
        try {
            java.lang.reflect.Method method = cart.getClass().getMethod("setFuseTicks", int.class);
            method.invoke(cart, ticks);
            return true;
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return false;
        }
    }
}
