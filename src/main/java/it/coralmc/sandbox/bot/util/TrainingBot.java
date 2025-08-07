package it.coralmc.sandbox.bot.util;

import com.mojang.authlib.GameProfile;
import it.coralmc.sandbox.SandboxTraining;
import it.coralmc.sandbox.bot.BotSpawner;
import it.coralmc.sandbox.bot.ai.BotAI;
import it.coralmc.sandbox.utils.armor.PlayerArmorManager;
import it.coralmc.sandbox.utils.chatcolor.ChatColorUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.event.entity.EntityDamageEvent;

public class TrainingBot extends Player {

	private final BotAI botAI;
	private org.bukkit.entity.Player targetPlayer;
	private boolean follow;
	private int totemCount = -1;
	private boolean hadTotemLastTick = false;

	public TrainingBot(Level level, BlockPos pos, float yRot, GameProfile gameProfile, org.bukkit.entity.Player targetPlayer, boolean follow) {
		super(level, pos, yRot, gameProfile);
		this.targetPlayer = targetPlayer;
		this.follow = follow;
		this.botAI = new BotAI(this);

	}

	@Override
	public void tick() {
		super.tick();

		ItemStack offhand = this.getItemBySlot(EquipmentSlot.OFFHAND);
		boolean hasTotemNow = offhand != null && !offhand.isEmpty() && offhand.is(net.minecraft.world.item.Items.TOTEM_OF_UNDYING);

		if (hadTotemLastTick && !hasTotemNow) {
			this.botAI.onTotemUsed();
		}

		hadTotemLastTick = hasTotemNow;

		botAI.manageTotem();

		if (follow) {
			botAI.tick(targetPlayer);
		}
	}

	@Override
	public void die(DamageSource cause) {
		this.getInventory().items.clear();

		super.die(cause);

		if (targetPlayer != null && targetPlayer.isOnline()) {
			String msg = SandboxTraining.getInstance().getConfig()
				.getString("messages.dead-bot-msg", "You have killed the bot!");
			targetPlayer.sendMessage(ChatColorUtils.translate(msg));
		}

		if (targetPlayer != null) {
			PlayerArmorManager.removePlayerSettings(targetPlayer.getUniqueId());
		}

		this.discard();
		BotSpawner.removeBot(this.getUUID());
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
		ItemStack totemBefore = this.getItemBySlot(EquipmentSlot.OFFHAND);
		boolean hadTotem = totemBefore != null && !totemBefore.isEmpty() && totemBefore.is(net.minecraft.world.item.Items.TOTEM_OF_UNDYING);

		boolean result = super.actuallyHurt(level, source, amount, event);

		if (result) {
			this.botAI.setKnockbackCooldown(20);

			if (hadTotem) {
				ItemStack totemAfter = this.getItemBySlot(EquipmentSlot.OFFHAND);
				boolean hasTotemNow = totemAfter != null && !totemAfter.isEmpty() && totemAfter.is(net.minecraft.world.item.Items.TOTEM_OF_UNDYING);

				if (!hasTotemNow) {
					this.botAI.onTotemUsed();
				}
			}

			for (EquipmentSlot slot : EquipmentSlot.values()) {
				if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
					ItemStack armorPiece = this.getItemBySlot(slot);
					if (armorPiece != null && !armorPiece.isEmpty() && armorPiece.isDamageableItem()) {
						armorPiece.setDamageValue(0);
						this.setItemSlot(slot, armorPiece);
					}
				}
			}
		}

		return result;
	}

	@Override
	public boolean canPickUpLoot() {
		return false;
	}

	@Override
	public boolean canBeCollidedWith() {
		return false;
	}

	@Override
	public boolean isPushable() {
		return false;
	}

	@Override
	public void playerTouch(Player player) {
		if (player == this) return;

		if (player instanceof TrainingBot || player.getClass() == Player.class) {
			super.playerTouch(player);
		}
	}

	@Override
	public CraftHumanEntity getBukkitEntity() {
		if (super.getBukkitEntity() == null) {
			return new BotCraftPlayer(this);
		}
		return super.getBukkitEntity();
	}

	@Override
	public void aiStep() {
		try {
			super.aiStep();
		} catch (ClassCastException e) {

		}
	}

	public org.bukkit.entity.Player getTargetPlayer() {
		return this.targetPlayer;
	}

	public BotAI getBotAI() {
		return this.botAI;
	}

	public boolean isFollow() {
		return follow;
	}

	public void setFollow(boolean follow) {
		this.follow = follow;
	}

	public int getTotemCount() {
		return this.totemCount;
	}

	public void setTotemCount(int count) {
		this.totemCount = count;
	}

	private static class BotCraftPlayer extends CraftHumanEntity {
		public BotCraftPlayer(TrainingBot entity) {
			super((org.bukkit.craftbukkit.CraftServer) Bukkit.getServer(), entity);
		}

		@Override
		public String toString() {
			return "BotCraftPlayer{name=" + getName() + "}";
		}
	}
}
