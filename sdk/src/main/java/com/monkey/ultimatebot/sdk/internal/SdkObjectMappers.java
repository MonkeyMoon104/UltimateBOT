package com.monkey.ultimatebot.sdk.internal;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

/**
 * Shared Jackson configuration for the remote SDK.
 *
 * <p>Public SDK types expose record-style accessors ({@code mode()}, {@code botUUID()}, …) and
 * private fields. Field visibility keeps serialization aligned with that surface; {@link
 * ParameterNamesModule} reconstructs immutable types from their single constructor.
 */
public final class SdkObjectMappers {
    private SdkObjectMappers() {}

    public static ObjectMapper createDefault() {
        return JsonMapper.builder()
                .addModule(new ParameterNamesModule())
                .addModule(new JavaTimeModule())
                .visibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
                .visibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE)
                .visibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.NONE)
                .visibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.NONE)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();
    }
}
