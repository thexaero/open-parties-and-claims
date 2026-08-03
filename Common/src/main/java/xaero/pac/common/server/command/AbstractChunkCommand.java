/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2026, Xaero <xaero1996@gmail.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of version 3 of the GNU Lesser General Public License
 * (LGPL-3.0-only) as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received copies of the GNU Lesser General Public License
 * and the GNU General Public License along with this program.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package xaero.pac.common.server.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.commands.arguments.coordinates.ColumnPosArgument;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.function.Predicate;

public abstract class AbstractChunkCommand {

	private final String featureLiteral;
	private final boolean apply;
	private final boolean canBeImpersonated;
	private final String prefix;
	private final String applyLiteral;
	private final String unapplyLiteral;
	private final String subArgumentLiteral;
	private final String subArgumentName;

	protected AbstractChunkCommand(
			String featureLiteral,
			boolean apply,
			boolean canBeImpersonated,
			String prefix,
			String applyLiteral,
			String unapplyLiteral,
			String subArgumentLiteral,
			String subArgumentName
	) {
		this.featureLiteral = featureLiteral;
		this.apply = apply;
		this.canBeImpersonated = canBeImpersonated;
		this.prefix = prefix;
		this.applyLiteral = applyLiteral;
		this.unapplyLiteral = unapplyLiteral;
		this.subArgumentLiteral = subArgumentLiteral;
		this.subArgumentName = subArgumentName;
	}

	public void register(CommandDispatcher<CommandSourceStack> dispatcher, Commands.CommandSelection environment) {
		Predicate<CommandSourceStack> requirement = getRequirement();
		registerCommands(false, false, requirement, dispatcher, environment);
		if(subArgumentLiteral != null)
			registerCommands(false, true, requirement, dispatcher, environment);
		if(canBeImpersonated) {
			registerCommands(true, false, requirement, dispatcher, environment);
			if(subArgumentLiteral != null)
				registerCommands(true, true, requirement, dispatcher, environment);
		}
	}

	private void registerCommands(boolean another, boolean subArgument, Predicate<CommandSourceStack> requirement, CommandDispatcher<CommandSourceStack> dispatcher, Commands.CommandSelection environment){
		String actionLiteral = apply ? applyLiteral : unapplyLiteral;

		Command<CommandSourceStack> defaultExecutor = createChunkCommand(
				apply, another,
				false);
		registerCommand(dispatcher, actionLiteral, null, defaultExecutor, requirement, another, subArgument);

		ArgumentBuilder<CommandSourceStack, ?> mainPart = Commands.argument("block-pos", ColumnPosArgument.columnPos())
				.executes(createChunkCommand(
						apply, another,
						false));
		registerCommand(dispatcher, actionLiteral, mainPart, null, requirement, another, subArgument);

		mainPart = Commands.literal("anyway").requires(source -> source.hasPermission(2))
				.executes(createChunkCommand(
						apply, another,
						true));
		registerCommand(dispatcher, actionLiteral, mainPart, null, requirement, another, subArgument);

		mainPart = Commands.argument("block-pos", ColumnPosArgument.columnPos())
				.then(Commands.literal("anyway").requires(source -> source.hasPermission(2))
				.executes(createChunkCommand(
						apply, another,
						true))
		);
		registerCommand(dispatcher, actionLiteral, mainPart, null, requirement, another, subArgument);

		mainPart = Commands.literal("in")
				.then(Commands.argument("dimension", DimensionArgument.dimension())
				.then(Commands.argument("block-pos", ColumnPosArgument.columnPos())
						.executes(createChunkCommand(
						apply, another,
						false)))
		);
		registerCommand(dispatcher, actionLiteral, mainPart, null, requirement, another, subArgument);

		mainPart = Commands.literal("in")
				.then(Commands.argument("dimension", DimensionArgument.dimension())
				.then(Commands.argument("block-pos", ColumnPosArgument.columnPos())
				.then(Commands.literal("anyway").requires(source -> source.hasPermission(2))
						.executes(createChunkCommand(
						apply, another,
						true))))
		);
		registerCommand(dispatcher, actionLiteral, mainPart, null, requirement, another, subArgument);
	}

	private void registerCommand(
			CommandDispatcher<CommandSourceStack> dispatcher,
			String actionLiteral,
			ArgumentBuilder<CommandSourceStack, ?> mainPart,
			Command<CommandSourceStack> directExecutor,
			Predicate<CommandSourceStack> requirement,
			boolean another,
			boolean subArgument
	){
		ArgumentBuilder<CommandSourceStack, ?> command = Commands.literal(actionLiteral).requires(requirement);//the post-prefix command should also have the requirement because another command might allow the prefix
		ArgumentBuilder<CommandSourceStack, ?> mainPartParent = command;
		ArgumentBuilder<CommandSourceStack, ?> playerArgument = null;

		if(another)
			mainPartParent = playerArgument = Commands.argument("player", GameProfileArgument.gameProfile());
		if(subArgument) {
			RequiredArgumentBuilder<CommandSourceStack, ?> subArgumentNode = Commands.argument(subArgumentName, StringArgumentType.word());
			SuggestionProvider<CommandSourceStack> subArgumentSuggestions = getSubArgumentSuggestions(another);
			if(subArgumentSuggestions != null)
				subArgumentNode.suggests(subArgumentSuggestions);
			mainPartParent = subArgumentNode;
		}

		if(mainPart != null)
			mainPartParent.then(mainPart);
		else if(directExecutor != null)
			mainPartParent.executes(directExecutor);

		ArgumentBuilder<CommandSourceStack, ?> withPlayerAndSubArgumentsBuilder = mainPartParent;
		if(subArgument) {
			withPlayerAndSubArgumentsBuilder = Commands.literal(subArgumentLiteral).then(withPlayerAndSubArgumentsBuilder);
			if(another)
				withPlayerAndSubArgumentsBuilder = playerArgument.then(withPlayerAndSubArgumentsBuilder);
		}
		if(another)
			withPlayerAndSubArgumentsBuilder = Commands.literal("as").requires(getImpersonationRequirement())
					.then(withPlayerAndSubArgumentsBuilder);

		if(withPlayerAndSubArgumentsBuilder != command)
			command = command.then(withPlayerAndSubArgumentsBuilder);
		ArgumentBuilder<CommandSourceStack, ?> prefixedCommand = prefix == null ? command :
				Commands.literal(prefix).then(command);
		LiteralArgumentBuilder<CommandSourceStack> fullCommand = Commands.literal(featureLiteral)
				.requires(context -> getFeatureConfigOption().get())
				.then(prefixedCommand.requires(requirement));
		dispatcher.register(fullCommand);
	}

	protected abstract ForgeConfigSpec.BooleanValue getFeatureConfigOption();
	protected abstract Predicate<CommandSourceStack> getImpersonationRequirement();
	protected abstract Predicate<CommandSourceStack> getRequirement();
	protected SuggestionProvider<CommandSourceStack> getSubArgumentSuggestions(boolean another){
		return null;
	}
	protected abstract Command<CommandSourceStack> createChunkCommand(
			boolean shouldApply,
			boolean another,
			boolean opForce
	);

}
