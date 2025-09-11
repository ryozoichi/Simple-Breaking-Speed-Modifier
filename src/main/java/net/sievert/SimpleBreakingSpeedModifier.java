package net.sievert;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SimpleBreakingSpeedModifier
 *
 * Fabric mod entry point that sets each player's block breaking speed attribute
 * based on the loaded config value, whenever they join the world.
 */
public class SimpleBreakingSpeedModifier implements ModInitializer {

	/** Mod ID, used for logging and config file naming. */
	public static final String MOD_ID = "simple_breaking_speed_modifier";

	/** Logger for mod messages. */
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	/**
	 * Mod initializer. Loads config and registers player join handler.
	 */
	@Override
	public void onInitialize() {
		// Load (and validate) config on mod initialization
		Config.load();

		// When a player joins the server, set their block break speed attribute
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ServerPlayerEntity player = handler.player;
			EntityAttributeInstance attr = player.getAttributeInstance(EntityAttributes.PLAYER_BLOCK_BREAK_SPEED);
			if (attr != null) {
				// Apply config value for breaking speed
				attr.setBaseValue(Config.INSTANCE.playerBlockBreakSpeedBase);
				if (Config.INSTANCE.enableDebugLogging) {
					LOGGER.info("Set {}'s base block break speed to {}", player.getGameProfile().getName(), Config.INSTANCE.playerBlockBreakSpeedBase);
				}
			}
		});
	}
}