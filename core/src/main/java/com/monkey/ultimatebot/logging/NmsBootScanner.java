package com.monkey.ultimatebot.logging;

import com.monkey.ultimatebot.nms.INMSBridge;
import com.monkey.ultimatebot.nms.NMSBridgeManager;
import org.bukkit.Material;
import org.jspecify.annotations.Nullable;

public final class NmsBootScanner {

    private final BootLogger boot;
    private int checked;
    private int found;
    private int skipped;
    private int materialChecked;
    private int materialFound;
    private int packetChecked;
    private int packetFound;
    private int reflectionChecked;
    private int reflectionFound;

    public NmsBootScanner(BootLogger boot) {
        this.boot = boot;
    }

    public void scan() {
        INMSBridge bridge = NMSBridgeManager.get();
        boot.nms("Bridge -> " + bridge.getClass().getSimpleName());

        scanMaterials();
        scanPackets();
        scanReflectionClasses();

        boot.nms("Scanned: " + checked + " | found: " + found + " | skipped: " + skipped);
    }

    private void scanMaterials() {
        probeMaterial("TOTEM_OF_UNDYING", null, true);
        probeMaterial("MACE", "requires 1.21+", false);
        probeMaterial("WIND_CHARGE", "requires 1.21+", false);
        probeMaterial("CROSSBOW", "requires 1.14+", false);
        probeMaterial("NETHERITE_SWORD", "requires 1.16+", false);
        probeMaterial("NETHERITE_HELMET", "requires 1.16+", false);
        probeMaterial("RESPAWN_ANCHOR", "requires 1.16+", false);
        probeMaterial("END_CRYSTAL", null, false);
        probeMaterial("SHIELD", "requires 1.9+", false);
        probeMaterial("RECOVERY_COMPASS", "requires 1.19+", false);
        probeMaterial("PIGLIN_HEAD", "requires 1.16+", false);
        probeMaterial("SHORT_GRASS", "requires 1.20.3+", false);
        probeMaterial("SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE", "requires 1.20+", false);
        probeMaterial("TNT_MINECART", null, false);
        probeMaterial("TRIDENT", "requires 1.13+", false);
        boot.nms("Materials -> " + materialFound + "/" + materialChecked + " available");
    }

    private void scanPackets() {
        probeClass("ClientboundSetEntityDataPacket",
                "net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket", "requires 1.17+", false, "Packet");
        probeClass("ClientboundAddEntityPacket",
                "net.minecraft.network.protocol.game.ClientboundAddEntityPacket", "requires 1.17+", false, "Packet");
        probeClass("ClientboundRemoveEntitiesPacket",
                "net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket", "requires 1.17+", false, "Packet");
        probeClass("ClientboundTeleportEntityPacket",
                "net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket", "requires 1.17+", false, "Packet");
        probeClass("ClientboundRotateHeadPacket",
                "net.minecraft.network.protocol.game.ClientboundRotateHeadPacket", "requires 1.17+", false, "Packet");
        probeClass("ClientboundPlayerInfoUpdatePacket",
                "net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket", "requires 1.19.3+", false, "Packet");
        boot.nms("Packets -> " + packetFound + "/" + packetChecked + " available");
    }

    private void scanReflectionClasses() {
        probeClass("ServerPlayer",
                "net.minecraft.server.level.ServerPlayer", "requires 1.17+", false, "Reflect");
        probeClass("ServerLevel",
                "net.minecraft.server.level.ServerLevel", "requires 1.17+", false, "Reflect");
        probeClass("GameProfile",
                "com.mojang.authlib.GameProfile", null, true, "Reflect");
        probeClass("ServerGamePacketListenerImpl",
                "net.minecraft.server.network.ServerGamePacketListenerImpl", "requires 1.17+", false, "Reflect");
        boot.nms("Reflection -> " + reflectionFound + "/" + reflectionChecked + " available");
    }

    private void probeMaterial(String name, @Nullable String versionNote, boolean alwaysLog) {
        checked++;
        materialChecked++;
        Material matched = Material.matchMaterial(name);
        if (matched != null) {
            found++;
            materialFound++;
            if (alwaysLog) {
                boot.nms("Material " + name + " -> FOUND");
            }
        } else {
            skipped++;
            String suffix = versionNote != null ? " (" + versionNote + ")" : "";
            boot.nms("Material " + name + " -> NOT FOUND" + suffix);
        }
    }

    private static boolean isRecord(Class<?> clazz) {
        try {
            return (Boolean) Class.class.getMethod("isRecord").invoke(clazz);
        } catch (ReflectiveOperationException e) {
            return false;
        }
    }

    private void probeClass(
            String shortName,
            String className,
            @Nullable String versionNote,
            boolean alwaysLog,
            String type) {
        checked++;
        if ("Packet".equals(type)) {
            packetChecked++;
        } else {
            reflectionChecked++;
        }
        try {
            Class<?> clazz = Class.forName(className, false, getClass().getClassLoader());
            found++;
            if ("Packet".equals(type)) {
                packetFound++;
            } else {
                reflectionFound++;
            }
            String extra = isRecord(clazz) ? " (record-based)" : "";
            if (alwaysLog) {
                boot.nms(type + " " + shortName + " -> FOUND" + extra);
            }
        } catch (ClassNotFoundException e) {
            skipped++;
            String suffix = versionNote != null ? " (" + versionNote + ")" : "";
            boot.nms(type + " " + shortName + " -> NOT FOUND" + suffix);
        }
    }
}
