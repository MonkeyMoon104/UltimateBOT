package com.monkey.ultimatebot.license;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.*;
import org.bukkit.plugin.java.JavaPlugin;

public final class ServerFingerprintService {

    public String computeInstallationFingerprint(JavaPlugin plugin) {
        try {
            List<String> signals = new ArrayList<>();
            signals.addAll(baseSignals());
            signals.add(
                    "path=" + safePath(plugin.getServer().getWorldContainer().toPath()));
            return sha256Hex(String.join("|", signals));
        } catch (Exception ex) {
            return sha256Hex(plugin.getDataFolder().getAbsolutePath());
        }
    }

    public String computeHostFingerprint(JavaPlugin plugin) {
        try {
            return sha256Hex(String.join("|", baseSignals()));
        } catch (Exception ex) {
            return sha256Hex(safeHostName());
        }
    }

    public int resolveServerPort(JavaPlugin plugin) {
        return plugin.getServer().getPort();
    }

    private List<String> baseSignals() throws Exception {
        List<String> signals = new ArrayList<>();
        signals.add("host=" + safeHostName());
        signals.add("os=" + System.getProperty("os.name", ""));
        signals.add("arch=" + System.getProperty("os.arch", ""));
        signals.add("java=" + System.getProperty("java.vendor", ""));
        signals.add("macs=" + String.join(",", networkFingerprints()));
        return signals;
    }

    private String safeHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception ignored) {
            return "unknown-host";
        }
    }

    private String safePath(Path path) {
        try {
            return path.toRealPath().toString();
        } catch (Exception ignored) {
            return path.toAbsolutePath().toString();
        }
    }

    private List<String> networkFingerprints() throws Exception {
        List<String> values = new ArrayList<>();
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();
            if (!networkInterface.isUp() || networkInterface.isLoopback() || networkInterface.isVirtual()) {
                continue;
            }
            byte[] mac = networkInterface.getHardwareAddress();
            if (mac == null || mac.length == 0) {
                continue;
            }
            values.add(bytesToHex(mac));
        }
        values.sort(Comparator.naturalOrder());
        return values;
    }

    private String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return bytesToHex(digest.digest(input.getBytes(StandardCharsets.UTF_8)))
                    .toLowerCase(Locale.ROOT);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to compute fingerprint hash", ex);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) {
            builder.append(String.format(Locale.ROOT, "%02x", value));
        }
        return builder.toString();
    }
}
