# MinecraftBot API Module

## Overview
`mcbot-api` is the public integration contract for MinecraftBot.

It exposes:
- the global API entrypoint
- the readiness event
- the mutable bot management interface
- the read-only registry interface
- the shared model types used to spawn, inspect and update bots
- a typed Bukkit-backed EventBus and cancellable runtime events

This module intentionally contains no GUI logic, no runtime AI implementation and no NMS code. It is the boundary that third-party plugins should depend on.

## What This Module Provides
### Entry Point
- `MinecraftBotAPI`

### Events and EventBus
- `MinecraftBotReadyEvent`
- lifecycle: spawn, despawn and death
- combat: target, attack, damage, kill, explosion and totem use
- actions: heal and teleport
- cancellable runtime setting changes
- `MinecraftBotAPI.getEventBus()` for functional subscriptions

### Service Interfaces
- `IBotManager`
- `IBotRegistry`

### Public Model Types
- `BotMode`
- `BotSpawnRequest`
- `BotSettings`
- `BotSkin`
- `BotSkinSource`
- `BotBlastProtection`
- `BotArmorType`
- `BotRank`
- `BotSnapshot`
- `BotSource`
- `BotOperationResult`

## Dependency Usage
Local multi-module usage:

```gradle
dependencies {
    compileOnly project(":mcbot-api")
}
```

If your environment publishes the artifact externally, the effective coordinates are expected to follow this module group and version:

```gradle
dependencies {
    compileOnly "com.monkey.mcbot:mcbot-api:<version>"
}
```

Use `mcbot-api` for in-server Paper plugins that directly integrate with the loaded MinecraftBot plugin.
If you need an includable dependency, use the separate Java-only remote SDK:

```gradle
dependencies {
    implementation "com.monkey.mcbot:mcbot-sdk:<version>"
}
```

Suggested plugin declaration:
- use `depend: [MinecraftBot]` if your plugin cannot function without MinecraftBot
- use `softdepend: [MinecraftBot]` if the integration is optional and you can work without it

## API Lifecycle
The core plugin registers the singleton during startup and then fires `MinecraftBotReadyEvent`.

Third-party plugins should:
1. check `MinecraftBotAPI.isAvailable()` during startup
2. listen for `MinecraftBotReadyEvent` to catch late availability
3. use `MinecraftBotAPI.get()` only after availability is confirmed

Do not instantiate `MinecraftBotAPI` yourself. That constructor is for the core integration layer.

## Main Services
| Type | Responsibility |
| --- | --- |
| `MinecraftBotAPI` | global singleton entrypoint that exposes the manager and registry |
| `IBotManager` | create, update, remove and resolve bots |
| `IBotRegistry` | inspect active bots through immutable snapshots |
| `BotEventBus` | subscribe to the same events delivered through Bukkit `@EventHandler` |

## EventBus

Every bot event extends `BotEvent` and provides an event ID, a per-bot sequence,
timestamp, owner UUID, bot UUID, source and immutable snapshot. Developers may use
normal Bukkit listeners or the functional EventBus; both receive the same event instance.

```java
BotEventSubscription subscription = MinecraftBotAPI.get().getEventBus().subscribe(
        this,
        BotExplosionEvent.class,
        event -> event.setBlockDamage(true)
);
```

Close the returned subscription when it is no longer needed. Bukkit also removes its
listener automatically when the owning plugin is disabled.

Available runtime events include `BotSpawnPrepareEvent`, `BotSpawnEvent`,
`BotDespawnPrepareEvent`, `BotDespawnEvent`, `BotDeathEvent`,
`BotTargetChangeEvent`, `BotSettingsChangeEvent`, `BotAttackEvent`, `BotDamageEvent`,
`BotKillEntityEvent`, `BotExplosionEvent`, `BotTotemUseEvent`, `BotHealEvent` and
`BotTeleportEvent`. Cancellable events can modify their safe mutable properties before
the underlying action continues.

## `IBotManager` at a Glance
### Lookup and Parsing
- `isBotSpawned(UUID ownerUUID)`
- `getBot(UUID ownerUUID)`
- `getTeamAllyBot(UUID teamOwnerUUID)`
- `findTeamAllyPrimaryOwner(UUID teamOwnerUUID)`
- `parsePlayerReference(String playerReference)`
- `parsePlayerReferences(Collection<String> playerReferences)`

### Spawning
- `spawn(BotSpawnRequest request)`
- `spawnByReferences(BotMode mode, String ownerReference, Collection<String> targetReferences, Collection<String> teamOwnerReferences, BotSettings settings)`

