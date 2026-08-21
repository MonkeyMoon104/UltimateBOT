package com.monkey.ultimatebot.sdk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.monkey.ultimatebot.common.model.bot.BotArmorTier;
import com.monkey.ultimatebot.common.model.bot.BotMode;
import com.monkey.ultimatebot.common.model.bot.BotTargetMode;
import com.monkey.ultimatebot.common.model.combat.CombatMode;
import com.monkey.ultimatebot.common.model.combat.DifficultyTier;
import com.monkey.ultimatebot.sdk.model.request.BotSpawnRequest;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class SdkSurfaceParityTest {

    @Test
    void clientExposesTheCompleteRemoteManagementSurface() {
        Set<String> methods = Arrays.stream(UltimateBotClient.class.getDeclaredMethods())
                .filter(method -> java.lang.reflect.Modifier.isPublic(method.getModifiers()))
                .map(Method::getName)
                .collect(Collectors.toUnmodifiableSet());

        assertThat(methods)
                .contains(
                        "listBots",
                        "activeBotCount",
                        "platform",
                        "listCombatModes",
                        "getCombatMode",
                        "listBrains",
                        "getBrain",
                        "listAddons",
                        "getBot",
                        "spawnBot",
                        "remove",
                        "removeByBotUUID",
                        "removeAll",
                        "removeBySource",
                        "updateTotems",
                        "updateFollow",
                        "updateCombat",
                        "updateBlastProtection",
                        "updateDifficulty",
                        "updateArmor",
                        "updateEquipmentSlot",
                        "updateAutoTarget",
                        "updateAttackBots",
                        "updateTargetMode",
                        "updateTargets",
                        "updateTeamOwners",
                        "updateCombatMode",
                        "updateCombatTuning",
                        "resetCombatTuning",
                        "updateBrain",
                        "resetBrain",
                        "updateWorldGuardPvpRespect",
                        "updateStayAfterOwnerDeath",
                        "updateIdleWander",
                        "updateCrystalPvp",
                        "updateExplosions",
                        "updateExplosionBlockDamage",
                        "updateEnderPearls",
                        "updateHealing",
                        "updateKillMessage",
                        "disableKillMessage",
                        "events");
    }

    @Test
    void spawnContractSupportsEveryBotAndCombatModeWithCanonicalTypes() throws Exception {
        UUID owner = UUID.fromString("4ed787a3-1f40-45a7-bb8f-13f987420003");
        ObjectMapper mapper = new ObjectMapper();

        for (CombatMode combatMode : CombatMode.values()) {
            BotSpawnRequest request = BotSpawnRequest.ownedBy(owner)
                    .armor(BotArmorTier.GOLDEN)
                    .difficulty(DifficultyTier.NORMAL)
                    .targetMode(BotTargetMode.PLAYERS_AND_MOBS)
                    .combatMode(combatMode)
                    .build();
            String json = mapper.writeValueAsString(request);
            assertThat(json).contains("\"mode\":\"SINGLE\"");
            assertThat(json).contains("\"armor\":\"GOLDEN\"");
            assertThat(json).contains("\"difficulty\":\"NORMAL\"");
            assertThat(json)
                    .contains(
                            "\"combatMode\":{\"namespace\":\"ultimatebot\",\"value\":\"" + combatMode.value() + "\"}");
        }

        assertThat(BotSpawnRequest.independent().build().mode()).isEqualTo(BotMode.EVENT);
        assertThat(BotSpawnRequest.builder()
                        .mode(BotMode.ALLY)
                        .ownerUUID(owner)
                        .build()
                        .mode())
                .isEqualTo(BotMode.ALLY);
        assertThat(BotSpawnRequest.builder()
                        .mode(BotMode.TEAM_ALLY)
                        .teamOwnerUUIDs(java.util.List.of(owner))
                        .build()
                        .mode())
                .isEqualTo(BotMode.TEAM_ALLY);
    }

    @Test
    void spawnContractRejectsInvalidOwnershipAndRangesBeforeNetworkIo() {
        assertThatThrownBy(() -> BotSpawnRequest.builder().mode(BotMode.SINGLE).build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ownerUUID");
        assertThatThrownBy(() ->
                        BotSpawnRequest.builder().autoTargetRange(Double.NaN).build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("autoTargetRange");
        assertThatThrownBy(() -> BotSpawnRequest.builder()
                        .armorRange(BotArmorTier.NETHERITE, BotArmorTier.LEATHER)
                        .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("armor");
    }
}
