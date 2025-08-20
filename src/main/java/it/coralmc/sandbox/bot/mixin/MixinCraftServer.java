package it.coralmc.sandbox.bot.mixin;

import it.coralmc.sandbox.SandboxTraining;
import it.coralmc.sandbox.bot.ai.crazy.FakeOfflinePlayer;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.CraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(CraftServer.class)
public class MixinCraftServer {

    @Inject(
            method = "getOfflinePlayer(Ljava/util/UUID;)Lorg/bukkit/OfflinePlayer;",
            at = @At("HEAD"),
            cancellable = true
    )
    public void onGetOfflinePlayer(UUID uuid, CallbackInfoReturnable<OfflinePlayer> cir) {
        FakeOfflinePlayer bot = SandboxTraining.getInstance().getBotRegistry().getFakeOfflinePlayer(uuid);
        if (bot != null) {
            cir.setReturnValue(bot);
            cir.cancel();
        }
    }
}
