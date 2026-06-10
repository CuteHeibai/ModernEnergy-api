package com.modernenergy.api.energy;

public final class EnergyBuffer implements ElectricalEnergyStorage {
	private long energy;
	private long capacity;
	private long maxInsert;
	private long maxExtract;
	private ElectricalProperties electricalProperties = ElectricalProperties.DEFAULT;

	public EnergyBuffer(long capacity, long maxInsert, long maxExtract) {
		this(capacity, 0, maxInsert, maxExtract);
	}

	public EnergyBuffer(long capacity, long energy, long maxInsert, long maxExtract) {
		setCapacity(capacity);
		setTransferRates(maxInsert, maxExtract);
		setEnergy(energy);
	}

	@Override
	public long getEnergy() {
		return energy;
	}

	@Override
	public long getCapacity() {
		return capacity;
	}

	@Override
	public long getMaxInsert() {
		return maxInsert;
	}

	@Override
	public long getMaxExtract() {
		return maxExtract;
	}

	@Override
	public ElectricalProperties getElectricalProperties() {
		return electricalProperties;
	}

	public void setEnergy(long energy) {
		this.energy = clamp(energy, 0, capacity);
	}

	public void setCapacity(long capacity) {
		this.capacity = requireNonNegative(capacity, "capacity");
		this.energy = Math.min(energy, this.capacity);
	}

	public void setTransferRates(long maxInsert, long maxExtract) {
		this.maxInsert = requireNonNegative(maxInsert, "maxInsert");
		this.maxExtract = requireNonNegative(maxExtract, "maxExtract");
	}

	public void setElectricalProperties(ElectricalProperties electricalProperties) {
		this.electricalProperties = java.util.Objects.requireNonNull(electricalProperties, "electricalProperties");
	}

	public void setElectricalProperties(ElectricalProperties electricalProperties, boolean applyTransferRate) {
		setElectricalProperties(electricalProperties);
		if (applyTransferRate) {
			applyElectricalTransferRate();
		}
	}

	public void applyElectricalTransferRate() {
		long transferRate = electricalProperties.toEnergyRate();
		setTransferRates(transferRate, transferRate);
	}

	@Override
	public long insert(long amount, EnergyAction action) {
		if (amount <= 0 || maxInsert == 0 || energy >= capacity) {
			return 0;
		}

		long inserted = Math.min(Math.min(amount, maxInsert), capacity - energy);
		if (action.shouldExecute()) {
			energy += inserted;
		}
		return inserted;
	}

	@Override
	public long extract(long amount, EnergyAction action) {
		if (amount <= 0 || maxExtract == 0 || energy == 0) {
			return 0;
		}

		long extracted = Math.min(Math.min(amount, maxExtract), energy);
		if (action.shouldExecute()) {
			energy -= extracted;
		}
		return extracted;
	}

	private static long requireNonNegative(long value, String name) {
		if (value < 0) {
			throw new IllegalArgumentException(name + " must be non-negative");
		}
		return value;
	}

	private static long clamp(long value, long min, long max) {
		return Math.max(min, Math.min(value, max));
	}
}
