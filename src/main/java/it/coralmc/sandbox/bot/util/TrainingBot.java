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
	private final SandboxTraining plugin;
	private org.bukkit.entity.Player targetPlayer;
	private boolean follow;
	private int totemCount = -1;

	private int previousEquippedTotems = 0;
	private boolean skipNextTotemTracking = false;

	private final BotSpawner botSpawner;
	private final PlayerArmorManager playerArmorManager;
	private final ChatColorUtils chatColorUtils;
	private final String deadBotMessage;

	private double targetDistance = 1.0;

	public TrainingBot(Level level, BlockPos pos, float yRot, GameProfile gameProfile,
					   org.bukkit.entity.Player targetPlayer,
					   boolean follow,
					   BotSpawner botSpawner,
					   SandboxTraining plugin,
					   String deadBotMessage) {
		super(level, pos, yRot, gameProfile);
		this.setNoGravity(false);
		this.setOnGround(false);
		this.targetPlayer = targetPlayer;
		this.follow = follow;
		this.plugin = plugin;
		this.botAI = new BotAI(this, plugin);

		this.botSpawner = botSpawner;
		this.playerArmorManager = plugin.getPlayerArmorManager();
		this.chatColorUtils = plugin.getChatColorUtils();
		this.deadBotMessage = deadBotMessage;

		configureBotAI();
	}

	private void configureBotAI() {
		if (targetPlayer != null && follow) {
			Player target = ((org.bukkit.craftbukkit.entity.CraftPlayer) targetPlayer).getHandle();
			botAI.getRotationController().setInstantRotation(target);
		}
	}

	@Override
	public void tick() {
		super.tick();

		if (targetPlayer != null && !targetPlayer.isDead()) {
			Player target = ((org.bukkit.craftbukkit.entity.CraftPlayer) targetPlayer).getHandle();
			botAI.getRotationController().updateRotation(target);
		}

		ItemStack offhand = this.getItemBySlot(EquipmentSlot.OFFHAND);
		ItemStack mainhand = this.getItemBySlot(EquipmentSlot.MAINHAND);

		boolean hasOffhandTotem = offhand != null && !offhand.isEmpty() && offhand.is(net.minecraft.world.item.Items.TOTEM_OF_UNDYING);
		boolean hasMainhandTotem = mainhand != null && !mainhand.isEmpty() && mainhand.is(net.minecraft.world.item.Items.TOTEM_OF_UNDYING);

		int currentEquippedTotems = (hasOffhandTotem ? 1 : 0) + (hasMainhandTotem ? 1 : 0);

		if (!skipNextTotemTracking && previousEquippedTotems > currentEquippedTotems && totemCount > 0) {
			int consumedTotems = previousEquippedTotems - currentEquippedTotems;
			if (totemCount != -1) {
				setTotemCount(Math.max(0, totemCount - consumedTotems));
			}
		}

		skipNextTotemTracking = false;

		previousEquippedTotems = currentEquippedTotems;

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
			targetPlayer.sendMessage(chatColorUtils.translate(deadBotMessage));
		}

		if (targetPlayer != null) {
			playerArmorManager.removePlayerSettings(targetPlayer.getUniqueId());
		}

		this.discard();
		botSpawner.removeBot(this.getUUID());
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
			this.botAI.setKnockbackCooldown(20);

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
		this.skipNextTotemTracking = true;
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