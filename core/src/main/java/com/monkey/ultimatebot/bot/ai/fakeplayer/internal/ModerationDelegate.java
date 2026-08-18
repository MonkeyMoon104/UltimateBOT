package com.monkey.ultimatebot.bot.ai.fakeplayer.internal;

import com.destroystokyo.paper.profile.PlayerProfile;
import java.net.InetAddress;
import org.bukkit.BanEntry;
import org.jspecify.annotations.Nullable;

public final class ModerationDelegate {
    public <E extends BanEntry<? super PlayerProfile>> @Nullable E ban() {
        return null;
    }

    public @Nullable BanEntry<InetAddress> banIp() {
        return null;
    }

    public boolean isWhitelisted() {
        return false;
    }

    public void setWhitelisted(boolean value) {}
}
