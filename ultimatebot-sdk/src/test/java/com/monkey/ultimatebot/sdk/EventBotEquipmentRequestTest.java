package com.monkey.ultimatebot.sdk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.monkey.ultimatebot.sdk.model.BotEquipmentSlotRequest;
import com.monkey.ultimatebot.sdk.model.EventBotSpawnRequest;
import com.monkey.ultimatebot.sdk.model.SdkBotEquipmentSlot;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class EventBotEquipmentRequestTest {

    @Test
    void serializesCustomUuidAndExplicitEmptySlots() throws Exception {
        UUID botUUID = UUID.fromString("4ed787a3-1f40-45a7-bb8f-13f987420001");
        EventBotSpawnRequest request = EventBotSpawnRequest.independent()
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
        EventBotSpawnRequest request = EventBotSpawnRequest.builder()
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
}
