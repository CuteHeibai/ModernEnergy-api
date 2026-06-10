package com.modernenergy.api.energy;

public enum EnergyAction {
	EXECUTE,
	SIMULATE;

	public boolean shouldExecute() {
		return this == EXECUTE;
	}
}
