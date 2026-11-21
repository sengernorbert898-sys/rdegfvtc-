# Reel Comfort

Client-side fishing quality of life for Minecraft Java **26.3** (Fabric). Install on your client only; it works on any server and does nothing on the server side.

**No auto-fishing.** Reel Comfort only watches and informs. It never casts, reels, clicks or sends fishing input for you.

## Features

- **Bite alert**: sound (3 choices) and an on-screen "BITE" flash when your bobber dips.
- **Session stats HUD**: fishing time, casts, bites, bites reeled in, average wait, average reaction time.
- **Expected bites per hour**: calculated from *your* measured wait time plus *your* measured reel-in and recast time, with an "if you reeled instantly" number underneath.
- **Bobber ring**: a customizable ring of colored particles around the bobber. It follows the bobber, turns red on a bite, and stays at the last spot for 10 seconds after you reel in so you can recast to the same place.
- **Rod hotkey**: select a fishing rod in your hotbar (press again to cycle to the next rod). Selecting only; it never uses the rod.
- **Rod durability warning**: warns when your rod is almost out of uses.

## Keys (Options > Controls > Key Binds > Reel Comfort)

| Action | Default |
| --- | --- |
| Select Fishing Rod | R |
| Toggle Fishing HUD | H |
| Reset Session Stats | unbound |
| Reload Config | unbound |

## Config

`config/reelcomfort.json` is created on first launch. Edit it, then press the **Reload Config** key in game. Notable options:

- `ringRadius`, `ringColor`, `ringBiteColor`, `ringDurationSeconds` (default 10), `ringPoints`, `ringParticleSize`
- `soundType` (`orb`, `ding`, `levelup`), `soundVolume`, `soundPitch`
- `hudAnchor` (`top_left`, `top_right`, `bottom_left`, `bottom_right`), `hudOffsetX/Y`, and `show*` toggles for each stat line
- `biteDipThreshold`: lower it if bites are missed, raise it if you get false alerts
- `durabilityWarnUses`

Set Particles to **All** in Video Settings so the ring is fully visible.

## How bite detection works

There are no mixins. Each tick the mod compares the bobber's height with the previous tick; a sudden drop after the bobber has settled in water counts as a bite. If it misses or false-triggers on your setup, adjust `biteDipThreshold`.

## Building

Requirements: **Java 25** and **Gradle 9.6+**.

```
gradle wrapper --gradle-version 9.6.0   # once, to create ./gradlew
./gradlew build
```

The jar is written to `build/libs/` (use the one without `-sources`). Test it with `./gradlew runClient`.

Versions used (Minecraft 26.3): Fabric Loader 0.19.5, Fabric Loom 1.17, Fabric API 0.166.2+26.3. Check <https://fabricmc.net/develop> for newer ones.

## Building on GitHub (no local setup)

Push this folder to a GitHub repo. The workflow in `.github/workflows/build.yml` builds the jar on every push: open the run in the Actions tab and download the `reel-comfort-jar` artifact. Pushing a tag like `v1.0.0` also attaches the jar to a GitHub release.

## Publishing to Modrinth

- Project type: Mod. Environment: **Client** required, **Server** unsupported.
- Loader: Fabric. Game version: 26.3. Dependency: Fabric API (required).
- Summary: "Fishing quality of life: bite alerts, session stats, bites per hour and a bobber ring. No auto-fishing."
- License: MIT. Use `MODRINTH.md` as the description.

## License

MIT
