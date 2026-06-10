package com.modernenergy.api.energy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EnergyUnitsTest {
	@Test
	void formatsMechatronicEnergySymbol() {
		assertEquals("ME", EnergyUnits.SYMBOL);
		assertEquals("Mechatronic Energy", EnergyUnits.FULL_NAME);
		assertEquals("100 ME", EnergyUnits.format(100));
	}
}
