package com.modernenergy.api.energy;

public interface ElectricalEnergyStorage extends EnergyStorage {
	ElectricalProperties getElectricalProperties();

	default double getVoltage() {
		return getElectricalProperties().voltage();
	}

	default double getCurrent() {
		return getElectricalProperties().current();
	}

	default double getResistance() {
		return getElectricalProperties().resistance();
	}

	default double getFrequency() {
		return getElectricalProperties().frequency();
	}

	default long getElectricalTransferRate() {
		return getElectricalProperties().toEnergyRate();
	}
}
