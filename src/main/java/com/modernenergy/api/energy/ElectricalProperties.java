package com.modernenergy.api.energy;

public record ElectricalProperties(double voltage, double current, double resistance, double frequency) {
	public static final double CHINA_MAINS_VOLTAGE = 220.0;
	public static final double CHINA_MAINS_FREQUENCY = 50.0;
	public static final double DEFAULT_REFERENCE_CURRENT = 10.0;
	public static final ElectricalProperties CHINA_MAINS = fromVoltageAndCurrent(
			CHINA_MAINS_VOLTAGE,
			DEFAULT_REFERENCE_CURRENT,
			CHINA_MAINS_FREQUENCY);
	public static final ElectricalProperties DEFAULT = CHINA_MAINS;

	public ElectricalProperties {
		requirePositive(voltage, "voltage");
		requirePositive(current, "current");
		requirePositive(resistance, "resistance");
		requirePositive(frequency, "frequency");
	}

	public static ElectricalProperties fromVoltageAndResistance(double voltage, double resistance) {
		return fromVoltageAndResistance(voltage, resistance, CHINA_MAINS_FREQUENCY);
	}

	public static ElectricalProperties fromVoltageAndResistance(double voltage, double resistance, double frequency) {
		requirePositive(voltage, "voltage");
		requirePositive(resistance, "resistance");
		requirePositive(frequency, "frequency");
		return new ElectricalProperties(voltage, voltage / resistance, resistance, frequency);
	}

	public static ElectricalProperties fromVoltageAndCurrent(double voltage, double current) {
		return fromVoltageAndCurrent(voltage, current, CHINA_MAINS_FREQUENCY);
	}

	public static ElectricalProperties fromVoltageAndCurrent(double voltage, double current, double frequency) {
		requirePositive(voltage, "voltage");
		requirePositive(current, "current");
		requirePositive(frequency, "frequency");
		return new ElectricalProperties(voltage, current, voltage / current, frequency);
	}

	public double power() {
		return voltage * current;
	}

	public long toEnergyRate() {
		double power = power();
		if (power >= Long.MAX_VALUE) {
			return Long.MAX_VALUE;
		}
		return Math.max(1, Math.round(power));
	}

	private static void requirePositive(double value, String name) {
		if (!Double.isFinite(value) || value <= 0) {
			throw new IllegalArgumentException(name + " must be a finite positive number");
		}
	}
}
