/**
 * Spigot 1.8.3 ({@code v1_8_R2}) NMS bridge.
 *
 * <p><b>Supported (this pass):</b> fake-player spawn, world tick, tab-list add, named-entity spawn /
 * metadata, equipment packets, basic move / velocity / on-ground, attack, inventory, profile
 * textures, enderpearl throw, explosions where the revision API allows.
 *
 * <p><b>GUI:</b> pre-1.17 uses core {@code LegacyBotGui} (Bukkit inventory, no InvUI) via
 * {@code INMSBridge#openBotGui}.
 *
 * <p><b>Gaps:</b> full crystal AI parity with 1.17+ is not claimed.
 */
@org.jspecify.annotations.NullMarked
package com.monkey.ultimatebot.nms;