### Runtime Updates
- `updateTotems(UUID ownerUUID, int totemCount)`
- `updateFollow(UUID ownerUUID, boolean follow)`
- `updateCombat(UUID ownerUUID, boolean combat)`
- `updateBlastProtection(UUID ownerUUID, boolean blastProtection)`
- `updateRank(UUID ownerUUID, BotRank rank)`

### Removal
- `remove(UUID ownerUUID)`
- `removeBySource(BotSource source)`
- `removeAll()`
- `despawn(UUID ownerUUID)`
- `despawnAll()`
- `getActiveBotCount()`

## `IBotRegistry` at a Glance
- `getAllBots()`
- `getBot(UUID ownerUUID)`
- `getBotUUID(UUID ownerUUID)`
- `isBotSpawned(UUID ownerUUID)`
- `size()`

Use `IBotRegistry` when you only need monitoring or status inspection. Use `IBotManager` for mutation.

## Bot Modes
| Mode | Meaning |
| --- | --- |
| `SINGLE` | personal bot for one owner; target is forced to the owner |
| `EVENT` | API-manageable event bot mode; ownerless API spawns can coexist as independent bots |
| `ALLY` | defensive ally for one owner |
| `TEAM_ALLY` | shared ally bot for multiple owners |

## Spawn Workflow
Typical spawn flow:
1. build a validated `BotSettings`
2. build a `BotSpawnRequest`
3. call `IBotManager.spawn(...)`
4. inspect the returned `BotOperationResult`

## `BotSettings`
`BotSettings` is intentionally strict. It uses a step-builder so integrations provide the full configuration in a defined order.

The payload controls:
- follow default
- combat default
- blast protection profile
- runtime mutability flags
- bot name template
- skin source
- armor default and allowed range
- totem default and allowed range
- rank default and allowed range

Important validation rules:
- `combat=true` requires `follow=true`
- default armor must stay inside the configured armor range
- default totem count must stay inside the configured totem range
- default rank must stay inside the configured rank range
- `combat` cannot be changeable when follow is permanently fixed to `false`
- follow cannot be changeable while combat is permanently fixed to `true`

## Name Template Tokens
The current core implementation resolves these tokens inside `BotSettings.setBotNameTemplate(...)`:
- `%player%`
- `%owner%`
- `%owner_name%`
- `%target%`
- `%first_owner%`
- `%owners_count%`
- `%mode%`

If the runtime server has PlaceholderAPI available, the core also applies PlaceholderAPI resolution before final profile-name sanitization.

## Skins
`BotSkin` supports these sources:
- `RANDOM`
- `OWNER`
- `FIRST_TEAM_OWNER`
- `PLAYER_REFERENCE`
- `TEXTURE_VALUE`
- `TEXTURE_URL`

Convenience factories:
- `BotSkin.random()`
- `BotSkin.owner()`
- `BotSkin.firstTeamOwner()`
- `BotSkin.player(String playerReference)`
- `BotSkin.texture(String textureValue)`
- `BotSkin.texture(String textureValue, String textureSignature)`
- `BotSkin.url(String textureUrl)`

Validation notes:
- `FIRST_TEAM_OWNER` is valid only for `TEAM_ALLY`
- `PLAYER_REFERENCE` requires a valid online player reference
- `TEXTURE_URL` must use `http` or `https`

## Blast Protection
`BotBlastProtection` represents a per-armor-piece profile:
- feet
- legs
- chest
- head

Convenience factories:
- `BotBlastProtection.all(boolean enabled)`
- `BotBlastProtection.of(boolean boots, boolean leggings, boolean chestplate, boolean helmet)`
- `BotBlastProtection.of(int boots, int leggings, int chestplate, int helmet)`

The integer variant accepts only `0` and `1`.

## `BotSpawnRequest`
`BotSpawnRequest` combines:
- `BotMode`
- primary owner UUID
- target UUID set
- team-owner UUID set
- validated `BotSettings`

Builder entry point:

```java
BotSpawnRequest.builder(BotMode.SINGLE)
```

Validation rules enforced by the request builder and the current core implementation:
- `SINGLE` and `ALLY` require a primary owner UUID
- `EVENT` can omit owner UUID; the returned snapshot owner UUID is the generated management key for future updates/removal
- `TEAM_ALLY` requires at least one owner in the request and at least two team owners at effective spawn time
- `settings` is mandatory
- `SINGLE` forces the target set to the owner
- an online primary owner must be resolvable at spawn time

## Result and Snapshot Model
### `BotOperationResult`
Use it to inspect:
- `success`
- `message`
- optional `snapshot`

Convenience helpers:
- `BotOperationResult.success(...)`
- `BotOperationResult.failure(...)`
- `snapshotOptional()`

