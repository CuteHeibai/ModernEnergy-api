package com.modernenergy.api.energy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ElectricalPropertiesTest {
	@Test
	void derivesCurrentAndPowerFromVoltageAndResistance() {
		ElectricalProperties properties = ElectricalProperties.fromVoltageAndResistance(120.0, 12.0);

		assertEquals(120.0, properties.voltage());
		assertEquals(10.0, properties.current());
		assertEquals(12.0, properties.resistance());
		assertEquals(50.0, properties.frequency());
		assertEquals(1200.0, properties.power());
		assertEquals(1200, properties.toEnergyRate());
	}

	@Test
	void derivesResistanceFromVoltageAndCurrent() {
		ElectricalProperties properties = ElectricalProperties.fromVoltageAndCurrent(240.0, 20.0);

		assertEquals(12.0, properties.resistance());
		assertEquals(4800.0, properties.power());
	}

	@Test
	void defaultProfileUsesChinaMainsReference() {
		ElectricalProperties properties = ElectricalProperties.DEFAULT;

		assertEquals(220.0, properties.voltage());
		assertEquals(10.0, properties.current());
		assertEquals(22.0, properties.resistance());
		assertEquals(50.0, properties.frequency());
		assertEquals(2200, properties.toEnergyRate());
	}

	@Test
	void rejectsInvalidElectricalValues() {
		assertThrows(IllegalArgumentException.class, () -> ElectricalProperties.fromVoltageAndResistance(0.0, 1.0));
		assertThrows(IllegalArgumentException.class, () -> ElectricalProperties.fromVoltageAndResistance(1.0, 0.0));
		assertThrows(IllegalArgumentException.class, () -> ElectricalProperties.fromVoltageAndResistance(1.0, 1.0, 0.0));
		assertThrows(IllegalArgumentException.class, () -> ElectricalProperties.fromVoltageAndCurrent(1.0, Double.NaN));
	}
}
