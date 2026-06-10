package com.modernenergy.core.debug;

import com.modernenergy.api.energy.EnergyBuffer;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

final class DebugEnergyRegistry {
	private static final Map<String, EnergyBuffer> BUFFERS = new LinkedHashMap<>();

	private DebugEnergyRegistry() {
	}

	static boolean contains(String id) {
		return BUFFERS.containsKey(id);
	}

	static EnergyBuffer create(String id, long capacity, long energy, long maxInsert, long maxExtract) {
		EnergyBuffer buffer = new EnergyBuffer(capacity, energy, maxInsert, maxExtract);
		BUFFERS.put(id, buffer);
		return buffer;
	}

	static Optional<EnergyBuffer> get(String id) {
		return Optional.ofNullable(BUFFERS.get(id));
	}

	static boolean remove(String id) {
		return BUFFERS.remove(id) != null;
	}

	static int clear() {
		int size = BUFFERS.size();
		BUFFERS.clear();
		return size;
	}

	static Map<String, EnergyBuffer> snapshot() {
		return Collections.unmodifiableMap(BUFFERS);
	}
}
