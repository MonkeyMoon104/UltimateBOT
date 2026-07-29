package com.monkey.mcbot.remote;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.monkey.mcbot.MinecraftBot;
import com.monkey.mcbot.api.event.action.BotHealEvent;
import com.monkey.mcbot.api.event.action.BotTeleportEvent;
import com.monkey.mcbot.api.event.base.BotEvent;
import com.monkey.mcbot.api.event.combat.BotAttackEvent;
import com.monkey.mcbot.api.event.combat.BotDamageEvent;
import com.monkey.mcbot.api.event.combat.BotExplosionEvent;
import com.monkey.mcbot.api.event.combat.BotKillEntityEvent;
import com.monkey.mcbot.api.event.combat.BotTotemUseEvent;
import com.monkey.mcbot.api.event.lifecycle.BotDeathEvent;
import com.monkey.mcbot.api.event.lifecycle.BotDespawnEvent;
import com.monkey.mcbot.api.event.lifecycle.BotDespawnPrepareEvent;
import com.monkey.mcbot.api.event.lifecycle.BotSpawnEvent;
import com.monkey.mcbot.api.event.lifecycle.BotSpawnPrepareEvent;
import com.monkey.mcbot.api.event.state.BotSettingsChangeEvent;
import com.monkey.mcbot.api.event.state.BotTargetChangeEvent;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/** Bounded replayable Server-Sent Events bridge for SDK clients. */
final class RemoteEventStream implements AutoCloseable {
    private static final int BUFFER_SIZE = 256;
    private final ObjectMapper mapper;
    private final AtomicLong ids = new AtomicLong();
    private final Deque<RemoteBotEvent> replay = new ArrayDeque<>();
    private final CopyOnWriteArrayList<Client> clients = new CopyOnWriteArrayList<>();
    private final AutoCloseable observer;

    RemoteEventStream(MinecraftBot plugin, ObjectMapper mapper) {
        this.mapper = mapper;
        this.observer = plugin.getBotEventDispatcher().observe(this::publish);
    }

    void handle(HttpExchange exchange) throws IOException {
        Map<String, String> filters = parseFilters(exchange.getRequestURI().getQuery());
        Set<String> types = parseTypes(filters.get("types"));
        long lastId = parseLastId(exchange.getRequestHeaders().getFirst("Last-Event-ID"));
        exchange.getResponseHeaders().set("Content-Type", "text/event-stream; charset=UTF-8");
        exchange.getResponseHeaders().set("Cache-Control", "no-cache");
        exchange.getResponseHeaders().set("Connection", "keep-alive");
        exchange.sendResponseHeaders(200, 0);

        Client client = new Client(types, parseUuid(filters.get("ownerUUID")), parseUuid(filters.get("botUUID")));
        clients.add(client);
        try (OutputStream output = exchange.getResponseBody()) {
            for (RemoteBotEvent event : replayAfter(lastId)) {
                if (client.accepts(event)) write(output, event);
            }
            output.write(": connected\n\n".getBytes(StandardCharsets.UTF_8));
            output.flush();
            while (true) {
                RemoteBotEvent event;
                try {
                    event = client.queue.poll(15, TimeUnit.SECONDS);
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    return;
                }
                if (event == null) {
                    output.write(": heartbeat\n\n".getBytes(StandardCharsets.UTF_8));
                    output.flush();
                } else {
                    write(output, event);
                }
            }
        } finally {
            clients.remove(client);
        }
    }

    private void publish(BotEvent event) {
        RemoteBotEvent remote = map(event);
        synchronized (replay) {
            replay.addLast(remote);
            while (replay.size() > BUFFER_SIZE) replay.removeFirst();
        }
        for (Client client : clients) {
            if (!client.accepts(remote)) continue;
            if (!client.queue.offer(remote)) {
                client.queue.poll();
                client.queue.offer(remote);
            }
        }
    }

