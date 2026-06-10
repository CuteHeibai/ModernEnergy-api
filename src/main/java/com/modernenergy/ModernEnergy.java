package com.modernenergy;

import com.modernenergy.api.energy.EnergyUnits;
import com.modernenergy.core.debug.DebugEnergyCommands;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ModernEnergy implements ModInitializer {
	public static final String MOD_ID = "modern_energy";
	public static final String MOD_NAME = "Modern Energy";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing {} with {} ({})", MOD_NAME, EnergyUnits.FULL_NAME, EnergyUnits.SYMBOL);

		if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
			DebugEnergyCommands.register();
			LOGGER.info("Registered Modern Energy development commands.");
		}
	}
}
