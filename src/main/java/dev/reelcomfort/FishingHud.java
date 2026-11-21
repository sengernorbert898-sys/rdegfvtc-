package dev.reelcomfort;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Stats panel, bite flash and rod-durability warning. */
public final class FishingHud {
	private static final int LINE_HEIGHT = 10;
	private static final int PAD = 4;

	private record Line(String text, int color) {
	}

	private FishingHud() {
	}

	public static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;
		ReelComfortConfig cfg = ReelComfortClient.config;
		FishingSession session = ReelComfortClient.SESSION;
		if (player == null || mc.options.hudHidden || cfg == null) {
			return;
		}

		Font font = mc.font;
		int screenW = mc.getWindow().getGuiScaledWidth();
		int screenH = mc.getWindow().getGuiScaledHeight();

		// Bite flash, centered above the crosshair.
		if (cfg.biteAlertFlash && session.tick < session.biteFlashUntil) {
			String text = "§l! BITE !";
			int w = font.width(text);
			graphics.text(font, text, (screenW - w) / 2, screenH / 2 - 36, 0xFFFFFF55, true);
		}

		ItemStack rod = heldRod(player);

		if (!cfg.hudEnabled || !ReelComfortClient.hudVisible) {
			return;
		}
		boolean active = session.hookOut
				|| rod != null
				|| session.tick - session.lastActiveTick < cfg.hudLingerSeconds * 20L;
		if (cfg.hudOnlyWhileFishing && !active) {
			return;
		}

		List<Line> lines = new ArrayList<>();
		lines.add(new Line("§bReel Comfort", 0xFFFFFFFF));

		if (cfg.showTimer) {
			lines.add(new Line("Fishing time: " + formatTime(session.fishingSeconds()), 0xFFE0E0E0));
		}
		if (cfg.showBites) {
			lines.add(new Line(String.format(Locale.ROOT, "Casts: %d  Bites: %d  Reeled: %d",
					session.casts, session.bites, session.reeled), 0xFFE0E0E0));
		}
		if (cfg.showWait) {
			double wait = session.avgWaitSeconds();
			lines.add(new Line("Avg wait: " + (wait < 0 ? "--" : String.format(Locale.ROOT, "%.1fs", wait)),
					0xFFE0E0E0));
		}
		if (cfg.showExpected) {
			double ideal = session.expectedBitesPerHourIdeal();
			double real = session.expectedBitesPerHourRealistic(cfg);
			lines.add(new Line("Expected bites/h: "
					+ (real < 0 ? "--" : String.format(Locale.ROOT, "%.0f", real)), 0xFF8CFF8C));
			if (ideal >= 0) {
				lines.add(new Line(String.format(Locale.ROOT, "  (%.0f/h if you reel instantly)", ideal),
						0xFF9A9A9A));
			}
		}
		if (cfg.showReaction) {
			double react = session.avgReactionSeconds();
			lines.add(new Line("Avg reaction: " + (react < 0 ? "--" : String.format(Locale.ROOT, "%.2fs", react)),
					0xFFE0E0E0));
		}

		if (cfg.durabilityWarning && rod != null && rod.isDamageableItem()) {
			int remaining = rod.getMaxDamage() - rod.getDamageValue();
			if (remaining <= cfg.durabilityWarnUses) {
				lines.add(new Line("§lROD LOW: " + remaining + " uses left", 0xFFFF5555));
			}
		}

		int width = 0;
		for (Line line : lines) {
			width = Math.max(width, font.width(line.text()));
		}
		int boxW = width + PAD * 2;
		int boxH = lines.size() * LINE_HEIGHT + PAD * 2 - 2;

		int x = cfg.hudAnchor.endsWith("right") ? screenW - boxW - cfg.hudOffsetX : cfg.hudOffsetX;
		int y = cfg.hudAnchor.startsWith("bottom") ? screenH - boxH - cfg.hudOffsetY : cfg.hudOffsetY;

		if (cfg.hudBackground) {
			graphics.fill(x, y, x + boxW, y + boxH, 0x90000000);
		}
		int ty = y + PAD;
		for (Line line : lines) {
			graphics.text(font, line.text(), x + PAD, ty, line.color(), true);
			ty += LINE_HEIGHT;
		}
	}

	private static ItemStack heldRod(LocalPlayer player) {
		ItemStack main = player.getMainHandItem();
		if (main.is(Items.FISHING_ROD)) {
			return main;
		}
		ItemStack off = player.getOffhandItem();
		return off.is(Items.FISHING_ROD) ? off : null;
	}

	private static String formatTime(double seconds) {
		int total = (int) seconds;
		return String.format(Locale.ROOT, "%d:%02d", total / 60, total % 60);
	}
}
