/**
 * Spigot 1.14.4 ({@code v1_14_R1}) NMS bridge — same fake-player runtime as 1.15.x, with only this
 * revision's NMS surface changed ({@code locX} fields, no {@code CONSUME}, TNT {@code c(double)}).
 *
 * <p><b>Supported (this pass):</b> fake-player spawn, world tick, tab-list add/remove, named-entity
 * spawn / metadata, equipment packets, motion + position broadcast, attack, inventory, profile
 * textures, enderpearl throw, CPvP place, TNT minecarts, swimming pose.
 *
 * <p><b>GUI:</b> pre-1.17 uses core {@code LegacyBotGui} (Bukkit inventory, no InvUI) via
 * {@code INMSBridge#openBotGui}.
 *
 * <p><b>Gaps:</b> full crystal AI parity with 1.17+ is not claimed.
 */
@org.jspecify.annotations.NullMarked
package com.monkey.ultimatebot.nms;