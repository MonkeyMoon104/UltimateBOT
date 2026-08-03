# UltimateBot Core Module

## Overview
`core` is the runtime implementation module of the UltimateBot project.

It contains:
- the Bukkit/Paper plugin entrypoint
- bot lifecycle orchestration
- GUI workflows and commands
- PlaceholderAPI integration
- public API publication
- the shared NMS-facing abstractions used by the version bridges
- the combat AI stack that drives the training bots

This module is the implementation layer, not the final multi-version distribution by itself. The production jar is assembled by the `buildLogic` module, which shadows `core` together with the version-specific bridges under `NMS/`.

Platform-independent contracts shared by runtime modules live in `common`. That module contains only Java and JSpecify types and is protected by an architecture test that rejects Bukkit, Paper, Mojang and NMS dependencies.

## Key Capabilities
- Four runtime bot modes: `SINGLE`, `EVENT`, `ALLY`, `TEAM_ALLY`
- NMS-backed fake-player bots with version-specific bridge loading
- GUI-driven runtime configuration for armor, blast protection, totems, follow, combat and difficulty
- Difficulty-based behavior scaling from `EASY` to `GOD`
- Combat state machine with aggressive, defensive, repositioning, crystal-setup, anchor-setup and retreat logic
- Dedicated Crystal PvP and Respawn Anchor PvP controllers
- Auto-heal, totem management, enderpearl logic and safe recovery teleports
- Team-owner support and target filtering
- PlaceholderAPI expansion for live bot telemetry
- Public API publication through `UltimateBotAPI` and `UltimateBotReadyEvent`
- Structured startup diagnostics and version-aware NMS bootstrap

## Runtime Stack
- Java 21
- Paper 1.21.4 through 1.21.11
- Final packaged jar: `buildLogic/build/libs/UltimateBot.jar`
- External packet dependencies: none
- Optional integrations: PlaceholderAPI, LuckPerms and WorldGuard
- Optional observability runtime: downloaded automatically to `plugins/UltimateBot/addon/UltimateBot-Metrics.jar`

Deployment note:
- the current source also registers a CombatLogX `PlayerPreTagEvent` listener, so production deployments should keep a compatible CombatLogX runtime available, or guard/remove that hook in the release build

## Module Responsibilities
### Bootstrap
`com.monkey.ultimatebot.UltimateBot` is the plugin entrypoint. During `onEnable()` it:
- saves the default configuration
- resolves the active NMS bridge with `NMSBridgeManager`
- creates runtime services (`TargetingService`, `PlayerOptions`, `BotRegistry`, `BotManager`)
- wires the public API adapters
- registers commands and listeners
- registers PlaceholderAPI placeholders
- registers `UltimateBotAPI`
- fires `UltimateBotReadyEvent`

### Bot Lifecycle
Core bot management lives in:
- `BotRegistry`: active bot storage keyed by owner UUID
- `BotManager`: orchestration facade for spawn, update, lookup and despawn
- `BotSpawner`: fake-player creation, profile resolution, equipment application and registration
- `BotUpdater` and `BotLookup`: runtime mutation and lookup helpers

### Combat AI
The AI stack is composed by `BotAI`, which wires:
- movement
- rotation
- attack handling
- inventory control
- totem management
- healing
- teleport recovery
- enderpearl logic
- crystal PvP logic
- respawn anchor PvP logic
- combat-state evaluation
- combat strategy execution

### Operator Workflow
The player-facing interface is built with InvUI and exposed by `NewBotGUI`.

The GUI is organized into four tabs:
- `KitTab`
- `DifficultyTab`
- `OwnersTab`
- `TargetsTab`

Runtime actions available through GUI items include:
- cycling armor tier per slot
- toggling per-slot blast protection
- increasing or decreasing totems
- toggling follow
- toggling combat
- cycling difficulty
- spawning or despawning the managed bot
- teleporting the active bot back to the player position

### Platform Integration
Core integration points include:
- `BotPlaceholderCoordinator` for PlaceholderAPI
- `CoreBotManagerAdapter` and `CoreBotRegistryAdapter` for public API exposure
- `INMSBridge` and `NMSBridgeManager` for multi-version server support
- `INMSBridge` packet factories for native, version-specific client rendering

## Bot Modes
### `SINGLE`
- Personal training bot for one owner
- Target is forced to the owner UUID
- Opened by `/bot`

