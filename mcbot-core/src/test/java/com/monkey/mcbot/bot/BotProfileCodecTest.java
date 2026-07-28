package com.monkey.mcbot.bot;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class BotProfileCodecTest {

    @Test
    void sanitizesColorsSymbolsAndLengthForMinecraftProfiles() {
        assertEquals("Long_Bot_Name_12", BotProfileCodec.sanitizeName("§aLong Bot Name 123456"));
        assertEquals("CrystalBot", BotProfileCodec.sanitizeName("&c"));
        assertEquals("CrystalBot", BotProfileCodec.sanitizeName(null));
    }

    @Test
    void createsMojangTexturePayloadFromUrl() {
        String url = "https://textures.minecraft.net/texture/example";
        String encoded = BotProfileCodec.textureValueFromUrl(url);
        String decoded = new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);

        assertEquals("{\"textures\":{\"SKIN\":{\"url\":\"" + url + "\"}}}", decoded);
        assertNull(BotProfileCodec.textureValueFromUrl(" "));
    }
}
