/**
 * Spigot/Paper 1.16.5 ({@code v1_16_R3}) NMS bridge.
 *
 * <p><b>Supported (this pass):</b> fake-player spawn, world tick, tab-list add, named-entity spawn /
 * metadata, equipment packets (incl. offhand), basic move / velocity / on-ground, attack, inventory,
 * profile textures, enderpearl throw, explosions.
 *
 * <p><b>GUI:</b> 1.14+ uses core {@code NewBotGUI} (InvUI) via {@code INMSBridge#openBotGui};
 * older revisions keep {@code LegacyBotGui}.
 *
 * <p><b>Gaps:</b> full AI parity with 1.17+ is not claimed. NMS revision {@code v1_16_R3} covers
 * 1.16.4–1.16.5; see {@code v1_16_R1}/{@code v1_16_R2} for 1.16–1.16.3.
 */
@org.jspecify.annotations.NullMarked
package com.monkey.ultimatebot.nms;
