package com.monkey.ultimatebot.lib;

import com.monkey.ultimatebot.access.runtime.JavaRuntimeAccess;
import com.monkey.ultimatebot.access.runtime.MinecraftVersionAccess;
import com.monkey.ultimatebot.common.lib.LibraryLoader;
import com.monkey.ultimatebot.common.lib.LibraryLoader.LibraryLoadResult;
import com.monkey.ultimatebot.common.lib.LibraryLoader.LibraryRequest;
import com.monkey.ultimatebot.common.lib.LibraryTrack;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;
import org.bukkit.plugin.java.JavaPlugin;

public final class RuntimeLibraryBootstrap {
    private RuntimeLibraryBootstrap() {}

    public static List<LibraryLoadResult> install(JavaPlugin plugin) throws Exception {
        Logger logger = plugin.getLogger();
        List<LibraryRequest> requests = selectRequests();
        logger.info("[Libs] Installing " + requests.size() + " library group(s) for "
                + "Java " + JavaRuntimeAccess.major()
                + " / MC " + MinecraftVersionAccess.minecraftVersion());
        LibraryLoader loader =
                new LibraryLoader(plugin.getDataFolder().toPath(), plugin.getClass().getClassLoader(), logger);
        return loader.loadAll(requests);
    }

    static List<LibraryRequest> selectRequests() {
        LibraryTrack sharedTrack = JavaRuntimeAccess.isAtLeast(11) ? LibraryTrack.MODERN : LibraryTrack.LEGACY;
        List<LibraryRequest> requests = new ArrayList<LibraryRequest>();
        requests.add(request("jackson", "Jackson", sharedTrack));
        requests.add(request("lamp", "Lamp", sharedTrack));
        requests.add(request("configurate", "Configurate", sharedTrack));
        requests.add(request("pathetic", "Pathetic", sharedTrack));
        requests.add(request("bstats", "bStats", sharedTrack));

        if (JavaRuntimeAccess.isAtLeast(11)) {
            requests.add(request("caffeine-modern", "Caffeine 3.x", LibraryTrack.MODERN));
        } else {
            requests.add(request("caffeine-legacy", "Caffeine 2.x", LibraryTrack.LEGACY));
        }

        String minecraft = MinecraftVersionAccess.minecraftVersion().toLowerCase(Locale.ROOT);
        if (minecraft.startsWith("26.3")) {
            requests.add(request("invui-v2-5", "InvUI 2.5", LibraryTrack.MODERN));
        } else if (minecraft.startsWith("26.2")) {
            requests.add(request("invui-v2-2", "InvUI 2.2", LibraryTrack.MODERN));
        } else if (minecraft.startsWith("26.")) {
            requests.add(request("invui-v2-1", "InvUI 2.1", LibraryTrack.MODERN));
        } else {
            requests.add(request("invui-v1", "InvUI 1.x", LibraryTrack.LEGACY));
        }
        return requests;
    }

    private static LibraryRequest request(String id, String displayName, LibraryTrack track) {
        return new LibraryRequest(
                id,
                displayName,
                "META-INF/ultimatebot/libs/" + id + ".properties",
                "ultimatebot.lib." + id,
                track);
    }
}
