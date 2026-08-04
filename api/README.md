# UltimateBot API Module

## Custom bot UUID and equipment slots

Spawn requests can optionally assign a stable bot UUID and keep individual equipment slots fixed to an item or
empty. When `botUUID(...)` is omitted, UltimateBot continues to generate a random UUID.

```java
BotSpawnRequest request = BotSpawnRequest.builder(BotMode.SINGLE)
        .owner(owner.getUniqueId())
        .botUUID(UUID.fromString("4ed787a3-1f40-45a7-bb8f-13f987420001"))
        .emptyEquipmentSlot(BotEquipmentSlot.MAIN_HAND)
        .emptyEquipmentSlot(BotEquipmentSlot.OFF_HAND)
        .equipmentItem(BotEquipmentSlot.HEAD, new ItemStack(Material.DIAMOND_HELMET))
        .settings(settings)
        .build();
```

Use `defaultEquipmentSlot(...)` or `BotEquipmentSlotSetting.defaultSlot()` to return a runtime slot to normal AI
management. Custom UUIDs are rejected when already assigned to an active bot, player, or loaded entity.

## Overview
`api` is the public integration contract for UltimateBot.

It exposes:
- the global API entrypoint
- the readiness event
- the mutable bot management interface
- the read-only registry interface
- the shared model types used to spawn, inspect and update bots
- a typed Bukkit-backed EventBus and cancellable runtime events

This module intentionally contains no GUI logic, no runtime AI implementation and no NMS code. It is the boundary that third-party plugins should depend on.

Canonical Java-only domain behavior is supplied transitively by `common`. The API keeps its established public type names for binary compatibility and maps them to the shared contracts.

Public packages are null-marked with JSpecify. Parameters and return values are non-null
unless explicitly annotated with `@Nullable`; invalid required arguments fail fast at the
API boundary.

## What This Module Provides
### Entry Point
- `UltimateBotAPI`

### Events and EventBus
- `UltimateBotReadyEvent`
- lifecycle: spawn, despawn and death
- combat: attack, damage, kill, explosion and totem use
- state: target and runtime setting changes
- actions: heal and teleport
- `UltimateBotAPI.getEventBus()` for functional subscriptions

Event contracts are organized by responsibility instead of sharing one flat package:

| Package | Contents |
| --- | --- |
| `api.event.base` | `BotEvent`, source metadata and shared contracts |
| `api.event.bus` | event bus and subscription types |
| `api.event.lifecycle` | readiness, spawn, despawn and death |
| `api.event.combat` | attacks, damage, kills, explosions and totems |
| `api.event.state` | target and setting changes |
| `api.event.action` | healing and teleportation |

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
- `BotArmorTier`
- `DifficultyTier`
- `BotSnapshot`
- `BotSource`
- `BotOperationResult`

## Dependency Usage
Local multi-module usage:

```gradle
dependencies {
    compileOnly project(":api")
}
```

If your environment publishes the artifact externally, the effective coordinates are expected to follow this module group and version:

```gradle
dependencies {
    compileOnly "com.monkey.ultimatebot:api:<version>"
}
```

Use `api` for in-server Paper plugins that directly integrate with the loaded UltimateBot plugin.
If you need an includable dependency, use the separate Java-only remote SDK:

```gradle
dependencies {
    implementation "com.monkey.ultimatebot:sdk:<version>"
}
```

Suggested plugin declaration:
- use `depend: [UltimateBot]` if your plugin cannot function without UltimateBot
- use `softdepend: [UltimateBot]` if the integration is optional and you can work without it

## API Lifecycle
The core plugin registers the singleton during startup and then fires `UltimateBotReadyEvent`.

Third-party plugins should:
1. check `UltimateBotAPI.isAvailable()` during startup
2. listen for `UltimateBotReadyEvent` to catch late availability
3. use `UltimateBotAPI.get()` only after availability is confirmed

Do not instantiate `UltimateBotAPI` yourself. That constructor is for the core integration layer.

## Main Services
| Type | Responsibility |
| --- | --- |
| `UltimateBotAPI` | global singleton entrypoint that exposes the manager and registry |
| `IBotManager` | create, update, remove and resolve bots |
| `IBotRegistry` | inspect active bots through immutable snapshots |
| `BotEventBus` | subscribe to the same events delivered through Bukkit `@EventHandler` |

## EventBus

Every bot event extends `BotEvent` and provides an event ID, a per-bot sequence,
timestamp, owner UUID, bot UUID, source and immutable snapshot. Developers may use
normal Bukkit listeners or the functional EventBus; both receive the same event instance.

```java
import com.monkey.ultimatebot.api.event.bus.BotEventSubscription;
import com.monkey.ultimatebot.api.event.combat.BotExplosionEvent;

BotEventSubscription subscription = UltimateBotAPI.get().getEventBus().subscribe(
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
- `updateDifficulty(UUID ownerUUID, DifficultyTier difficulty)`

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
- difficulty default and allowed range

Important validation rules:
- `combat=true` requires `follow=true`
- default armor must stay inside the configured armor range
- default totem count must stay inside the configured totem range
- default difficulty must stay inside the configured difficulty range
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
- current difficulty
- min and max difficulty
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
- difficulty changes fail if the requested difficulty is outside the configured min/max window

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
import com.monkey.ultimatebot.api.UltimateBotAPI;
import com.monkey.ultimatebot.api.event.lifecycle.UltimateBotReadyEvent;
import com.monkey.ultimatebot.api.model.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;
import java.util.UUID;

public final class ExamplePlugin extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);

        if (UltimateBotAPI.isAvailable()) {
            bootstrap(UltimateBotAPI.get());
        }
    }

    @EventHandler
    public void onUltimateBotReady(UltimateBotReadyEvent event) {
        bootstrap(event.getApi());
    }

    private void bootstrap(UltimateBotAPI api) {
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
                .armorValue(BotArmorTier.DIAMOND, BotArmorTier.NETHERITE)
                .armor(BotArmorTier.NETHERITE)
                .setChangeableArmor(true)
                .totemValue(0, 32)
                .totemCount(16)
                .setChangeableTotem(true)
                .difficultyValue(DifficultyTier.NORMAL, DifficultyTier.GOD)
                .difficulty(DifficultyTier.HARD)
                .setChangeableDifficulty(true)
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
            getLogger().warning("UltimateBot spawn failed: " + result.message());
        }
    }
}
```

## Best Practices
- Prefer `UltimateBotReadyEvent` over assuming the API is already available
- Treat `UltimateBotAPI.register(...)` and `unregister()` as core-only lifecycle operations
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
Depend on `api` when you want a stable way to:
- spawn bots
- inspect active bots
- update runtime options
- resolve owners and players
- cleanly integrate your own plugin with UltimateBot

Do not depend on `core` unless you are modifying the implementation itself.
