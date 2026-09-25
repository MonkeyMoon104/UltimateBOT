# UltimateBot

Advanced, standalone **PvP training bots** for Minecraft servers.

[![GitHub Repo stars](https://img.shields.io/github/stars/MonkeyMoon104/UltimateBOT?style=flat&logo=github&label=Star)](https://github.com/MonkeyMoon104/UltimateBOT)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

UltimateBot spawns NMS-backed fake players with combat AI, GUI configuration, optional integrations, and a public API for third-party plugins. It is designed for Paper (and Folia-compatible) environments across a wide Minecraft version range.

---

## Table of contents

- [Overview](#overview)
- [Features](#features)
- [Supported versions](#supported-versions)
- [Requirements](#requirements)
- [Installation](#installation)
- [Commands and permissions](#commands-and-permissions)
- [Configuration](#configuration)
- [Building from source](#building-from-source)
- [Project structure](#project-structure)
- [Developer API](#developer-api)
- [Optional integrations](#optional-integrations)
- [Disclaimer and limitation of liability](#disclaimer-and-limitation-of-liability)
- [Security and NMS notice](#security-and-nms-notice)
- [Contributing](#contributing)
- [Support](#support)
- [Authors](#authors)
- [License](#license)

---

## Overview

UltimateBot is a multi-module Minecraft plugin that provides realistic training opponents for PvP practice, events, and ally/team scenarios.

Bots are implemented as **version-specific NMS fake players**, not simple ArmorStand placeholders. Combat behavior is driven by a dedicated AI stack (movement, aim, attack, healing, totems, pearls, crystal/anchor logic where the platform supports it).

The production artifact is a single shaded jar:

```text
dist/build/libs/UltimateBot.jar
```

---

## Features

- **Four bot modes:** `SINGLE`, `EVENT`, `ALLY`, `TEAM_ALLY`
- **NMS fake-player bots** with per-version bridges
- **GUI-driven setup** for equipment, combat profile, difficulty, follow, and related options
- **Difficulty scaling** from easy practice up to high-pressure profiles
- **Combat AI** including aggressive/defensive repositioning and recovery behavior
- **Crystal / respawn-anchor combat** on versions that support those mechanics
- **Healing, totem, and ender pearl** logic
- **World protection helpers** for bot-placed combat blocks/entities
- **PlaceholderAPI** placeholders for live bot telemetry
- **Public Java API** for spawn/despawn/update and typed runtime events
- **Optional Remote API + SDK** for out-of-process control
- **Optional addons** (metrics/Prometheus, guard compatibility layer)
- **Folia-supported** plugin metadata

---

## Supported versions

UltimateBot ships dedicated NMS modules for a large Minecraft span, including legacy and modern Paper mappings, for example:

| Era | Examples |
| --- | --- |
| Legacy | 1.7.10 (`v1_7_R4`), 1.8.x, 1.9–1.12 |
| Transitional | 1.13–1.16.x |
| Modern | 1.17–1.21.x |
| Newest tracked | `v26_1`, `v26_2`, `v26_3` |

Exact server revision support depends on the matching NMS bridge included in the built jar. Always use a build that contains the bridge for your server version.

> Not every combat feature exists on every Minecraft version. Capabilities that require newer game mechanics are gated by platform support.

---

## Requirements

- A **Paper** (or Paper-compatible) server for the target Minecraft version  
  Folia is declared as supported; validate on your Folia build before production use.
- **Java** compatible with your server runtime (modern Paper builds typically require a current LTS/JDK as documented by Paper)
- Optional soft-dependencies:
  - [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/)
  - [LuckPerms](https://luckperms.net/)
  - [WorldGuard](https://enginehub.org/worldguard/)

---

## Installation

1. Download or build `UltimateBot.jar`.
2. Place it in your server `plugins/` folder.
3. Start the server once to generate default files under `plugins/UltimateBot/`.
4. Edit `config.yml` and language files as needed.
5. Restart or reload according to your operational policy (full restart recommended after addon toggles).

### Quick operator usage

| Command | Purpose |
| --- | --- |
| `/bot` | Open the normal bot settings GUI |
| `/botevent` | Open the event bot settings GUI |
| `/botally` | Open the ally bot settings GUI |
| `/botteamally` | Create and manage a team ally bot |
| `/ultimatebotreload <config\|bot\|maps\|all>` | Reload configuration or clear bots/maps |

---

## Commands and permissions

| Permission | Default | Description |
| --- | --- | --- |
| `ultimatebot.bot.use` | `true` | Use `/bot`, `/botally`, `/botteamally` |
| `admin.host.bot` | `op` | Use `/botevent` |
| `ultimatebot.admin.use` | `op` | Use `/ultimatebotreload` |

---

## Configuration

Primary files are created in `plugins/UltimateBot/`:

- `config.yml` — language, updates, remote API, addons, caches, world protection, bot defaults (auto-updated via BoostedYAML using `config-version`)
- `combat-modes.yml` — combat profile definitions
- Language files (for example `EN.yml`)

Important defaults include:

- blocked worlds for bots
- explosion / combat-block protection limits
- remote API bind address and token (keep private)
- optional metrics and guard addon enablement
- `config-version` (managed by BoostedYAML; new keys merge in on startup/reload)
- `vanilla-statistics.kills` / `vanilla-statistics.deaths` (default `true`; set both to `false` in practice worlds to avoid K/D farming)

Treat `remote-api.token` as a secret. Prefer binding the remote API to `127.0.0.1` unless it is protected by network controls.

---

## Building from source

### Prerequisites

- JDK suitable for the Gradle toolchain used by this repository
- Git
- Network access on first plugin start (runtime libraries are downloaded into `plugins/UltimateBot/libs/`)

### Build the plugin jar

```bash
./gradlew :dist:build
```

On Windows:

```powershell
.\gradlew :dist:build
```

Output:

```text
dist/build/libs/UltimateBot.jar
```

### Runtime libraries

Third-party libraries (Jackson, Lamp, Configurate, Pathetic, bStats, Caffeine, InvUI) are **not** shaded into the main jar. On `onLoad`, UltimateBot:

1. reuses relocated classes already on the classpath when possible;
2. otherwise **installs from local cache** under `plugins/UltimateBot/libs/{legacy|modern}/` (relocated jars, or original jars that get relocated without network);
3. only if locals are missing, downloads from the configured HTTPS repositories (vendor repo first, then Central mirrors / other public repos), verifies SHA-256, caches the original, relocates, then injects.

Force offline install-only with `-Dultimatebot.libs.offline=true` (no remote fetch; requires jars already present under `libs/`).

Download order: vendor repo when known (e.g. xenondevs for InvUI), then your mirror (`https://repo.monkeymoon104.it/releases`, override at build with `ULTIMATEBOT_LIBS_MIRROR`), then Central mirrors / other public repos. HTTP timeouts are short (5s connect / 10s read) so a dead host fails over quickly.

Only `jar-relocator` (+ ASM) stay shaded in `UltimateBot.jar` (needed before other libs load). Descriptors embed **multiple HTTPS candidate URLs** per jar, plus size and SHA-256 of the original artifact, under `META-INF/ultimatebot/libs/` (key-class names are relocated). Runtime tries URLs in order until download + integrity check succeed.

| Original package | Relocated under |
| --- | --- |
| `com.fasterxml.jackson` | `com.monkey.ultimatebot.libs.jackson` |
| `revxrsal.commands` | `com.monkey.ultimatebot.libs.lamp` |
| `org.spongepowered.configurate` | `com.monkey.ultimatebot.libs.configurate` |
| `xyz.xenondevs.invui` | `com.monkey.ultimatebot.libs.invui` |
| … | `com.monkey.ultimatebot.libs.<lib>` |

### Maintaining the Maven libs mirror

Mirror sync tasks (`bumpLibsToServer`, `verifyLibsMirror`) live in a **local, gitignored** build-logic script (`ultimatebot.libs-mirror.gradle.kts`) and are applied by `:dist` only when that file is present. Credentials stay in `~/.gradle/gradle.properties`; nothing from that workflow is committed.

### Module notes

| Module | Role |
| --- | --- |
| `api` | Public integration contracts for other plugins |
| `sdk` | Remote HTTP client (no Bukkit dependency) |
| `common` | Shared Java-only domain contracts |
| `core` | Plugin runtime, GUI, AI orchestration, bootstrap |
| `NMS/*` | Version-specific fake-player bridges |
| `addons/*` | Optional metrics/guard runtimes |
| `dist` | Final shaded distribution jar |
| `examples/` | Sample addon integrations |

Deeper module documentation lives in each module’s own `README.md` where present.

---

## Project structure

```text
UltimateBot/
├── api/                 Public plugin API
├── sdk/                 Remote client SDK
├── common/              Shared contracts (no Bukkit/NMS)
├── core/                Main plugin implementation
├── NMS/                 Per-version NMS bridges
├── addons/              Optional addons (metrics, guard)
├── dist/                Shaded UltimateBot.jar assembly
├── examples/            Example integrations
├── docs/                Generated/reference docs
└── build-logic/         Shared Gradle conventions
```

---

## Developer API

Third-party plugins should depend on the **`api`** module, not on `core` internals or NMS packages.

Typical flow:

1. Soft-depend on `UltimateBot` in your `plugin.yml`.
2. Wait for `UltimateBotReadyEvent`, or obtain `UltimateBotAPI` after enable.
3. Spawn/update/despawn bots through `IBotManager` / `IBotRegistry`.
4. Subscribe to typed bot events via the EventBus when needed.

For out-of-process control, enable the Remote API and use the **`sdk`** module. See:

- [`api/README.md`](api/README.md)
- [`sdk/README.md`](sdk/README.md)
- [`examples/custom-ai-addon`](examples/custom-ai-addon)

---

## Optional integrations

| Integration | Purpose |
| --- | --- |
| PlaceholderAPI | Bot status placeholders |
| LuckPerms | Permission backend compatibility |
| WorldGuard | Respect region protection for bot placements when configured |
| Metrics addon | Optional Micrometer/Prometheus export |
| Guard addon | Optional Paper compatibility protection layer |

Addon jars are managed by the main plugin when enabled in `config.yml` and generally require a **full server restart** after enable/disable.

---

## Disclaimer and limitation of liability

**UltimateBot is provided “AS IS”, without warranty of any kind**, express or implied, including but not limited to warranties of merchantability, fitness for a particular purpose, and non-infringement.

### No responsibility for modified builds

The author(s) and copyright holder(s) of this project **accept no responsibility and assume no liability** for:

- **personal or third-party modifications** to this plugin, its dependencies, or its packaging;
- modifications intended to (or that result in) **crashing, destabilizing, or disabling** Minecraft servers;
- modifications that **harm, grief, wipe, corrupt, or otherwise destroy player data, inventories, worlds, backups, or economy state**;
- modifications that **alter, relocate, corrupt, or misuse sensitive NMS / CraftBukkit / Paper internals**, including fake-player, packet, entity, player-connection, or world code paths;
- any resulting **server crashes, client crashes, desyncs, rollbacks, bans, data loss, downtime, or security incidents**.

If you fork, patch, reobfuscate, inject into, or otherwise change UltimateBot or its NMS bridges, **you do so entirely at your own risk**. Support is not owed for unofficial or altered builds.

### Intended use

UltimateBot is intended for legitimate PvP training, events, and server features on systems you are authorized to operate. Misuse against networks, players, or infrastructure you do not own or administer is solely your responsibility.

### Operational responsibility

Server operators remain responsible for:

- validating builds on staging before production;
- maintaining backups;
- configuring protection plugins and world rules;
- restricting permissions and remote API access;
- monitoring resource usage when running many bots.

To the maximum extent permitted by applicable law, the author(s) shall not be liable for any direct, indirect, incidental, special, consequential, or punitive damages arising from use of this software, whether or not advised of the possibility of such damages.

---

## Security and NMS notice

UltimateBot interacts with **version-sensitive Minecraft server internals (NMS)**.

- Do **not** casually edit NMS bridge classes unless you understand Paper mappings for that exact revision.
- Incorrect changes to entity lifecycle, connection handling, inventory, damage, or packet paths can crash the **server**, disconnect or crash **clients**, corrupt runtime state, or break other plugins.
- Prefer the public **`api`** / **`sdk`** surfaces for integrations.
- Never commit or publish secrets such as remote API tokens.

---

## Contributing

Contributions that improve stability, version coverage, documentation, or public API clarity are welcome.

Before opening a pull request:

1. Reproduce the issue or describe the change clearly.
2. Keep diffs focused; avoid unrelated refactors.
3. Test on the Minecraft version(s) you claim to affect.
4. Do not include secrets, personal license keys, or proprietary server jars in the repository.

For API/SDK details, follow the module READMEs and existing examples.

---

## Support

If UltimateBot helps your server or project, the simplest way to support it is to
[★ star the repository on GitHub](https://github.com/MonkeyMoon104/UltimateBOT).

Stars help others discover the project and encourage continued maintenance — no account setup beyond GitHub, and it takes a second.

---

## Authors

- **MonkeyMoon104** — primary author

---

## License

This project is licensed under the [MIT License](LICENSE).

Copyright (c) 2026 MonkeyMoon104

Redistribution of modified NMS-bearing builds does not transfer any warranty or liability to the original author(s); see [Disclaimer and limitation of liability](#disclaimer-and-limitation-of-liability).

---

<p align="center">
  <sub>UltimateBot — PvP training bots powered by multi-version NMS fake players.</sub>
</p>
