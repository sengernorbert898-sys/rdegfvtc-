# Reel Comfort

Fishing quality of life, **client-side only**. Works on any server. **No auto-fishing**: the mod never casts or reels for you.

## Features

- **Bite alert** with sound and an on-screen flash
- **Session stats HUD**: fishing time, casts, bites, reeled in, average wait, average reaction time
- **Expected bites per hour**, based on your own measured wait and recast time
- **Bobber ring**: customizable particle ring that follows the bobber and stays for 10 seconds after you reel in, so you can recast to the same spot
- **Select Fishing Rod** hotkey (cycles through rods in your hotbar)
- **Low durability warning** for your rod

## Setup

Requires **Fabric API** and Minecraft **26.3**. Install on the client only.

Everything is configurable in `config/reelcomfort.json` (ring radius, color and duration, sound, HUD position and lines, bite sensitivity). Press the *Reload Config* key in game to apply changes.

Set Particles to **All** in Video Settings for the ring.

## Notes

Bite detection watches the bobber's movement, so it works without server support. If it misses or over-triggers on your setup, adjust `biteDipThreshold`.
