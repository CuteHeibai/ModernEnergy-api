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

	@Test
	void formatsElectricalUnits() {
		assertEquals("120 V", ElectricalUnits.formatVoltage(120.0));
		assertEquals("10.5 A", ElectricalUnits.formatCurrent(10.5));
		assertEquals("12 Ohm", ElectricalUnits.formatResistance(12.0));
		assertEquals("50 Hz", ElectricalUnits.formatFrequency(50.0));
		assertEquals("1200 ME/t", ElectricalUnits.formatPower(1200.0));
	}
}
