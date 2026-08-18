/**
 * Spigot 1.7.10 ({@code v1_7_R4}) NMS bridge.
 *
 * <p><b>Supported (this pass):</b> fake-player spawn, scheduler tick, tab-list add/remove,
 * named-entity spawn / metadata, equipment packets, move / velocity / on-ground, attack, inventory
 * clear, profile textures.
 *
 * <p><b>GUI:</b> pre-1.17 uses core {@code LegacyBotGui} (Bukkit inventory, no InvUI) via
 * {@code INMSBridge#openBotGui}.
 *
 * <p><b>Gaps:</b> crystal / totem / offhand / combat-cooldown AI paths are capability-gated or
 * no-op. Only Minecraft 1.7.10 ({@code v1_7_R4}) is mapped — 1.7.2–1.7.9 are different Craft
 * revisions.
 */
@org.jspecify.annotations.NullMarked
package com.monkey.ultimatebot.nms;
