package dev.reelcomfort;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.DustParticleOptions;

/**
 * Draws a flat ring of colored dust particles around the bobber.
 * The ring follows the bobber while it is out and stays at its last position for
 * {@code ringDurationSeconds} after you reel in. Uses only vanilla particles, so it needs no
 * custom render code. (Particles must not be set to "Minimal" in Video Settings.)
 */
public final class RingParticles {
	/** Dust particles live ~0.5-2s; refresh a bit faster than that for a continuous ring. */
	private static final int REFRESH_TICKS = 4;

	private RingParticles() {
	}

	public static void tick(Minecraft mc, FishingSession session, ReelComfortConfig cfg) {
		if (!cfg.ringEnabled || mc.level == null || !session.hasPos) {
			return;
		}
		if (session.tick > session.ringExpireTick) {
			return;
		}
		if (session.tick % REFRESH_TICKS != 0) {
			return;
		}

		int rgb = session.biteActive
				? ReelComfortConfig.parseColor(cfg.ringBiteColor, 0xFF5555)
				: ReelComfortConfig.parseColor(cfg.ringColor, 0x55FFFF);
		DustParticleOptions options = new DustParticleOptions(rgb, (float) cfg.ringParticleSize);

		double y = session.posY + 0.08;
		int points = cfg.ringPoints;
		for (int i = 0; i < points; i++) {
			double angle = (Math.PI * 2.0 * i) / points;
			double x = session.posX + Math.cos(angle) * cfg.ringRadius;
			double z = session.posZ + Math.sin(angle) * cfg.ringRadius;
			mc.level.addParticle(options, x, y, z, 0.0, 0.0, 0.0);
		}
	}
}
