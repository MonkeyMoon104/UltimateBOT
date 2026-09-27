# UltimateBot Common

`common` is an **internal** runtime module. It is not published to MonkeyRepo.

Third-party plugins and remote clients must depend on `api` (and optionally `sdk`), never on `common`.

It owns:

- runtime library download / relocate / inject under `common.lib.*`
- hosted addon loader internals under `common.addon`
- cross-module service-provider contracts such as `common.metrics` and `common.guard`
- networking helpers under `common.net`

Public domain models live in `api` (`com.monkey.ultimatebot.api.model.*` and `api.util`). This module depends on `api` for those types.

Production code here may use the Java standard library, JSpecify, jar-relocator/ASM (library loader), and the `api` module. Bukkit, Paper, Mojang and NMS remain forbidden and are enforced by `CommonArchitectureTest`.