### `EVENT`
- Global event bot mode
- Actively targets the nearest player inside the configured event range
- Mutually exclusive with normal bot modes
- When spawned, current non-event bots are despawned
- Opened by `/botevent`

### `ALLY`
- Defensive companion for one owner
- Scans around the owner with `pre-range` and `range` thresholds
- Can operate in watch-only mode during pre-range detection
- Escalates to combat only when a threat enters the action range
- Teleports back near the owner if it drifts too far
- Opened by `/botally`

### `TEAM_ALLY`
- Shared ally bot for multiple owners
- Supports creation, owner addition and owner removal commands
- Protects the entire owner set and excludes owners from valid attack targets
- Shares `BotOptions` across the team
- Can use explicit target filters
- Opened by `/botteamally`

## Combat and Behavior Model
The core behavior is not a single attack loop. The source currently includes:
- combat states: `AGGRESSIVE`, `DEFENSIVE`, `REPOSITIONING`, `ANCHOR_SETUP`, `CRYSTAL_SETUP`, `RETREATING`
- end crystal placement, scoring and attack preparation
- obsidian search and placement helpers for crystal PvP
- respawn anchor placement, charging and explosion flow
- enderpearl strategies for aggression, repositioning, escape and anchor positioning
- auto-heal flow built around golden-apple style recovery behavior
- follow mode with non-combat chasing when combat is disabled
- safe recovery teleports for ally and team-ally bots

## Difficulty System
Available difficulties:
- `EASY`
- `NORMAL`
- `MEDIUM`
- `HARD`
- `GOD`

Difficulties are not cosmetic labels. They tune concrete combat parameters, including:
- crystal distance windows
- crystal and obsidian preparation timings
- attack cooldowns
- anchor search cadence
- prediction depth
- minimum safe distance
- scan intervals
- damage and placement thresholds

## Name, Skin and Target Resolution
### Name Templates
API-aware spawns can use dynamic name templates. The current core implementation resolves:
- `%player%`
- `%owner%`
- `%owner_name%`
- `%target%`
- `%first_owner%`
- `%owners_count%`
- `%mode%`

If PlaceholderAPI is available, the template is also passed through PlaceholderAPI before the final profile name is sanitized for Minecraft profile compatibility.

### Skin Sources
The runtime supports skin resolution from:
- owner skin
- first team owner skin
- random profile
- explicit online player reference
- raw texture value
- raw texture value with signature
- texture URL converted into a Mojang texture payload

### Targeting Rules
- `EVENT` bots use nearest-player targeting within `bot.event.range`
- `ALLY` and `TEAM_ALLY` bots search around owners using `pre-range` and `range`
- protected owners are excluded from valid combat targets
- explicit target filters can narrow the eligible player set for ally modes

## Commands
| Command | Purpose | Permission |
| --- | --- | --- |
| `/bot` | Open the normal bot GUI | `ultimatebot.bot.use` |
| `/botally` | Open the ally bot GUI | `ultimatebot.bot.use` |
| `/botevent` | Open the event bot GUI | `admin.host.bot` |
| `/botteamally` | Open the TEAM_ALLY GUI for an existing team member | `ultimatebot.bot.use` |
| `/botteamally create <owner1> <owner2> [owner3 ...]` | Prepare a shared TEAM_ALLY bot for multiple online owners | `ultimatebot.bot.use` |
| `/botteamally addowners <player1> [player2 ...]` | Add owners to an active TEAM_ALLY bot | `ultimatebot.bot.use` |
| `/botteamally removeowners <player1> [player2 ...]` | Remove owners from an active TEAM_ALLY bot | `ultimatebot.bot.use` |
| `/ultimatebotreload <config|bot|maps|all>` | Reload config, despawn bots, clear cached options, or do all three | `ultimatebot.admin.use` |

## Permissions
| Permission | Default | Description |
| --- | --- | --- |
| `ultimatebot.bot.use` | `true` | Access normal, ally and team-ally workflows |
| `admin.host.bot` | `op` | Access event bot workflow |
| `ultimatebot.admin.use` | `op` | Access maintenance and reload workflow |

## PlaceholderAPI
Identifier:
- `UltimateBot`

Format:
- `%UltimateBot_<key>%`

Registered placeholder keys:
- `armor`
- `combat`
- `combat_state`
- `distance`
- `follow`
- `healing`
- `health`
- `health_bar`
- `health_percentage`
- `location`
- `online_time`
- `difficulty`
- `status`
- `totems`

