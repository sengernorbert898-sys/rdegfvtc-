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
		if (player == null || cfg == null || mc.gui == null || mc.gui.hud.isHidden()) {
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
