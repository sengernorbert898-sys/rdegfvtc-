package dev.reelcomfort;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Items;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reel Comfort: client-side fishing quality of life.
 * This mod only observes and informs. It never casts, reels or otherwise sends fishing input.
 */
public final class ReelComfortClient implements ClientModInitializer {
	public static final String MOD_ID = "reelcomfort";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static ReelComfortConfig config;
	public static final FishingSession SESSION = new FishingSession();
	/** Runtime HUD toggle (separate from the config option). */
	public static boolean hudVisible = true;

	private static KeyMapping equipRodKey;
	private static KeyMapping toggleHudKey;
	private static KeyMapping resetStatsKey;
	private static KeyMapping reloadConfigKey;

	@Override
	public void onInitializeClient() {
		config = ReelComfortConfig.load();

		KeyMapping.Category category = KeyMapping.Category.register(
				Identifier.fromNamespaceAndPath(MOD_ID, "main"));
		equipRodKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.reelcomfort.equip_rod", InputConstants.Type.KEYBOARD, InputConstants.KEY_R, category));
		toggleHudKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.reelcomfort.toggle_hud", InputConstants.Type.KEYBOARD, InputConstants.KEY_H, category));
		resetStatsKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.reelcomfort.reset_stats", InputConstants.Type.KEYBOARD, InputConstants.UNKNOWN.getValue(), category));
		reloadConfigKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.reelcomfort.reload_config", InputConstants.Type.KEYBOARD, InputConstants.UNKNOWN.getValue(), category));

		ClientTickEvents.END_CLIENT_TICK.register(ReelComfortClient::onTick);

		HudElementRegistry.attachElementBefore(
				VanillaHudElements.CHAT,
				Identifier.fromNamespaceAndPath(MOD_ID, "fishing_hud"),
				FishingHud::render);

		LOGGER.info("Reel Comfort loaded (client-side, no auto-fishing).");
	}

	private static void onTick(Minecraft mc) {
		while (toggleHudKey.consumeClick()) {
			hudVisible = !hudVisible;
		}
		while (resetStatsKey.consumeClick()) {
			SESSION.reset();
		}
		while (reloadConfigKey.consumeClick()) {
			config = ReelComfortConfig.load();
		}
		while (equipRodKey.consumeClick()) {
			selectNextRod(mc);
		}

		boolean bite = SESSION.update(mc, config);
		if (bite) {
			playBiteSound(mc);
		}
		RingParticles.tick(mc, SESSION, config);
	}

	private static void playBiteSound(Minecraft mc) {
		if (!config.biteAlertSound) {
			return;
		}
		SoundEvent sound = switch (config.soundType.toLowerCase(java.util.Locale.ROOT)) {
			case "ding" -> SoundEvents.ARROW_HIT_PLAYER;
			case "levelup" -> SoundEvents.PLAYER_LEVELUP;
			default -> SoundEvents.EXPERIENCE_ORB_PICKUP;
		};
		mc.getSoundManager().play(SimpleSoundInstance.forUI(sound, config.soundPitch, config.soundVolume));
	}

	/** Selects the next fishing rod in the hotbar (wraps around). Selecting only; never uses it. */
	private static void selectNextRod(Minecraft mc) {
		LocalPlayer player = mc.player;
		if (player == null) {
			return;
		}
		var inventory = player.getInventory();
		int current = inventory.getSelectedSlot();
		for (int offset = 1; offset <= 9; offset++) {
			int slot = (current + offset) % 9;
			if (inventory.getItem(slot).is(Items.FISHING_ROD)) {
				inventory.setSelectedSlot(slot);
				return;
			}
		}
	}
}
