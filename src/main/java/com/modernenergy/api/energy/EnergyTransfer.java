package com.modernenergy.api.energy;

import java.util.Objects;

public final class EnergyTransfer {
	private EnergyTransfer() {
	}

	public static long transfer(EnergyStorage source, EnergyStorage target, long amount, EnergyAction action) {
		Objects.requireNonNull(source, "source");
		Objects.requireNonNull(target, "target");

		if (amount <= 0) {
			return 0;
		}

		long available = source.extract(amount, EnergyAction.SIMULATE);
		long accepted = target.insert(available, EnergyAction.SIMULATE);
		long moved = Math.min(available, accepted);

		if (action.shouldExecute() && moved > 0) {
			long extracted = source.extract(moved, EnergyAction.EXECUTE);
			return target.insert(extracted, EnergyAction.EXECUTE);
		}

		return moved;
	}
}
