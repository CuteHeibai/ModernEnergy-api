package com.modernenergy.api.energy;

public interface EnergyStorage {
	long getEnergy();

	long getCapacity();

	long getMaxInsert();

	long getMaxExtract();

	long insert(long amount, EnergyAction action);

	long extract(long amount, EnergyAction action);

	default boolean canInsert() {
		return getMaxInsert() > 0 && getEnergy() < getCapacity();
	}

	default boolean canExtract() {
		return getMaxExtract() > 0 && getEnergy() > 0;
	}

	default boolean consume(long amount, EnergyAction action) {
		if (amount < 0) {
			return false;
		}
		if (amount == 0) {
			return true;
		}
		if (extract(amount, EnergyAction.SIMULATE) != amount) {
			return false;
		}
		if (action.shouldExecute()) {
			extract(amount, EnergyAction.EXECUTE);
		}
		return true;
	}
}
