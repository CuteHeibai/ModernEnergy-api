package com.modernenergy.core.debug;

import com.modernenergy.api.energy.ElectricalEnergyStorage;
import com.modernenergy.api.energy.ElectricalProperties;
import com.modernenergy.api.energy.ElectricalUnits;
import com.modernenergy.api.energy.EnergyAction;
import com.modernenergy.api.energy.EnergyBuffer;
import com.modernenergy.api.energy.EnergyStorage;
import com.modernenergy.api.energy.EnergyTransfer;
import com.modernenergy.api.energy.EnergyUnits;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public final class DebugEnergyCommands {
	private DebugEnergyCommands() {
	}

	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
				dispatcher.register(CommandManager.literal("modernenergy")
						.requires(source -> source.hasPermissionLevel(2))
						.then(CommandManager.literal("energy")
								.then(helpCommand())
								.then(createCommand())
								.then(infoCommand())
								.then(operationCommand("insert", EnergyOperation.INSERT))
								.then(operationCommand("extract", EnergyOperation.EXTRACT))
								.then(operationCommand("consume", EnergyOperation.CONSUME))
								.then(setRateCommand())
								.then(setElectricalCommand())
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

	private static LiteralArgumentBuilder<ServerCommandSource> helpCommand() {
		return CommandManager.literal("help")
				.executes(DebugEnergyCommands::help);
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

	private static LiteralArgumentBuilder<ServerCommandSource> setElectricalCommand() {
		return CommandManager.literal("set-electrical")
				.then(CommandManager.argument("id", StringArgumentType.word())
						.then(CommandManager.argument("voltage", DoubleArgumentType.doubleArg(Double.MIN_NORMAL))
								.then(CommandManager.argument("resistance", DoubleArgumentType.doubleArg(Double.MIN_NORMAL))
										.executes(context -> setElectrical(context, true))
										.then(CommandManager.argument("applyRate", BoolArgumentType.bool())
												.executes(context -> setElectrical(context, BoolArgumentType.getBool(context, "applyRate")))))));
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
		return send(context, Text.translatable(
				replacing ? "commands.modern_energy.energy.replaced" : "commands.modern_energy.energy.created",
				id,
				format(buffer)));
	}

	private static int list(CommandContext<ServerCommandSource> context) {
		if (DebugEnergyRegistry.snapshot().isEmpty()) {
			return send(context, Text.translatable("commands.modern_energy.energy.none"));
		}

		MutableText builder = Text.translatable("commands.modern_energy.energy.list.header");
		DebugEnergyRegistry.snapshot().forEach((id, buffer) ->
				builder.append(Text.literal("\n- "))
						.append(Text.translatable("commands.modern_energy.energy.list.entry", id, format(buffer))));
		return send(context, builder);
	}

	private static int info(CommandContext<ServerCommandSource> context) {
		String id = StringArgumentType.getString(context, "id");
		return DebugEnergyRegistry.get(id)
				.map(buffer -> send(context, Text.translatable("commands.modern_energy.energy.info", id, format(buffer))))
				.orElseGet(() -> error(context, Text.translatable("commands.modern_energy.energy.unknown", id)));
	}

	private static int operate(CommandContext<ServerCommandSource> context, EnergyOperation operation, boolean simulate) {
		String id = StringArgumentType.getString(context, "id");
		long amount = LongArgumentType.getLong(context, "amount");
		EnergyAction action = simulate ? EnergyAction.SIMULATE : EnergyAction.EXECUTE;

		return DebugEnergyRegistry.get(id)
				.map(buffer -> {
					Text message = switch (operation) {
						case INSERT -> {
							long changed = buffer.insert(amount, action);
							yield Text.translatable("commands.modern_energy.energy.insert",
									prefix(simulate), EnergyUnits.format(changed), id, format(buffer));
						}
						case EXTRACT -> {
							long changed = buffer.extract(amount, action);
							yield Text.translatable("commands.modern_energy.energy.extract",
									prefix(simulate), EnergyUnits.format(changed), id, format(buffer));
						}
						case CONSUME -> {
							boolean consumed = buffer.consume(amount, action);
							yield consumed
									? Text.translatable("commands.modern_energy.energy.consume",
											prefix(simulate), EnergyUnits.format(amount), id, format(buffer))
									: Text.translatable("commands.modern_energy.energy.consume.fail",
											prefix(simulate), EnergyUnits.format(amount), id, format(buffer));
						}
					};

					return send(context, message);
				})
				.orElseGet(() -> error(context, Text.translatable("commands.modern_energy.energy.unknown", id)));
	}

	private static int setRate(CommandContext<ServerCommandSource> context) {
		String id = StringArgumentType.getString(context, "id");
		long maxInsert = LongArgumentType.getLong(context, "maxInsert");
		long maxExtract = LongArgumentType.getLong(context, "maxExtract");

		return DebugEnergyRegistry.get(id)
				.map(buffer -> {
					buffer.setTransferRates(maxInsert, maxExtract);
					return send(context, Text.translatable("commands.modern_energy.energy.set_rate", id, format(buffer)));
				})
				.orElseGet(() -> error(context, Text.translatable("commands.modern_energy.energy.unknown", id)));
	}

	private static int setElectrical(CommandContext<ServerCommandSource> context, boolean applyRate) {
		String id = StringArgumentType.getString(context, "id");
		double voltage = DoubleArgumentType.getDouble(context, "voltage");
		double resistance = DoubleArgumentType.getDouble(context, "resistance");
		ElectricalProperties properties = ElectricalProperties.fromVoltageAndResistance(voltage, resistance);

		return DebugEnergyRegistry.get(id)
				.map(buffer -> {
					buffer.setElectricalProperties(properties, applyRate);
					Text mode = Text.translatable(applyRate
							? "commands.modern_energy.energy.set_electrical.applied"
							: "commands.modern_energy.energy.set_electrical.metadata");
					return send(context, Text.translatable("commands.modern_energy.energy.set_electrical",
							id, formatElectrical(properties), mode, format(buffer)));
				})
				.orElseGet(() -> error(context, Text.translatable("commands.modern_energy.energy.unknown", id)));
	}

	private static int transfer(CommandContext<ServerCommandSource> context, boolean simulate) {
		String sourceId = StringArgumentType.getString(context, "source");
		String targetId = StringArgumentType.getString(context, "target");
		long amount = LongArgumentType.getLong(context, "amount");

		if (sourceId.equals(targetId)) {
			return error(context, Text.translatable("commands.modern_energy.energy.same_buffer"));
		}

		EnergyBuffer source = DebugEnergyRegistry.get(sourceId).orElse(null);
		EnergyBuffer target = DebugEnergyRegistry.get(targetId).orElse(null);

		if (source == null) {
			return error(context, Text.translatable("commands.modern_energy.energy.unknown_source", sourceId));
		}
		if (target == null) {
			return error(context, Text.translatable("commands.modern_energy.energy.unknown_target", targetId));
		}

		long moved = EnergyTransfer.transfer(source, target, amount, simulate ? EnergyAction.SIMULATE : EnergyAction.EXECUTE);
		return send(context, Text.translatable("commands.modern_energy.energy.transfer",
				prefix(simulate), EnergyUnits.format(moved), sourceId, targetId, format(source), format(target)));
	}

	private static int clearAll(CommandContext<ServerCommandSource> context) {
		int count = DebugEnergyRegistry.clear();
		return send(context, Text.translatable("commands.modern_energy.energy.clear.all", count));
	}

	private static int clearOne(CommandContext<ServerCommandSource> context) {
		String id = StringArgumentType.getString(context, "id");
		if (DebugEnergyRegistry.remove(id)) {
			return send(context, Text.translatable("commands.modern_energy.energy.clear.one", id));
		}
		return error(context, Text.translatable("commands.modern_energy.energy.unknown", id));
	}

	private static int help(CommandContext<ServerCommandSource> context) {
		return send(context, Text.translatable("commands.modern_energy.energy.help"));
	}

	private static Text format(EnergyStorage storage) {
		Text base = Text.translatable("commands.modern_energy.energy.format",
				EnergyUnits.format(storage.getEnergy()),
				EnergyUnits.format(storage.getCapacity()),
				EnergyUnits.format(storage.getMaxInsert()),
				EnergyUnits.format(storage.getMaxExtract()));

		if (storage instanceof ElectricalEnergyStorage electricalStorage) {
			return Text.translatable("commands.modern_energy.energy.format.electrical",
					base, formatElectrical(electricalStorage.getElectricalProperties()));
		}

		return base;
	}

	private static Text formatElectrical(ElectricalProperties properties) {
		return Text.translatable("commands.modern_energy.energy.electrical",
				ElectricalUnits.formatVoltage(properties.voltage()),
				ElectricalUnits.formatCurrent(properties.current()),
				ElectricalUnits.formatResistance(properties.resistance()),
				ElectricalUnits.formatFrequency(properties.frequency()),
				ElectricalUnits.formatPower(properties.power()));
	}

	private static Text prefix(boolean simulate) {
		return simulate
				? Text.translatable("commands.modern_energy.energy.simulated")
				: Text.empty();
	}

	private static long optionalLong(CommandContext<ServerCommandSource> context, String name, long fallback) {
		try {
			return LongArgumentType.getLong(context, name);
		} catch (IllegalArgumentException ignored) {
			return fallback;
		}
	}

	private static int send(CommandContext<ServerCommandSource> context, Text message) {
		context.getSource().sendFeedback(() -> message, false);
		return Command.SINGLE_SUCCESS;
	}

	private static int error(CommandContext<ServerCommandSource> context, Text message) {
		context.getSource().sendError(message);
		return 0;
	}

	private enum EnergyOperation {
		INSERT,
		EXTRACT,
		CONSUME
	}
}
