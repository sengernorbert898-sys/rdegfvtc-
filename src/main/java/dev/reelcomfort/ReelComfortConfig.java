package dev.reelcomfort;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Plain JSON config stored at {@code config/reelcomfort.json}.
 * Edit the file, then press the "Reload Config" key in game (unbound by default).
 */
public final class ReelComfortConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("reelcomfort.json");

	// ---- Bite detection ----
	/** How far (in blocks, per tick) the bobber must drop to count as a bite. Lower = more sensitive. */
	public double biteDipThreshold = 0.08;

	// ---- Bite alert ----
	public boolean biteAlertSound = true;
	/** One of: "orb", "ding", "levelup". */
	public String soundType = "orb";
	public float soundVolume = 1.0f;
	public float soundPitch = 1.2f;
	public boolean biteAlertFlash = true;
	public double biteFlashSeconds = 1.5;

	// ---- HUD ----
	public boolean hudEnabled = true;
	/** One of: "top_left", "top_right", "bottom_left", "bottom_right". */
	public String hudAnchor = "top_left";
	public int hudOffsetX = 6;
	public int hudOffsetY = 6;
	public boolean hudBackground = true;
	/** Only show the stats HUD while holding a rod / having a bobber out (plus a linger time). */
	public boolean hudOnlyWhileFishing = true;
	public int hudLingerSeconds = 30;
	public boolean showTimer = true;
	public boolean showWait = true;
	public boolean showBites = true;
	public boolean showExpected = true;
	public boolean showReaction = true;

	// ---- Expected bites per hour ----
	/** Assumed bite-to-next-landing time (reel in + recast) until enough samples are measured. */
	public double defaultRecastSeconds = 3.0;

	// ---- Bobber ring ----
	public boolean ringEnabled = true;
	public double ringRadius = 1.0;
	public String ringColor = "#55FFFF";
	/** Ring color while a bite is active. */
	public String ringBiteColor = "#FF5555";
	/** How long the ring stays after the bobber is reeled in. */
	public double ringDurationSeconds = 10.0;
	public int ringPoints = 28;
	public double ringParticleSize = 0.8;

	// ---- Rod durability ----
	public boolean durabilityWarning = true;
	public int durabilityWarnUses = 10;

	public static ReelComfortConfig load() {
		ReelComfortConfig cfg = null;
		if (Files.exists(PATH)) {
			try {
				cfg = GSON.fromJson(Files.readString(PATH), ReelComfortConfig.class);
			} catch (Exception e) {
				ReelComfortClient.LOGGER.warn("Could not read {}, using defaults", PATH, e);
			}
		}
		if (cfg == null) {
			cfg = new ReelComfortConfig();
		}
		cfg.sanitize();
		cfg.save(); // writes new options added by updates
		return cfg;
	}

	public void save() {
		try {
			Files.createDirectories(PATH.getParent());
			Files.writeString(PATH, GSON.toJson(this));
		} catch (IOException e) {
			ReelComfortClient.LOGGER.warn("Could not write {}", PATH, e);
		}
	}

	private void sanitize() {
		biteDipThreshold = clamp(biteDipThreshold, 0.02, 0.5);
		soundVolume = (float) clamp(soundVolume, 0.0, 2.0);
		soundPitch = (float) clamp(soundPitch, 0.5, 2.0);
		biteFlashSeconds = clamp(biteFlashSeconds, 0.2, 10.0);
		hudLingerSeconds = (int) clamp(hudLingerSeconds, 0, 3600);
		defaultRecastSeconds = clamp(defaultRecastSeconds, 0.5, 30.0);
		ringRadius = clamp(ringRadius, 0.25, 8.0);
		ringDurationSeconds = clamp(ringDurationSeconds, 0.5, 120.0);
		ringPoints = (int) clamp(ringPoints, 6, 128);
		ringParticleSize = clamp(ringParticleSize, 0.1, 4.0);
		durabilityWarnUses = (int) clamp(durabilityWarnUses, 1, 64);
		if (soundType == null) soundType = "orb";
		if (hudAnchor == null) hudAnchor = "top_left";
		if (ringColor == null) ringColor = "#55FFFF";
		if (ringBiteColor == null) ringBiteColor = "#FF5555";
	}

	private static double clamp(double v, double min, double max) {
		return Math.max(min, Math.min(max, v));
	}

	/** Parses "#RRGGBB" (or "RRGGBB") into an RGB int; falls back on bad input. */
	public static int parseColor(String hex, int fallback) {
		try {
			String s = hex.startsWith("#") ? hex.substring(1) : hex;
			return Integer.parseInt(s, 16) & 0xFFFFFF;
		} catch (Exception e) {
			return fallback;
		}
	}
}
