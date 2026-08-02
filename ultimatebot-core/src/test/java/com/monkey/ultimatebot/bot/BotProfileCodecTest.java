package com.monkey.ultimatebot.bot;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.Test;

class BotProfileCodecTest {

    @Test
    void sanitizesColorsSymbolsAndLengthForMinecraftProfiles() {
        assertThat(BotProfileCodec.sanitizeName("§aLong Bot Name 123456")).isEqualTo("Long_Bot_Name_12");
        assertThat(BotProfileCodec.sanitizeName("&c")).isEqualTo("CrystalBot");
        assertThat(BotProfileCodec.sanitizeName(null)).isEqualTo("CrystalBot");
    }

    @Test
    void createsMojangTexturePayloadFromUrl() {
        String url = "https://textures.minecraft.net/texture/example";
        String encoded = BotProfileCodec.textureValueFromUrl(url);
        String decoded = new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);

        assertThat(decoded).isEqualTo("{\"textures\":{\"SKIN\":{\"url\":\"" + url + "\"}}}");
        assertThat(BotProfileCodec.textureValueFromUrl(" ")).isNull();
    }
}
