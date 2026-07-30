# MinecraftBot Addons

This module groups optional runtimes that are intentionally kept outside the main plugin jar:

- `metrics` provides Micrometer and the Prometheus registry.
- `guard` provides the Paper-specific bot compatibility protection.

The main plugin embeds signed build metadata for every addon. At startup it downloads only enabled addons into
`plugins/MinecraftBot/addon`, verifies their exact size and SHA-256 digest, and loads them through isolated class
loaders. Addon state changes require a full server restart.
