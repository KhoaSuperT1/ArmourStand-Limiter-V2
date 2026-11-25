<div align="center">

<img src=".github/logo.jpg" alt="Java-Cookieee" width="180" />

# ArmorStand-Limiter V2

**Keep armor stand lag machines and leftover custom stands from dragging down TPS.**

[![Minecraft](https://img.shields.io/badge/Minecraft-1.21--1.26.3-16a34a?style=flat-square)](https://papermc.io)
[![Java](https://img.shields.io/badge/Java-21%20%7C%2025-ea580c?style=flat-square)](https://adoptium.net)
[![Platforms](https://img.shields.io/badge/Platform-Paper%20%7C%20Spigot%20%7C%20Folia-d97706?style=flat-square)](https://papermc.io/software/folia)
[![Version](https://img.shields.io/badge/Version-2.1.0-334155?style=flat-square)](https://github.com/KhoaSuperT1/ArmourStand-Limiter-V2)

[Original Project (xSavior_of_God)](https://github.com/xSavior-of-God/ArmorStand-Limiter) · [Support Discord](https://discord.gg/NZycRMWrSS)

</div>

---

> [!NOTE]
> **Origin & AI Notice:**
> This repository is an updated V2 recode based on [ArmorStand-Limiter by xSavior_of_God](https://github.com/xSavior-of-God/ArmorStand-Limiter). All credit for the original plugin and idea goes to the original author.
> Roughly **80%** of this V2 update was written with AI assistance. It compiles cleanly against Paper/Folia `1.21.4` (Java 21) and `26.3` (Java 25), so please test it on a staging server before deploying to production.

## Why V2?

The original v2.0 plugin relied on NMS reflection for TPS checks, off-thread chunk entity scans, and internal `PaperPluginManagerImpl` / `CommandDispatcher` reflection for `/asl reload`, all of which broke across newer 1.21+ and 26.x server builds.

**v2.1.0** keeps the original config format and features while cleaning up the internals:

- Targets **Minecraft 1.21 – 1.26.3** from a single Java 21 JAR (verified against both 1.21.x on Java 21 and 26.3 on Java 25).
- Replaces the reflection-based `/asl reload` with a clean task, hook, notification, and config reload.
- Reads TPS through Paper's native `Server#getTPS()` API first, falling back gracefully instead of spamming stack traces every tick.
- Runs Paper/Spigot chunk scanning on the main thread and switches Folia region counters/schedulers to `AtomicInteger`, `AtomicBoolean`, and millisecond scheduling.
- Fixes a placement-check bug where counting stands in a chunk cleared marker and gravity flags on existing custom armor stands.

## Features

- **Block & Chunk Caps:** Periodically scan loaded chunks (or trigger on low TPS on non-Folia servers) to clear excess armor stands per block or per chunk.
- **Filter Whitelist:** Protect named stands, specific name keywords, invulnerable stands, invisible stands, small stands, stands with arms, stands without base plates, or stands wearing armor.
- **Plugin Hooks:** Optional protection for **HolographicDisplays** and **ModelEngine** entities without hard dependencies.
- **Lag-Machine Prevention:** Limit per-chunk player placement, block dispenser spawning, disable gravity/water movement above a chunk threshold (with fall-block tracking on Paper), and block piston pushes near dense armor stands.
- **Alerts:** Send removal logs to console, staff in-game, Discord webhooks, or Telegram bots.
- **Folia Ready:** Native global, region, and async scheduler support (`folia-supported: true`).

## Commands & Permissions

| Command | Description | Permission |
| :--- | :--- | :--- |
| `/asl help` | Show command usage | `armorstandlimiter.help` |
| `/asl reload` | Reload config, hooks, notifications, and tasks | `armorstandlimiter.reload` |
| `/asl check [chunk/xyz]` | Count total and removable stands in your chunk or block | `armorstandlimiter.check` |
| `/asl clear [chunk/xyz]` | Clear detected stands in your chunk or block | `armorstandlimiter.clear` |
| `/asl test` | Trigger a test notification across enabled channels | `armorstandlimiter.test` |

Extra nodes (all default to `op`; legacy `armostandlimiter.*` typos are also accepted):

- `armorstandlimiter.bypass` — Bypass chunk placement limits
- `armorstandlimiter.notify` — Receive in-game removal alerts

## Building from Source

Requires **Java 21+** (Java 25 for the `verify-26.3` profile) and **Apache Maven 3.9+**.

```bash
git clone https://github.com/KhoaSuperT1/ArmourStand-Limiter-V2.git
cd ArmourStand-Limiter-V2
mvn package
```

Compiled artifact: `target/ArmorStand-Limiter-v2.1.0.jar`

## Support

- **Discord:** https://discord.gg/NZycRMWrSS

---

<div align="center"><sub>Made with love and care by <b>Java-Cookieee</b> &amp; AI.</sub></div>
