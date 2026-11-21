package dev.reelcomfort;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

/**
 * Tracks the player's bobber and session statistics purely from client-side observation
 * (no mixins, no packets). Never sends input: it only observes.
 */
public final class FishingSession {
	/** Vanilla gives you roughly 1-2 seconds after a bite; treat a bite as "live" for 40 ticks. */
	private static final int BITE_WINDOW_TICKS = 40;
	/** Ignore the splash of the bobber landing. */
	private static final int SETTLE_TICKS = 20;
	/** Don't register another bite right after one. */
	private static final int BITE_COOLDOWN_TICKS = 30;

	/** Client tick counter (20 per second). */
	public long tick;

	// ---- Stats ----
	public int casts;
	public int bites;
	public int reeled;
	private long waitTotalTicks;
	private int waitSamples;
	private long reactTotalTicks;
	private int reactSamples;
	private long gapTotalTicks;
	private int gapSamples;
	/** Ticks spent with a bobber in the water. */
	public long fishingTicks;

	// ---- Live bobber state ----
	public boolean hookOut;
	public boolean landed;
	private long landedTick;
	private double lastY;
	public boolean biteActive;
	private long biteTick;
	private long lastBiteTick = -1000;
	private long lastReelBiteTick = -1;

	// ---- Ring / HUD state ----
	public boolean hasPos;
	public double posX, posY, posZ;
	public long ringExpireTick = -1;
	public long biteFlashUntil = -1;
	public long lastActiveTick = -100000;

	public void reset() {
		casts = 0;
		bites = 0;
		reeled = 0;
		waitTotalTicks = 0;
		waitSamples = 0;
		reactTotalTicks = 0;
		reactSamples = 0;
		gapTotalTicks = 0;
		gapSamples = 0;
		fishingTicks = 0;
		lastReelBiteTick = -1;
	}

	/** @return true on the tick a new bite is detected. */
	public boolean update(Minecraft mc, ReelComfortConfig cfg) {
		tick++;
		LocalPlayer player = mc.player;
		if (player == null) {
			hookOut = false;
			landed = false;
			biteActive = false;
			return false;
		}

		var hook = player.fishing;
		boolean bitNow = false;

		if (hook != null && !hook.isRemoved()) {
			if (!hookOut) {
				hookOut = true;
				landed = false;
				biteActive = false;
				casts++;
				lastY = hook.getY();
			}
			lastActiveTick = tick;

			if (!landed && hook.isInWater()) {
				landed = true;
				landedTick = tick;
				if (lastReelBiteTick >= 0) {
					gapTotalTicks += tick - lastReelBiteTick;
					gapSamples++;
					lastReelBiteTick = -1;
				}
			}

			if (landed) {
				fishingTicks++;
				double dy = hook.getY() - lastY;
				boolean hookedSomething = hook.getHookedIn() != null;
				if (!hookedSomething
						&& !biteActive
						&& tick - landedTick > SETTLE_TICKS
						&& tick - lastBiteTick > BITE_COOLDOWN_TICKS
						&& dy < -cfg.biteDipThreshold) {
					bitNow = true;
					biteActive = true;
					biteTick = tick;
					lastBiteTick = tick;
					bites++;
					waitTotalTicks += tick - landedTick;
					waitSamples++;
					if (cfg.biteAlertFlash) {
						biteFlashUntil = tick + (long) (cfg.biteFlashSeconds * 20.0);
					}
				}
				if (biteActive && tick - biteTick > BITE_WINDOW_TICKS) {
					biteActive = false;
				}

				// Ring follows the bobber and is refreshed while it is out.
				hasPos = true;
				posX = hook.getX();
				posY = hook.getY();
				posZ = hook.getZ();
				ringExpireTick = tick + (long) (cfg.ringDurationSeconds * 20.0);
			}
			lastY = hook.getY();
		} else if (hookOut) {
			// Bobber just disappeared: the player reeled in (or it despawned).
			if (biteActive) {
				reeled++;
				reactTotalTicks += tick - biteTick;
				reactSamples++;
				lastReelBiteTick = tick;
			}
			hookOut = false;
			landed = false;
			biteActive = false;
		}
		return bitNow;
	}

	// ---- Derived stats (all -1 when there is not enough data) ----

	public double avgWaitSeconds() {
		return waitSamples == 0 ? -1 : waitTotalTicks / 20.0 / waitSamples;
	}

	public double avgReactionSeconds() {
		return reactSamples == 0 ? -1 : reactTotalTicks / 20.0 / reactSamples;
	}

	public double avgRecastSeconds(ReelComfortConfig cfg) {
		return gapSamples == 0 ? cfg.defaultRecastSeconds : gapTotalTicks / 20.0 / gapSamples;
	}

	/** Bites per hour if you did nothing but wait (average cast-to-bite time only). */
	public double expectedBitesPerHourIdeal() {
		double w = avgWaitSeconds();
		return w <= 0 ? -1 : 3600.0 / w;
	}

	/** Bites per hour including your measured reel-in and recast time. */
	public double expectedBitesPerHourRealistic(ReelComfortConfig cfg) {
		double w = avgWaitSeconds();
		return w <= 0 ? -1 : 3600.0 / (w + avgRecastSeconds(cfg));
	}

	public double fishingSeconds() {
		return fishingTicks / 20.0;
	}
}