These placeholders expose live bot telemetry such as status, current difficulty, health, distance, online time and totem count for the player requesting the placeholder.

## Configuration Map
The default configuration lives in `src/main/resources/config.yml`.

### `messages.*`
Contains player-facing texts for:
- spawn and despawn feedback
- event kill broadcasts
- reload feedback
- validation and lock-state errors
- blocked-world handling
- ally and team-ally threat alerts

### `bot.*`
Contains runtime defaults and limits such as:
- `bot.name`
- `bot.default-totem-count`
- `bot.max-totem-normal`
- `bot.max-totem-event`
- `bot.blocked-worlds`

Mode-specific controls:
- `bot.event.range`
- `bot.ally.range`
- `bot.ally.pre-range`
- `bot.ally.return-teleport-distance`
- `bot.ally.return-teleport-cooldown-ms`
- `bot.team-ally.range`
- `bot.team-ally.pre-range`
- `bot.team-ally.return-teleport-distance`
- `bot.team-ally.return-teleport-cooldown-ms`

### `gui.*`
Contains the inventory UI configuration for:
- tab border and tab selectors
- owners and targets tab heads
- armor display and blast-state text
- follow, spawn, despawn, teleport, combat, totem and difficulty items

## Lifecycle and Cleanup Rules
The core cleans runtime state aggressively to avoid stale entities and stale owner mappings:
- active bots are re-synced to players after join
- owner bots are despawned on quit
- owner bots are despawned on world change
- non-event bots are despawned when the owner dies
- event bots block non-event spawns
- cached player options are in-memory only and are cleared on logout, reload or bot death
- TEAM_ALLY option caches are removed for all owners when the shared bot is removed

## Internal Package Map
| Package | Responsibility |
| --- | --- |
| `com.monkey.ultimatebot.bot` | bot lifecycle, options, registry, spawn/update lookup |
| `com.monkey.ultimatebot.bot.ai` | high-level AI composition |
| `com.monkey.ultimatebot.bot.ai.controllers.*` | specialized combat and utility controllers |
| `com.monkey.ultimatebot.commands` | commands and tab completion |
| `com.monkey.ultimatebot.gui` | InvUI-based GUI construction |
| `com.monkey.ultimatebot.integration.api` | adapters from runtime core to public API |
| `com.monkey.ultimatebot.listener` | join, quit, world, death and tag listeners |
| `com.monkey.ultimatebot.placeholders` | PlaceholderAPI integration |
| `com.monkey.ultimatebot.nms` | bridge abstraction and runtime bridge selection |
| `com.monkey.ultimatebot.logging` | startup diagnostics and structured logging |

## Build and Packaging
This repository is a multi-module build:
- `api` exposes the public contract
- `core` contains the runtime implementation
- `common` contains the platform-independent contracts and shared Java utilities
- `addons:metrics` produces the optional shaded Micrometer/Prometheus runtime
- `addons:guard` produces the lightweight Paper compatibility guard
- `NMS:*` provide version-specific NMS bridges
- `plugin` assembles the final distributable jar

Useful tasks:

```bash
./gradlew :core:build
./gradlew :buildLogic:shadowJar
```

Expected production artifact:
- `buildLogic/build/libs/UltimateBot.jar`

Optional addon artifacts:
- `addons/metrics/build/libs/UltimateBot-Metrics.jar`
- `addons/guard/build/libs/UltimateBot-Guard.jar`

The metrics addon is disabled by default through `addons.metrics.enabled`, while the guard addon is enabled by
default. On a full server restart,
UltimateBot creates the `addon` directory, downloads each enabled version-matched artifact from the release
repository, verifies its embedded size and SHA-256, and loads it through an isolated class loader. `/ultimatebotreload`
intentionally does not install, enable or disable addons.

## Extension and Integration
During startup, the core:
1. builds a `UltimateBotAPI` instance
2. registers it globally
3. fires `UltimateBotReadyEvent`

Third-party integrations should consume the public API from `api`, not internal core classes.

## Operational Notes
- This is a Paper-oriented, NMS-backed implementation module
- Bot rendering uses the native version-specific NMS bridges
- PlaceholderAPI is part of the declared plugin dependency model
- Player option state is runtime-only; the core does not persist it to a database
- If you need a stable external integration surface, depend on `api` instead of importing `core`
