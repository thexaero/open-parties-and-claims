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

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.ColumnPosArgument;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.function.Predicate;

public abstract class AbstractChunkCommand {

	private final String featureLiteral;
	private final boolean apply;
	private final String prefix;
	private final String applyLiteral;
	private final String unapplyLiteral;

	protected AbstractChunkCommand(
			String featureLiteral,
			boolean apply,
			String prefix,
			String applyLiteral,
			String unapplyLiteral
	) {
		this.featureLiteral = featureLiteral;
		this.apply = apply;
		this.prefix = prefix;
		this.applyLiteral = applyLiteral;
		this.unapplyLiteral = unapplyLiteral;
	}

	public void register(CommandDispatcher<CommandSourceStack> dispatcher, Commands.CommandSelection environment) {
		Predicate<CommandSourceStack> requirement = getRequirement();
		String actionLiteral = apply ? applyLiteral : unapplyLiteral;

		ArgumentBuilder<CommandSourceStack, ?> mainPart = createChunkCommand(
				Commands.literal(actionLiteral), apply, false
		);
		registerCommand(dispatcher, mainPart, requirement);

		mainPart = Commands.literal(actionLiteral).then(
				createChunkCommand(
						Commands.argument("block-pos", ColumnPosArgument.columnPos()),
						apply, false
				)
		);
		registerCommand(dispatcher, mainPart, requirement);

		mainPart = Commands.literal(actionLiteral).then(
				createChunkCommand(
						Commands.literal("anyway").requires(source -> source.hasPermission(2)),
						apply, true
				)
		);
		registerCommand(dispatcher, mainPart, requirement);

		mainPart = Commands.literal(actionLiteral).then(
				Commands.literal("anyway").requires(source -> source.hasPermission(2))
				.then(createChunkCommand(
						Commands.argument("block-pos", ColumnPosArgument.columnPos()),
						apply, true
				))
		);
		registerCommand(dispatcher, mainPart, requirement);
	}

	private void registerCommand(
			CommandDispatcher<CommandSourceStack> dispatcher,
			ArgumentBuilder<CommandSourceStack, ?> mainPart,
			Predicate<CommandSourceStack> requirement
	){
		ArgumentBuilder<CommandSourceStack, ?> prefixedMainPart = prefix == null ? mainPart :
				Commands.literal(prefix).then(mainPart.requires(requirement));//the main part should also have the requirement because another command might allow the prefix
		LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal(featureLiteral)
				.requires(context -> getFeatureConfigOption().get())
				.then(prefixedMainPart.requires(requirement));
		dispatcher.register(command);
	}

	protected abstract ModConfigSpec.BooleanValue getFeatureConfigOption();
	protected abstract Predicate<CommandSourceStack> getRequirement();
	protected abstract ArgumentBuilder<CommandSourceStack, ?> createChunkCommand(
			ArgumentBuilder<CommandSourceStack, ?> builder,
			boolean shouldApply,
			boolean opForce
	);

}
