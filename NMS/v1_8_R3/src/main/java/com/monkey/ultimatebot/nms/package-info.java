/**
 * Spigot 1.8.8 ({@code v1_8_R3}) NMS bridge.
 *
 * <p><b>Supported (this pass):</b> fake-player spawn, world tick, tab-list add, named-entity spawn /
 * metadata, equipment packets, basic move / velocity / on-ground, attack, inventory clear, profile
 * textures.
 *
 * <p><b>GUI:</b> pre-1.17 uses core {@code LegacyBotGui} (Bukkit inventory, no InvUI) via
 * {@code INMSBridge#openBotGui}.
 *
 * <p><b>Gaps:</b> crystal / totem / offhand / combat-cooldown AI paths are capability-gated or
 * no-op; block interact and sound keys are best-effort. NMS revision {@code v1_8_R3} covers
 * 1.8.4–1.8.8; see {@code v1_8_R1}/{@code v1_8_R2} for earlier 1.8 builds.
 */
@org.jspecify.annotations.NullMarked
package com.monkey.ultimatebot.nms;
