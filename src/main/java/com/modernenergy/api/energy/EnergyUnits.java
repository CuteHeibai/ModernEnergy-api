package com.modernenergy.api.energy;

public final class EnergyUnits {
	public static final String SYMBOL = "ME";
	public static final String FULL_NAME = "Mechatronic Energy";

	private EnergyUnits() {
	}

	public static String format(long amount) {
		return amount + " " + SYMBOL;
	}
}
