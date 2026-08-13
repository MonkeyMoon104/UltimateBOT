package com.monkey.ultimatebot.license;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Machine / installation fingerprints for license binding.
 *
 * <p>Paper 1.17 (Java 16) vs 1.17.1+ often disagree on {@code NetworkInterface.isUp()/isVirtual()}
 * and on {@code java.vendor}. That minted a <em>new</em> host fingerprint and tripped
 * {@code HOST_LIMIT_REACHED} even on the same PC. Dual-path:
 *
 * <ul>
 *   <li>stable — JVM-agnostic (no vendor; all non-loopback MACs, ignore up/virtual)
 *   <li>legacy — exact pre-fix algorithm (vendor + strict NIC filters) for already-bound hosts
 * </ul>
 */
public final class ServerFingerprintService {

    public String computeInstallationFingerprint(JavaPlugin plugin) {
        try {
            List<String> signals = new ArrayList<>();
            signals.addAll(stableHostSignals());
            signals.add(
                    "path=" + safePath(plugin.getServer().getWorldContainer().toPath()));
            return sha256Hex(String.join("|", signals));
        } catch (Exception ex) {
            return sha256Hex(plugin.getDataFolder().getAbsolutePath());
        }
    }

    /** Preferred host id: same machine across JVMs / MC versions (incl. pure 1.17). */
    public String computeHostFingerprint(JavaPlugin plugin) {
        try {
            return sha256Hex(String.join("|", stableHostSignals()));
        } catch (Exception ex) {
            return sha256Hex(safeHostName());
        }
    }

    /**
     * Exact pre-fix host id ({@code java.vendor} + strict NIC filters). Fallback so hosts bound
     * by 1.17.1+ before the stable algorithm keep validating.
     */
    public String computeHostFingerprintLegacy(JavaPlugin plugin) {
        try {
            List<String> signals = new ArrayList<>();
            signals.add("host=" + safeHostName());
            signals.add("os=" + System.getProperty("os.name", ""));
            signals.add("arch=" + System.getProperty("os.arch", ""));
            signals.add("java=" + System.getProperty("java.vendor", ""));
            signals.add("macs=" + String.join(",", collectMacs(true)));
            return sha256Hex(String.join("|", signals));
        } catch (Exception ex) {
            return computeHostFingerprint(plugin);
        }
    }

    public int resolveServerPort(JavaPlugin plugin) {
        return plugin.getServer().getPort();
    }

    private List<String> stableHostSignals() throws Exception {
        List<String> signals = new ArrayList<>();
        signals.add("host=" + safeHostName());
        signals.add("os=" + System.getProperty("os.name", ""));
        signals.add("arch=" + System.getProperty("os.arch", ""));
        // Ignore isUp/isVirtual — Java 16 (Paper 1.17) disagrees with newer JVMs at boot.
        signals.add("macs=" + String.join(",", collectMacs(false)));
        return signals;
    }

    private String safeHostName() {
        try {
            return InetAddress.getLocalHost().getHostName().toLowerCase(Locale.ROOT);
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

    private List<String> collectMacs(boolean strict) throws Exception {
        List<String> values = new ArrayList<>();
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();
            if (networkInterface.isLoopback()) {
                continue;
            }
            if (strict) {
                if (!networkInterface.isUp() || networkInterface.isVirtual()) {
                    continue;
                }
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
