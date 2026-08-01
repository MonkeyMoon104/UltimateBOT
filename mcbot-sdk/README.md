# mcbot-sdk

## Custom bot UUID and equipment slots

Remote event bots can use a caller-provided UUID and persistent equipment settings:

```java
EventBotSpawnRequest request = EventBotSpawnRequest.independent()
        .botUUID(UUID.fromString("4ed787a3-1f40-45a7-bb8f-13f987420001"))
        .emptyEquipmentSlot(SdkBotEquipmentSlot.MAIN_HAND)
        .emptyEquipmentSlot(SdkBotEquipmentSlot.OFF_HAND)
        .equipmentItem(SdkBotEquipmentSlot.HEAD, "DIAMOND_HELMET", 1)
        .build();

client.spawnEventBot(request);
```

Slots can also be changed at runtime with `updateEquipmentSlot(...)`. `DEFAULT` returns control to the normal bot
AI, `ITEM` keeps the configured material equipped, and `EMPTY` keeps the slot empty.

`mcbot-sdk` is the Java-only remote client for MinecraftBot.

The SDK publishes JSpecify nullability contracts. Public parameters and return values are
non-null by default, while optional response fields and builder values are explicitly
annotated with `@Nullable`.

Use this artifact with `implementation` when you do not want to depend on Bukkit/Paper classloading or the in-server `mcbot-api` singleton.

The SDK receives the dependency-free `common` contracts transitively and keeps its existing SDK-specific model names as compatibility façades.

## Gradle

```gradle
dependencies {
    implementation "com.monkey.mcbot:mcbot-sdk:<version>"
}
```

## Server Config

Enable the remote API in `config.yml`:

```yaml
remote-api:
  enabled: true
  host: "127.0.0.1"
  port: 8765
  base-path: "/mcbot/api/v1"
  token: "replace-with-a-secret-token"
```

Keep the host on `127.0.0.1` unless you intentionally put it behind a firewall or reverse proxy.

## Example

```java
MinecraftBotClient client = MinecraftBotClient.builder()
        .baseUri("http://127.0.0.1:8765/mcbot/api/v1/")
        .token("replace-with-a-secret-token")
        .build();

BotOperationResponse spawn = client.spawnEventBot(EventBotSpawnRequest.builder()
        .botNameTemplate("EventBot")
        .autoTarget(true)
        .explosions(true)
        .explosionBlockDamage(false)
        .build());

UUID botId = spawn.snapshot().ownerUUID();
client.updateEnderPearls(botId, false);
client.remove(botId);
```

## Remote EventBus

The SDK exposes a reconnecting Server-Sent Events client. It authenticates with the
same token, sends `Last-Event-ID` after reconnects and supports server-side type filters.

```java
BotEventSubscription events = client.events().subscribe(
        Set.of(SdkBotEventType.SPAWNED, SdkBotEventType.KILLED_ENTITY,
                SdkBotEventType.EXPLOSION_PREPARED),
        event -> System.out.println(event.type() + " -> " + event.payload())
);

// later
events.close();
```

Each `BotEventEnvelope` includes schema version, event ID, per-bot sequence, timestamp,
owner/bot UUIDs, source, snapshot and an event-specific payload. Unknown future event
types remain readable through the envelope string instead of breaking deserialization.

The raw authenticated endpoint is `GET /mcbot/api/v1/events`; optional filtering uses
`?types=SPAWNED,DIED,TOTEM_USED`. Streams can also be filtered with `ownerUUID` or
`botUUID`; the SDK exposes `subscribeForOwner(...)` and `subscribeForBot(...)` helpers.

## Supported Operations

- `health()`
- `listBots()`
- `spawnEventBot(...)`
- `remove(UUID ownerUUID)`
- `removeAll()`
- `updateCrystalPvp(UUID ownerUUID, boolean enabled)`
- `updateExplosions(UUID ownerUUID, boolean enabled)`
- `updateExplosionBlockDamage(UUID ownerUUID, boolean enabled)`
- `updateEnderPearls(UUID ownerUUID, boolean enabled)`
- `events().subscribe(...)`
