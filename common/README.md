# MinecraftBot Common

`common` is the platform-independent foundation shared by the plugin runtime, public API, remote SDK and optional addons.

It owns:

- canonical bot domain values under `common.model`
- dependency-free value utilities under `common.util`
- cross-module service-provider contracts under feature packages such as `common.metrics`

Production code in this module may depend only on the Java standard library and JSpecify annotations. Bukkit, Paper, Mojang, NMS and implementation-specific libraries are forbidden and enforced by `CommonArchitectureTest`.

Published API and SDK types may retain compatibility façades in their original packages, but shared behavior and canonical values belong here.