### `BotSnapshot`
Exposes a read-only view of:
- owner UUID
- bot UUID
- bot type
- current rank
- min and max rank
- follow state
- combat state
- current and allowed totem counts
- current target
- full target UUID set
- creation source (`CORE` or `API`)
- auto target, WorldGuard PvP respect, idle wander, Crystal PvP, explosions, Ender Pearls and kill-message state

`BotSnapshot.hasTarget()` is a convenience check for target presence.

## Runtime Update Semantics
The current core implementation returns `false` from update methods when:
- the bot does not exist
- the requested field is locked by configuration
- the requested value is outside allowed bounds
- the requested state would break runtime rules

Use `BotSettings.explosions(false)` or `IBotManager.updateExplosions(ownerUUID, false)` to disable all bot-driven explosive combat. This also disables Crystal PvP and Respawn Anchor PvP and clears the explosive inventory slots.

Explosion terrain damage is independent from explosion entity damage. By default, bot crystals and respawn anchors keep their explosion, damage and knockback but preserve blocks. Use `BotSettings.explosionBlockDamage(true)`, `IBotManager.updateExplosionBlockDamage(ownerUUID, true)` or `BotExplosionEvent.setBlockDamage(true)` when bot explosions should also destroy terrain. Cancelling `BotExplosionEvent` cancels the full explosion.

Examples:
- enabling combat fails if follow is currently disabled
- setting an out-of-range totem count fails
- rank changes fail if the requested rank is outside the configured min/max window

Note:
- `updateBlastProtection(...)` is a global toggle at API level; it does not expose per-slot blast edits

## Online Reference Resolution
`parsePlayerReference(...)` and `spawnByReferences(...)` are convenience helpers for command-like integrations.

The current core implementation resolves:
- exact online player name
- online player UUID string
- Bukkit fuzzy player lookup

These helpers are online-player oriented. They should not be treated as offline-account resolvers.

## Example Integration
```java
import com.monkey.mcbot.api.MinecraftBotAPI;
import com.monkey.mcbot.api.event.MinecraftBotReadyEvent;
import com.monkey.mcbot.api.model.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;
import java.util.UUID;

public final class ExamplePlugin extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);

        if (MinecraftBotAPI.isAvailable()) {
            bootstrap(MinecraftBotAPI.get());
        }
    }

    @EventHandler
    public void onMinecraftBotReady(MinecraftBotReadyEvent event) {
        bootstrap(event.getApi());
    }

    private void bootstrap(MinecraftBotAPI api) {
        UUID owner = /* resolve online owner UUID */;

        BotSettings settings = BotSettings.builder()
                .setBotNameTemplate("Practice_%owner%")
                .setBotSkinOwner()
                .follow(true)
                .setChangeableFollow(true)
                .combat(true)
                .setChangeableCombat(true)
                .blastProtection(false, false, false, false)
                .setChangeableBlast(true)
                .armorValue(BotArmorType.DIAMOND, BotArmorType.NETHERITE)
                .armor(BotArmorType.NETHERITE)
                .setChangeableArmor(true)
                .totemValue(0, 32)
                .totemCount(16)
                .setChangeableTotem(true)
                .rankValue(BotRank.NORMAL, BotRank.GOD)
                .rank(BotRank.HARD)
                .setChangeableRank(true)
                .explosions(false)
                .crystalPvp(false)
                .build();

        BotSpawnRequest request = BotSpawnRequest.builder(BotMode.ALLY)
                .owner(owner)
                .targets(Set.of())
                .settings(settings)
                .build();

        BotOperationResult result = api.getBotManager().spawn(request);
        if (!result.success()) {
            getLogger().warning("MinecraftBot spawn failed: " + result.message());
        }
    }
}
```

## Best Practices
- Prefer `MinecraftBotReadyEvent` over assuming the API is already available
- Treat `MinecraftBotAPI.register(...)` and `unregister()` as core-only lifecycle operations
- Use `IBotRegistry` for read-only dashboards, placeholders and monitoring integrations
- Use `BotSource` and `removeBySource(...)` if your plugin needs to clean up only its own API-created bots
- Use `removeAll()` when your plugin needs to clear every active bot and know how many were removed
- Keep your integration on the server thread

The last point is an implementation-based recommendation: the current core spawn and update paths interact directly with Bukkit and NMS runtime objects, so main-thread usage is the safe default.

## Compatibility
- Compiles against Paper API 1.21.4
- Intended to be consumed together with the runtime core that currently supports Paper 1.21.4 through 1.21.11
- Java 21 should be treated as the project baseline because the repository is compiled with `--release 21`

## Summary
Depend on `mcbot-api` when you want a stable way to:
- spawn bots
- inspect active bots
- update runtime options
- resolve owners and players
- cleanly integrate your own plugin with MinecraftBot

Do not depend on `mcbot-core` unless you are modifying the implementation itself.
