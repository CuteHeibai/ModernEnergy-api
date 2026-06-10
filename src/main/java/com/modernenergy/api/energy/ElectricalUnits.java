package com.modernenergy.api.energy;

import java.util.Locale;

public final class ElectricalUnits {
	private ElectricalUnits() {
	}

	public static String formatVoltage(double voltage) {
		return format(voltage) + " V";
	}

	public static String formatCurrent(double current) {
		return format(current) + " A";
	}

	public static String formatResistance(double resistance) {
		return format(resistance) + " Ohm";
	}

	public static String formatFrequency(double frequency) {
		return format(frequency) + " Hz";
	}

	public static String formatPower(double power) {
		return format(power) + " ME/t";
	}

	private static String format(double value) {
		String formatted = String.format(Locale.ROOT, "%.2f", value);
		return formatted
				.replaceAll("0+$", "")
				.replaceAll("\\.$", "");
	}
}
