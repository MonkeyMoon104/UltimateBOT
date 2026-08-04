# UltimateBot SDK

The SDK is the Java-only remote client for UltimateBot. Use it with `implementation` when an integration must not
depend on Bukkit/Paper class loading or the in-server `api` singleton.

It receives the dependency-free `common` contracts transitively and uses their canonical enums directly for bot
modes, combat modes, difficulty, armor, targets and creation sources. Public contracts include JSpecify nullability
annotations.

## Gradle

```gradle
dependencies {
    implementation "com.monkey.ultimatebot:sdk:<version>"
}
```

## Server configuration

Enable the authenticated Remote API in `config.yml`:

```yaml
remote-api:
  enabled: true
  host: "127.0.0.1"
  port: 8765
  base-path: "/ultimatebot/api/v1"
  token: "replace-with-a-secret-token"
```

Keep the host on `127.0.0.1` unless the endpoint is intentionally protected by a firewall or reverse proxy.

## Spawn and manage bots

Every bot mode supports a caller-provided bot UUID, complete combat settings and persistent equipment-slot rules:

```java
UltimateBotClient client = UltimateBotClient.builder()
        .baseUri("http://127.0.0.1:8765/ultimatebot/api/v1/")
        .token("replace-with-a-secret-token")
        .build();

BotSpawnRequest request = BotSpawnRequest.independent()
        .botUUID(UUID.fromString("4ed787a3-1f40-45a7-bb8f-13f987420001"))
        .combatMode(CombatMode.UHC)
        .difficulty(DifficultyTier.HARD)
        .targetMode(BotTargetMode.PLAYERS_AND_MOBS)
        .emptyEquipmentSlot(SdkBotEquipmentSlot.OFF_HAND)
        .equipmentItem(SdkBotEquipmentSlot.HEAD, "DIAMOND_HELMET", 1)
        .build();

BotOperationResponse result = client.spawnBot(request);
UUID botUUID = result.snapshot().botUUID();
client.updateEnderPearls(botUUID, false);
client.removeByBotUUID(botUUID);
```

`DEFAULT` equipment slots return control to the bot AI, `ITEM` keeps the configured material equipped and `EMPTY`
keeps the slot empty.

## Remote EventBus

The reconnecting Server-Sent Events client authenticates with the same token, resumes with `Last-Event-ID` and
supports filters by event type, owner UUID and bot UUID.

```java
BotEventSubscription events = client.events().subscribe(
        Set.of(SdkBotEventType.SPAWNED, SdkBotEventType.KILLED_ENTITY,
                SdkBotEventType.EXPLOSION_PREPARED),
        event -> System.out.println(event.type() + " -> " + event.payload()));

events.close();
```

Each `BotEventEnvelope` includes the schema version, event ID, per-bot sequence, timestamp, owner/bot UUIDs, source,
snapshot and structured event payload. Unknown future event types remain readable through `type()`.

## Supported operations

- Health, active count, complete bot listing and lookup by owner or bot UUID.
- Complete combat-mode catalog, capabilities and per-difficulty profiles.
- Spawn for `SINGLE`, `EVENT`, `ALLY` and `TEAM_ALLY` bots.
- Removal by owner/bot UUID, creation source or all bots.
- Runtime updates for totems, follow, combat, difficulty, armor and equipment slots.
- Runtime updates for target mode, explicit targets, team owners and automatic targeting.
- Runtime updates for explosive combat, healing, idle movement and owner persistence.
- Combat-mode selection, custom tuning and tuning reset.
- Custom kill-message update and disable operations.
- Reconnecting EventBus subscriptions.
