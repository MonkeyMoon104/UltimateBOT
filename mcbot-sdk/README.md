# mcbot-sdk

`mcbot-sdk` is the Java-only remote client for MinecraftBot.

Use this artifact with `implementation` when you do not want to depend on Bukkit/Paper classloading or the in-server `mcbot-api` singleton.

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
        .explosions(false)
        .crystalPvp(false)
        .build());

UUID botId = spawn.snapshot().ownerUUID();
client.updateEnderPearls(botId, false);
client.remove(botId);
```

## Supported Operations

- `health()`
- `listBots()`
- `spawnEventBot(...)`
- `remove(UUID ownerUUID)`
- `removeAll()`
- `updateCrystalPvp(UUID ownerUUID, boolean enabled)`
- `updateExplosions(UUID ownerUUID, boolean enabled)`
- `updateEnderPearls(UUID ownerUUID, boolean enabled)`
