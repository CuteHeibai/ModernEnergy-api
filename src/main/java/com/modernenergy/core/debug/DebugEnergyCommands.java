package com.modernenergy.core.debug;

import com.modernenergy.api.energy.EnergyAction;
import com.modernenergy.api.energy.EnergyBuffer;
import com.modernenergy.api.energy.EnergyStorage;
import com.modernenergy.api.energy.EnergyTransfer;
import com.modernenergy.api.energy.EnergyUnits;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public final class DebugEnergyCommands {
	private DebugEnergyCommands() {
	}

	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
				dispatcher.register(CommandManager.literal("modernenergy")
						.requires(source -> source.hasPermissionLevel(2))
						.then(CommandManager.literal("energy")
								.then(createCommand())
								.then(infoCommand())
								.then(operationCommand("insert", EnergyOperation.INSERT))
								.then(operationCommand("extract", EnergyOperation.EXTRACT))
								.then(operationCommand("consume", EnergyOperation.CONSUME))
								.then(setRateCommand())
								.then(transferCommand())
								.then(clearCommand()))));
	}

	private static LiteralArgumentBuilder<ServerCommandSource> createCommand() {
		return CommandManager.literal("create")
				.then(CommandManager.argument("id", StringArgumentType.word())
						.then(CommandManager.argument("capacity", LongArgumentType.longArg(0))
								.executes(DebugEnergyCommands::create)
								.then(CommandManager.argument("energy", LongArgumentType.longArg(0))
										.executes(DebugEnergyCommands::create)
										.then(CommandManager.argument("maxInsert", LongArgumentType.longArg(0))
												.executes(DebugEnergyCommands::create)
												.then(CommandManager.argument("maxExtract", LongArgumentType.longArg(0))
														.executes(DebugEnergyCommands::create))))));
	}

	private static LiteralArgumentBuilder<ServerCommandSource> infoCommand() {
		return CommandManager.literal("info")
				.executes(DebugEnergyCommands::list)
				.then(CommandManager.argument("id", StringArgumentType.word())
						.executes(DebugEnergyCommands::info));
	}

	private static LiteralArgumentBuilder<ServerCommandSource> operationCommand(String name, EnergyOperation operation) {
		return CommandManager.literal(name)
				.then(CommandManager.argument("id", StringArgumentType.word())
						.then(CommandManager.argument("amount", LongArgumentType.longArg(0))
								.executes(context -> operate(context, operation, false))
								.then(CommandManager.argument("simulate", BoolArgumentType.bool())
										.executes(context -> operate(context, operation, BoolArgumentType.getBool(context, "simulate"))))));
	}

	private static LiteralArgumentBuilder<ServerCommandSource> setRateCommand() {
		return CommandManager.literal("set-rate")
				.then(CommandManager.argument("id", StringArgumentType.word())
						.then(CommandManager.argument("maxInsert", LongArgumentType.longArg(0))
								.then(CommandManager.argument("maxExtract", LongArgumentType.longArg(0))
										.executes(DebugEnergyCommands::setRate))));
	}

	private static LiteralArgumentBuilder<ServerCommandSource> transferCommand() {
		return CommandManager.literal("transfer")
				.then(CommandManager.argument("source", StringArgumentType.word())
						.then(CommandManager.argument("target", StringArgumentType.word())
								.then(CommandManager.argument("amount", LongArgumentType.longArg(0))
										.executes(context -> transfer(context, false))
										.then(CommandManager.argument("simulate", BoolArgumentType.bool())
												.executes(context -> transfer(context, BoolArgumentType.getBool(context, "simulate")))))));
	}

	private static LiteralArgumentBuilder<ServerCommandSource> clearCommand() {
		return CommandManager.literal("clear")
				.executes(DebugEnergyCommands::clearAll)
				.then(CommandManager.argument("id", StringArgumentType.word())
						.executes(DebugEnergyCommands::clearOne));
	}

	private static int create(CommandContext<ServerCommandSource> context) {
		String id = StringArgumentType.getString(context, "id");
		long capacity = LongArgumentType.getLong(context, "capacity");
		long energy = optionalLong(context, "energy", 0);
		long maxInsert = optionalLong(context, "maxInsert", capacity);
		long maxExtract = optionalLong(context, "maxExtract", capacity);
		boolean replacing = DebugEnergyRegistry.contains(id);

		EnergyBuffer buffer = DebugEnergyRegistry.create(id, capacity, energy, maxInsert, maxExtract);
		return send(context, (replacing ? "Replaced " : "Created ") + id + ": " + format(buffer));
	}

	private static int list(CommandContext<ServerCommandSource> context) {
		if (DebugEnergyRegistry.snapshot().isEmpty()) {
			return send(context, "No debug energy buffers exist.");
		}

		StringBuilder builder = new StringBuilder("Debug energy buffers:");
		DebugEnergyRegistry.snapshot().forEach((id, buffer) ->
				builder.append("\n- ").append(id).append(": ").append(format(buffer)));
		return send(context, builder.toString());
	}

	private static int info(CommandContext<ServerCommandSource> context) {
		String id = StringArgumentType.getString(context, "id");
		return DebugEnergyRegistry.get(id)
				.map(buffer -> send(context, id + ": " + format(buffer)))
				.orElseGet(() -> error(context, "Unknown debug energy buffer: " + id));
	}

	private static int operate(CommandContext<ServerCommandSource> context, EnergyOperation operation, boolean simulate) {
		String id = StringArgumentType.getString(context, "id");
		long amount = LongArgumentType.getLong(context, "amount");
		EnergyAction action = simulate ? EnergyAction.SIMULATE : EnergyAction.EXECUTE;

		return DebugEnergyRegistry.get(id)
				.map(buffer -> {
					long changed = switch (operation) {
						case INSERT -> buffer.insert(amount, action);
						case EXTRACT -> buffer.extract(amount, action);
						case CONSUME -> buffer.consume(amount, action) ? amount : 0;
					};

					String verb = switch (operation) {
						case INSERT -> "Inserted ";
						case EXTRACT -> "Extracted ";
						case CONSUME -> "Consumed ";
					};

					return send(context, prefix(simulate) + verb + EnergyUnits.format(changed) + " on " + id
							+ "; now " + format(buffer));
				})
				.orElseGet(() -> error(context, "Unknown debug energy buffer: " + id));
	}

	private static int setRate(CommandContext<ServerCommandSource> context) {
		String id = StringArgumentType.getString(context, "id");
		long maxInsert = LongArgumentType.getLong(context, "maxInsert");
		long maxExtract = LongArgumentType.getLong(context, "maxExtract");

		return DebugEnergyRegistry.get(id)
				.map(buffer -> {
					buffer.setTransferRates(maxInsert, maxExtract);
					return send(context, "Updated rates for " + id + ": " + format(buffer));
				})
				.orElseGet(() -> error(context, "Unknown debug energy buffer: " + id));
	}

	private static int transfer(CommandContext<ServerCommandSource> context, boolean simulate) {
		String sourceId = StringArgumentType.getString(context, "source");
		String targetId = StringArgumentType.getString(context, "target");
		long amount = LongArgumentType.getLong(context, "amount");

		if (sourceId.equals(targetId)) {
			return error(context, "Source and target must be different.");
		}

		EnergyBuffer source = DebugEnergyRegistry.get(sourceId).orElse(null);
		EnergyBuffer target = DebugEnergyRegistry.get(targetId).orElse(null);

		if (source == null) {
			return error(context, "Unknown source debug energy buffer: " + sourceId);
		}
		if (target == null) {
			return error(context, "Unknown target debug energy buffer: " + targetId);
		}

		long moved = EnergyTransfer.transfer(source, target, amount, simulate ? EnergyAction.SIMULATE : EnergyAction.EXECUTE);
		return send(context, prefix(simulate) + "Transferred " + EnergyUnits.format(moved) + " from " + sourceId
				+ " to " + targetId + ". Source " + format(source) + "; target " + format(target));
	}

	private static int clearAll(CommandContext<ServerCommandSource> context) {
		int count = DebugEnergyRegistry.clear();
		return send(context, "Cleared " + count + " debug energy buffers.");
	}

	private static int clearOne(CommandContext<ServerCommandSource> context) {
		String id = StringArgumentType.getString(context, "id");
		if (DebugEnergyRegistry.remove(id)) {
			return send(context, "Cleared debug energy buffer: " + id);
		}
		return error(context, "Unknown debug energy buffer: " + id);
	}

	private static String format(EnergyStorage storage) {
		return EnergyUnits.format(storage.getEnergy()) + "/" + EnergyUnits.format(storage.getCapacity())
				+ " (in " + EnergyUnits.format(storage.getMaxInsert())
				+ "/out " + EnergyUnits.format(storage.getMaxExtract()) + ")";
	}

	private static String prefix(boolean simulate) {
		return simulate ? "Simulated: " : "";
	}

	private static long optionalLong(CommandContext<ServerCommandSource> context, String name, long fallback) {
		try {
			return LongArgumentType.getLong(context, name);
		} catch (IllegalArgumentException ignored) {
			return fallback;
		}
	}

	private static int send(CommandContext<ServerCommandSource> context, String message) {
		context.getSource().sendFeedback(() -> Text.literal(message), false);
		return Command.SINGLE_SUCCESS;
	}

	private static int error(CommandContext<ServerCommandSource> context, String message) {
		context.getSource().sendError(Text.literal(message));
		return 0;
	}

	private enum EnergyOperation {
		INSERT,
		EXTRACT,
		CONSUME
	}
}
