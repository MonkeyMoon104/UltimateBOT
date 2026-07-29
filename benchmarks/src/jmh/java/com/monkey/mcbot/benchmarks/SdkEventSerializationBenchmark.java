package com.monkey.mcbot.benchmarks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.monkey.mcbot.sdk.event.BotEventEnvelope;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

@State(Scope.Thread)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
public class SdkEventSerializationBenchmark {
    private ObjectMapper mapper;
    private BotEventEnvelope event;
    private byte[] json;

    @Setup
    public void setUp() throws Exception {
        mapper = new ObjectMapper().findAndRegisterModules();
        event = new BotEventEnvelope(
                42L,
                1,
                "ATTACK",
                UUID.fromString("6a868736-d1fc-42ce-86a5-8ca3bd43c331"),
                123L,
                Instant.parse("2026-01-01T00:00:00Z"),
                UUID.fromString("f35be66a-c28e-4ed0-a473-361d2dab4fc8"),
                UUID.fromString("779339f2-b6be-4276-8b12-0054e36ed385"),
                "PLUGIN",
                null,
                Map.of("attackType", "MELEE", "targetType", "MOB"));
        json = mapper.writeValueAsBytes(event);
    }

    @Benchmark
    public void serialize(Blackhole blackhole) throws Exception {
        blackhole.consume(mapper.writeValueAsBytes(event));
    }

    @Benchmark
    public void deserialize(Blackhole blackhole) throws Exception {
        blackhole.consume(mapper.readValue(json, BotEventEnvelope.class));
    }
}
