package com.modernenergy.api.energy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EnergyTransferTest {
	@Test
	void transferHonorsBothStorageLimits() {
		EnergyBuffer source = new EnergyBuffer(1000, 800, 1000, 250);
		EnergyBuffer target = new EnergyBuffer(1000, 900, 150, 1000);

		assertEquals(100, EnergyTransfer.transfer(source, target, 500, EnergyAction.EXECUTE));
		assertEquals(700, source.getEnergy());
		assertEquals(1000, target.getEnergy());
	}

	@Test
	void simulatedTransferDoesNotMutate() {
		EnergyBuffer source = new EnergyBuffer(1000, 800, 1000, 250);
		EnergyBuffer target = new EnergyBuffer(1000, 0, 150, 1000);

		assertEquals(150, EnergyTransfer.transfer(source, target, 500, EnergyAction.SIMULATE));
		assertEquals(800, source.getEnergy());
		assertEquals(0, target.getEnergy());
	}
}