    private RemoteBotEvent map(BotEvent event) {
        Map<String, Object> payload = new LinkedHashMap<>();
        String type = typeOf(event);
        if (event instanceof BotDespawnEvent value) payload.put("reason", value.getReason().name());
        if (event instanceof BotDeathEvent value) payload.put("damageType", value.getDamageType());
        if (event instanceof BotKillEntityEvent value) {
            payload.put("victimUUID", value.getVictim().getUniqueId());
            payload.put("victimType", value.getVictim().getType().name());
        }
        if (event instanceof BotTargetChangeEvent value) {
            payload.put("previousTargetUUID", value.getPreviousTarget() == null ? null : value.getPreviousTarget().getUniqueId());
            payload.put("newTargetUUID", value.getNewTarget() == null ? null : value.getNewTarget().getUniqueId());
            payload.put("newTargetType", value.getNewTarget() == null ? null : value.getNewTarget().getType().name());
        }
        if (event instanceof BotSettingsChangeEvent value) {
            payload.put("setting", value.getSetting().name());
            payload.put("oldValue", String.valueOf(value.getOldValue()));
            payload.put("newValue", String.valueOf(value.getNewValue()));
        }
        if (event instanceof BotAttackEvent value) {
            payload.put("attackType", value.getAttackType().name());
            payload.put("targetUUID", value.getTarget().getUniqueId());
            payload.put("targetType", value.getTarget().getType().name());
        }
        if (event instanceof BotExplosionEvent value) {
            payload.put("explosionType", value.getExplosionType().name());
            payload.put("blockDamage", value.isBlockDamage());
            payload.put("world", value.getLocation().getWorld() == null ? null : value.getLocation().getWorld().getName());
            payload.put("x", value.getLocation().getX());
            payload.put("y", value.getLocation().getY());
            payload.put("z", value.getLocation().getZ());
        }
        if (event instanceof BotDamageEvent value) {
            payload.put("cause", value.getCause());
            payload.put("damage", value.getDamage());
        }
        if (event instanceof BotHealEvent value) {
            payload.put("reason", value.getReason());
            payload.put("amount", value.getAmount());
        }
        if (event instanceof BotTeleportEvent value) {
            payload.put("cause", value.getCause());
            payload.put("world", value.getTo().getWorld() == null ? null : value.getTo().getWorld().getName());
            payload.put("x", value.getTo().getX());
            payload.put("y", value.getTo().getY());
            payload.put("z", value.getTo().getZ());
        }
        if (event instanceof BotTotemUseEvent value) {
            payload.put("consumed", value.getConsumed());
            payload.put("remaining", value.getRemaining());
        }
        return new RemoteBotEvent(ids.incrementAndGet(), 1, type, event.getEventId(), event.getSequence(),
                event.getOccurredAt(), event.getOwnerUUID(), event.getBotUUID(), event.getSource().name(),
                event.getBotSnapshot(), Collections.unmodifiableMap(new LinkedHashMap<>(payload)));
    }

    private static String typeOf(BotEvent event) {
        if (event instanceof BotSpawnEvent) return "SPAWNED";
        if (event instanceof BotSpawnPrepareEvent) return "SPAWN_ACCEPTED";
        if (event instanceof BotDespawnEvent) return "DESPAWNED";
        if (event instanceof BotDespawnPrepareEvent) return "DESPAWN_ACCEPTED";
        if (event instanceof BotDeathEvent) return "DIED";
        if (event instanceof BotKillEntityEvent) return "KILLED_ENTITY";
        if (event instanceof BotTargetChangeEvent) return "TARGET_CHANGED";
        if (event instanceof BotSettingsChangeEvent) return "SETTINGS_CHANGE_ACCEPTED";
        if (event instanceof BotAttackEvent) return "ATTACK_STARTED";
        if (event instanceof BotExplosionEvent) return "EXPLOSION_PREPARED";
        if (event instanceof BotDamageEvent) return "DAMAGE_ACCEPTED";
        if (event instanceof BotHealEvent) return "HEAL_ACCEPTED";
        if (event instanceof BotTeleportEvent) return "TELEPORT_ACCEPTED";
        if (event instanceof BotTotemUseEvent) return "TOTEM_USED";
        return event.getClass().getSimpleName().toUpperCase(Locale.ROOT);
    }

    private void write(OutputStream output, RemoteBotEvent event) throws IOException {
        String frame = "id: " + event.id() + "\nevent: " + event.type()
                + "\ndata: " + mapper.writeValueAsString(event) + "\n\n";
        output.write(frame.getBytes(StandardCharsets.UTF_8));
        output.flush();
    }

    private List<RemoteBotEvent> replayAfter(long lastId) {
        synchronized (replay) {
            return replay.stream().filter(event -> event.id() > lastId).toList();
        }
    }

    private static long parseLastId(String value) {
        try { return value == null ? 0L : Long.parseLong(value); }
        catch (NumberFormatException ignored) { return 0L; }
    }

    private static Set<String> parseTypes(String value) {
        if (value == null || value.isBlank()) return Set.of();
        Set<String> result = new HashSet<>();
        for (String type : value.split(",")) result.add(type.trim().toUpperCase(Locale.ROOT));
        return Set.copyOf(result);
    }

    private static Map<String, String> parseFilters(String query) {
        if (query == null || query.isBlank()) return Map.of();
        Map<String, String> result = new HashMap<>();
        for (String part : query.split("&")) {
            int separator = part.indexOf('=');
            if (separator > 0) result.put(part.substring(0, separator), part.substring(separator + 1));
        }
        return result;
    }

    private static UUID parseUuid(String value) {
        try { return value == null || value.isBlank() ? null : UUID.fromString(value); }
        catch (IllegalArgumentException ignored) { return null; }
    }

    @Override
    public void close() {
        try { observer.close(); } catch (Exception ignored) { }
        clients.clear();
        synchronized (replay) { replay.clear(); }
    }

    private static final class Client {
        private final Set<String> types;
        private final UUID ownerUUID;
        private final UUID botUUID;
        private final ArrayBlockingQueue<RemoteBotEvent> queue = new ArrayBlockingQueue<>(BUFFER_SIZE);
        private Client(Set<String> types, UUID ownerUUID, UUID botUUID) {
            this.types = types;
            this.ownerUUID = ownerUUID;
            this.botUUID = botUUID;
        }
        private boolean accepts(RemoteBotEvent event) {
            return (types.isEmpty() || types.contains(event.type()))
                    && (ownerUUID == null || ownerUUID.equals(event.ownerUUID()))
                    && (botUUID == null || botUUID.equals(event.botUUID()));
        }
    }
}
