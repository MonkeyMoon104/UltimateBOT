package it.coralmc.sandbox.gui.builder;

import it.coralmc.sandbox.SandboxTraining;
import it.coralmc.sandbox.utils.chatcolor.ChatColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class GUIItemBuilder {

	private final ChatColorUtils chatColorUtils;
	private final FileConfiguration config;

	public GUIItemBuilder(SandboxTraining plugin) {
		this.config = plugin.getConfig();
		this.chatColorUtils = plugin.getChatColorUtils();
	}


	public ItemStack createSpawnButton() {
		String materialName = config.getString("gui.spawn-button.material", "SPAWNER");
		Material material = getMaterialSafely(materialName, Material.SPAWNER, "spawn-button");

		ItemStack item = new ItemStack(material);
		ItemMeta meta = item.getItemMeta();

		String name = config.getString("gui.spawn-button.name", "&aSpawn Bot");
		meta.setDisplayName(chatColorUtils.translate(name));

		List<String> lore = getLoreFromConfig("gui.spawn-button.lore");
		if (!lore.isEmpty()) {
			meta.setLore(lore);
		}

		item.setItemMeta(meta);
		return item;
	}

	public ItemStack createDespawnButton() {
		String materialName = config.getString("gui.despawn-button.material", "TNT");
		Material material = getMaterialSafely(materialName, Material.TNT, "despawn-button");

		ItemStack item = new ItemStack(material);
		ItemMeta meta = item.getItemMeta();

		String name = config.getString("gui.despawn-button.name", "&cDespawn Bot");
		meta.setDisplayName(chatColorUtils.translate(name));

		List<String> lore = getLoreFromConfig("gui.despawn-button.lore");
		if (!lore.isEmpty()) {
			meta.setLore(lore);
		}

		item.setItemMeta(meta);
		return item;
	}

	public ItemStack createSaveButton() {
		String materialName = config.getString("gui.save-button.material", "GREEN_WOOL");
		Material material = getMaterialSafely(materialName, Material.GREEN_WOOL, "save-button");

		ItemStack item = new ItemStack(material);
		ItemMeta meta = item.getItemMeta();

		String name = config.getString("gui.save-button.name", "&aSave Changes");
		meta.setDisplayName(chatColorUtils.translate(name));

		List<String> lore = getLoreFromConfig("gui.save-button.lore");
		if (!lore.isEmpty()) {
			meta.setLore(lore);
		}

		item.setItemMeta(meta);
		return item;
	}

	public ItemStack createFollowButton(boolean followStatus) {
		String materialName = config.getString("gui.follow-button.material", "LEAD");
		Material material = getMaterialSafely(materialName, Material.LEAD, "follow-button");

		ItemStack item = new ItemStack(material);
		ItemMeta meta = item.getItemMeta();

		String displayName = config.getString("gui.follow-button.name", "&bToggle Follow");
		meta.setDisplayName(chatColorUtils.translate(displayName));

		List<String> lore = getLoreFromConfigWithPlaceholder("gui.follow-button.lore", "%type%",
			followStatus ? "true" : "false");
		if (!lore.isEmpty()) {
			meta.setLore(lore);
		}

		item.setItemMeta(meta);
		return item;
	}

	public ItemStack createTotemButton(int totemCount) {
		String materialName = config.getString("gui.totem-button.material", "TOTEM_OF_UNDYING");
		Material material = getMaterialSafely(materialName, Material.TOTEM_OF_UNDYING, "totem-button");

		ItemStack item = new ItemStack(material);
		ItemMeta meta = item.getItemMeta();

		String name = config.getString("gui.totem-button.name", "&eTotem Count");
		meta.setDisplayName(chatColorUtils.translate(name));

		String countValue = totemCount == -1 ?
			config.getString("gui.totem-button.unlimited-text", "Unlimited") :
			String.valueOf(totemCount);

		List<String> lore = getLoreFromConfigWithPlaceholder("gui.totem-button.lore", "%count%", countValue);
		if (!lore.isEmpty()) {
			meta.setLore(lore);
		}

		item.setItemMeta(meta);
		return item;
	}

	public ItemStack createEnchantButton(boolean blastProtectionEnabled) {
		String enabledMaterialName = config.getString("gui.enchant-button.enabled-material", "LIME_STAINED_GLASS_PANE");
		String disabledMaterialName = config.getString("gui.enchant-button.disabled-material", "GLASS_PANE");

		Material enabledMaterial = getMaterialSafely(enabledMaterialName, Material.LIME_STAINED_GLASS_PANE, "enchant-button-enabled");
		Material disabledMaterial = getMaterialSafely(disabledMaterialName, Material.GLASS_PANE, "enchant-button-disabled");

		Material material = blastProtectionEnabled ? enabledMaterial : disabledMaterial;

		ItemStack item = new ItemStack(material);
		ItemMeta meta = item.getItemMeta();

		if (meta != null) {
			String name = blastProtectionEnabled ?
					config.getString("gui.enchant-button.enabled-name", "&aBlast Protection: ON") :
					config.getString("gui.enchant-button.disabled-name", "&cBlast Protection: OFF");

			meta.setDisplayName(chatColorUtils.translate(name));

			List<String> lore = blastProtectionEnabled ?
					getLoreFromConfig("gui.enchant-button.enabled-lore") :
					getLoreFromConfig("gui.enchant-button.disabled-lore");

			if (lore.isEmpty()) {
				String fallbackLore = blastProtectionEnabled ?
						config.getString("gui.enchant-button.fallback-enabled-lore", "&aClicca per disabilitare") :
						config.getString("gui.enchant-button.fallback-disabled-lore", "&cClicca per abilitare");
				lore = Collections.singletonList(chatColorUtils.translate(fallbackLore));
			}

			meta.setLore(lore);
			item.setItemMeta(meta);
		}

		return item;
	}

	public ItemStack createArmorItem(Material material, boolean hasBlastProtection) {
		ItemStack item = new ItemStack(material);
		ItemMeta meta = item.getItemMeta();

		if (meta != null) {
			List<String> lore = getLoreFromConfigWithPlaceholder(
					"gui.default-armor.lore.set-type",
					"%type%",
					getCleanArmorTypeName(material)
			);

			if (lore.isEmpty()) {
				String loreTemplate = config.getString("gui.default-armor.lore.set-type", "Set type = %type%");
				String loreText = loreTemplate.replace("%type%", getCleanArmorTypeName(material));
				lore = Collections.singletonList(chatColorUtils.translate(loreText));
			}

			meta.setLore(lore);

			if (hasBlastProtection) {
				meta.addEnchant(Enchantment.BLAST_PROTECTION, 4, false);
			}

			item.setItemMeta(meta);
		}

		return item;
	}

	private String getCleanArmorTypeName(Material material) {
		String name = material.name();

		name = name.replace("_HELMET", "");
		name = name.replace("_CHESTPLATE", "");
		name = name.replace("_LEGGINGS", "");
		name = name.replace("_BOOTS", "");

		return name.toLowerCase().replace("_", " ");
	}

	private Material getMaterialSafely(String materialName, Material fallback, String buttonType) {
		try {
			return Material.valueOf(materialName.toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException e) {
			Bukkit.getLogger().warning("Materiale " + buttonType + " non valido: " + materialName +
									   ", imposto " + fallback.name());
			return fallback;
		}
	}

	private List<String> getLoreFromConfig(String path) {
		List<String> configLore = config.getStringList(path);
		if (configLore.isEmpty()) {
			return new ArrayList<>();
		}

		List<String> translatedLore = new ArrayList<>();
		for (String line : configLore) {
			translatedLore.add(chatColorUtils.translate(line));
		}
		return translatedLore;
	}

	private List<String> getLoreFromConfigWithPlaceholder(String path, String placeholder, String replacement) {
		List<String> configLore = config.getStringList(path);
		if (configLore.isEmpty()) {
			String singleLore = config.getString(path);
			if (singleLore != null && !singleLore.isEmpty()) {
				String processedLine = singleLore.replace(placeholder, replacement);
				return Collections.singletonList(chatColorUtils.translate(processedLine));
			}
			return new ArrayList<>();
		}

		List<String> translatedLore = new ArrayList<>();
		for (String line : configLore) {
			String processedLine = line.replace(placeholder, replacement);
			translatedLore.add(chatColorUtils.translate(processedLine));
		}
		return translatedLore;
	}

	public ItemStack createTeleportItem() {
		String materialName = config.getString("gui.teleport-button.material", "ENDER_PEARL");
		Material material = getMaterialSafely(materialName, Material.ENDER_PEARL, "teleport-button");

		ItemStack item = new ItemStack(material);
		ItemMeta meta = item.getItemMeta();

		String name = config.getString("gui.teleport-button.name", "&cTeleport Bot");
		meta.setDisplayName(chatColorUtils.translate(name));

		List<String> lore = getLoreFromConfig("gui.teleport-button.lore");
		if (!lore.isEmpty()) {
			meta.setLore(lore);
		}

		item.setItemMeta(meta);
		return item;
	}
}