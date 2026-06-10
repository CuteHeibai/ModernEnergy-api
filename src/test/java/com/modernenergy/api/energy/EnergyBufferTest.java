package com.modernenergy.api.energy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnergyBufferTest {
	@Test
	void insertClampsToCapacity() {
		EnergyBuffer buffer = new EnergyBuffer(100, 0, 1000, 1000);

		assertEquals(100, buffer.insert(250, EnergyAction.EXECUTE));
		assertEquals(100, buffer.getEnergy());
	}

	@Test
	void insertHonorsMaxInsert() {
		EnergyBuffer buffer = new EnergyBuffer(1000, 0, 75, 1000);

		assertEquals(75, buffer.insert(500, EnergyAction.EXECUTE));
		assertEquals(75, buffer.getEnergy());
	}

	@Test
	void extractHonorsEnergyAndMaxExtract() {
		EnergyBuffer buffer = new EnergyBuffer(1000, 500, 1000, 125);

		assertEquals(125, buffer.extract(500, EnergyAction.EXECUTE));
		assertEquals(375, buffer.getEnergy());
	}

	@Test
	void simulateDoesNotMutate() {
		EnergyBuffer buffer = new EnergyBuffer(1000, 500, 200, 200);

		assertEquals(200, buffer.insert(300, EnergyAction.SIMULATE));
		assertEquals(200, buffer.extract(300, EnergyAction.SIMULATE));
		assertEquals(500, buffer.getEnergy());
	}

	@Test
	void consumeRequiresExactAmount() {
		EnergyBuffer buffer = new EnergyBuffer(1000, 500, 1000, 100);

		assertFalse(buffer.consume(200, EnergyAction.EXECUTE));
		assertEquals(500, buffer.getEnergy());
		assertTrue(buffer.consume(100, EnergyAction.EXECUTE));
		assertEquals(400, buffer.getEnergy());
	}

	@Test
	void negativeCapacityAndRatesAreRejected() {
		assertThrows(IllegalArgumentException.class, () -> new EnergyBuffer(-1, 0, 0, 0));
		assertThrows(IllegalArgumentException.class, () -> new EnergyBuffer(1, 0, -1, 0));
		assertThrows(IllegalArgumentException.class, () -> new EnergyBuffer(1, 0, 0, -1));
	}
}
