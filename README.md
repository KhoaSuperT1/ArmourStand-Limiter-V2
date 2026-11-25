# ArmorStand-Limiter (Updated)

An updated, NMS-free recode of **ArmorStand-Limiter** for **Spigot, Paper, Purpur, Leaf, and Folia (Minecraft 1.21 – 1.26.3)**.

> **Original Code Credit:**
> Based on the original open-source plugin by **xSavior_of_God**:
> https://github.com/xSavior-of-God/ArmorStand-Limiter

## What Changed in v2.1.0

- **Minecraft 1.21 – 1.26.3 Support**: Compiled with Java 21 bytecode (`--release 21`) and verified against both 1.21.x (Java 21) and 26.3 (Java 25) Paper/Folia APIs.
- **Zero-NMS `/asl reload`**: Removed brittle `PaperPluginManagerImpl` / `CommandDispatcher` reflection in favor of a clean task, hook, notification, and config reload.
- **Safe TPS Monitoring**: Uses Paper's native `Server#getTPS()` API first, falling back safely without console spam if unavailable.
- **Thread-Safe Scanning**: Fixed async chunk/entity scanning on Paper/Spigot and updated Folia region counters/schedulers (`AtomicBoolean`, `AtomicInteger`, millisecond scheduling).
- **Safe Placement Limiting**: Fixed chunk placement counting so counting existing stands no longer mutates marker/gravity flags on custom armor stands.

## Features

- Limit armor stands per **block** and per **chunk** on a configurable timer or TPS threshold (non-Folia).
- Whitelist checks for named stands, specific name keywords, invulnerable stands, invisible stands, small stands, stands with arms, stands without base plates, or stands wearing armor.
- Optional protection hooks for **HolographicDisplays** and **ModelEngine** (no required dependencies).
- Anti-lag-machine options: chunk placement cap, dispenser spawn blocking, gravity/water movement blocking, and piston push blocking.
- Notifications via Console, in-game staff alerts, Discord webhooks, and Telegram bots.
- Native **Folia** global, region, and async scheduler support (`folia-supported: true`).

## Commands & Permissions

| Command | Description | Permission |
| :--- | :--- | :--- |
| `/asl help` | Show usage | `armorstandlimiter.help` |
| `/asl reload` | Reload config, hooks, notifications, and tasks | `armorstandlimiter.reload` |
| `/asl check [chunk/xyz]` | Count total and removable armor stands | `armorstandlimiter.check` |
| `/asl clear [chunk/xyz]` | Remove detected armor stands | `armorstandlimiter.clear` |
| `/asl test` | Send a test alert to configured channels | `armorstandlimiter.test` |

Additional permissions (legacy `armostandlimiter.*` aliases are also supported, default `op`):
- `armorstandlimiter.bypass` — Bypass chunk placement limits
- `armorstandlimiter.notify` — Receive in-game removal alerts

## Building from Source

Requires **Java 21+** (Java 25 for the `verify-26.3` profile) and **Apache Maven 3.9+**.

```bash
git clone https://github.com/KhoaSuperT1/ArmourStand-Limiter-V2.git
cd ArmourStand-Limiter-V2
mvn package
```

Output JAR: `target/ArmorStand-Limiter-v2.1.0.jar`
