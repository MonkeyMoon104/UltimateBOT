package com.monkey.ultimatebot.sdk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.monkey.ultimatebot.common.model.settings.BlastProtectionSettings;
import com.monkey.ultimatebot.common.model.bot.BotMode;
import com.monkey.ultimatebot.common.model.brain.BrainKey;
import com.monkey.ultimatebot.common.model.combat.CombatMode;
import com.monkey.ultimatebot.common.model.combat.CombatTuning;
import com.monkey.ultimatebot.sdk.model.request.BotEquipmentSlotRequest;
import com.monkey.ultimatebot.sdk.model.request.BotSpawnRequest;
import com.monkey.ultimatebot.sdk.model.type.SdkBotEquipmentSlot;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class BotSpawnRequestTest {

    @Test
    void serializesCustomUuidAndExplicitEmptySlots() throws Exception {
        UUID botUUID = UUID.fromString("4ed787a3-1f40-45a7-bb8f-13f987420001");
        BotSpawnRequest request = BotSpawnRequest.independent()
                .botUUID(botUUID)
                .emptyEquipmentSlot(SdkBotEquipmentSlot.MAIN_HAND)
                .emptyEquipmentSlot(SdkBotEquipmentSlot.OFF_HAND)
                .equipmentItem(SdkBotEquipmentSlot.HEAD, "diamond_helmet", 1)
                .build();

        String json = new ObjectMapper().writeValueAsString(request);

        assertThat(json).contains(botUUID.toString());
        assertThat(json).contains("\"MAIN_HAND\":{\"mode\":\"EMPTY\"");
        assertThat(json).contains("\"HEAD\":{\"mode\":\"ITEM\",\"material\":\"DIAMOND_HELMET\"");
    }

    @Test
    void defaultModeRemovesAnExistingOverride() {
        BotSpawnRequest request = BotSpawnRequest.builder()
                .emptyEquipmentSlot(SdkBotEquipmentSlot.FEET)
                .defaultEquipmentSlot(SdkBotEquipmentSlot.FEET)
                .build();

        assertThat(request.equipmentSlots()).isEmpty();
    }

    @Test
    void itemModeRejectsMissingMaterial() {
        assertThatThrownBy(() -> BotEquipmentSlotRequest.item(" ", 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("material");
    }

    @Test
    void serializesCombatModeAndCustomTuning() throws Exception {
        BotSpawnRequest request = BotSpawnRequest.builder()
                .combatMode(CombatMode.WATER)
                .brain(BrainKey.of("example", "expert"))
                .combatTuning(CombatTuning.builder().attackRange(3.8D).build())
                .build();

        String json = new ObjectMapper().writeValueAsString(request);

        assertThat(json).contains("\"combatMode\":{\"namespace\":\"ultimatebot\",\"value\":\"water\"}");
        assertThat(json).contains("\"brain\":{\"namespace\":\"example\",\"value\":\"expert\"}");
        assertThat(json).contains("\"attackRange\":3.8");
    }

    @Test
    void serializesEveryOwnershipModeAndGuiPermission() throws Exception {
        UUID ownerUUID = UUID.fromString("4ed787a3-1f40-45a7-bb8f-13f987420002");
        BotSpawnRequest request = BotSpawnRequest.builder()
                .mode(BotMode.TEAM_ALLY)
                .teamOwnerUUIDs(java.util.List.of(ownerUUID))
                .changeableFollow(false)
                .changeableCombat(false)
                .changeableBlast(false)
                .blastProtection(new BlastProtectionSettings(true, false, true, false))
                .changeableArmor(false)
                .changeableTotem(false)
                .changeableDifficulty(false)
                .changeableCombatMode(false)
                .build();

        String json = new ObjectMapper().writeValueAsString(request);

        assertThat(json).contains("\"mode\":\"TEAM_ALLY\"");
        assertThat(json).contains(ownerUUID.toString());
        assertThat(json).contains("\"changeableCombatMode\":false");
        assertThat(json).contains("\"blastProtection\":{\"boots\":true,\"leggings\":false");
    }
}
