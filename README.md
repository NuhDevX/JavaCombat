# JavaCombat (Nukkit)

This plugin attempts to replicate **Minecraft Java Edition 1.9+** combat mechanics onto a **Bedrock (Nukkit)** server:

- ⏱️ **Attack Cooldown** — damage scales based on how "charged" the attack is (like the yellow bar in Java).
- 💥 **Critical Hit** — x1.5 damage when attacking while falling (not sprinting, not in water, etc.) + star particles.
- 🌀 **Sweep Attack** — The sword attacks entities around the primary target while the cooldown is full.
- 🏃 **Sprint Reset** — sprint automatically stops when attacking, like Java.
- 👊 **Minecraft Java style knockback** — Direction & distance boost formula mimics vanilla Java, including sprint bonus & Knockback enchant.

## Konfigurasi

All numbers can be changed in `config.yml` without recompiling — including attack speed per item type, critical hit multiplier, sweep radius, and knockback formula.All numbers can be changed in `config.yml` without recompiling — including attack speed per item type, critical hit multiplier, sweep radius, and knockback formula.

## Command

- `/javacombat status` — check plugin status
- `/javacombat toggle` — temporarily active/disabled
- `/javacombat reload` — reload config.yml
