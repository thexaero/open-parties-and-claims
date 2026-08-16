/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2022-2026, Xaero <xaero1996@gmail.com> and contributors
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

package xaero.pac.common.server.player.config.api;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import xaero.pac.common.server.command.ConfigCommandUtil;
import xaero.pac.common.server.player.config.api.v2.IPlayerConfigOptionSpecAPI;
import xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Predicate;

/**
 * All possible player config types
 */
public enum PlayerConfigType {

	/** Server claims config */
	SERVER(
			Component.translatable("gui.xaero_pac_config_type_server"),
			PlayerConfigOptions.USED_SERVER_SUBCLAIM,
			true,
			PlayerSubConfigType.SUBCLAIM,
			"server-claims-config",
			sourceStack -> true,
			sourceStack -> sourceStack.hasPermission(Commands.LEVEL_GAMEMASTERS),
			true
	),

	/** Expired claims config */
	EXPIRED(
			Component.translatable("gui.xaero_pac_config_type_expired"),
			null,
			true,
			PlayerSubConfigType.DIMENSION,
			"expired-claims-config",
			sourceStack -> sourceStack.hasPermission(Commands.LEVEL_GAMEMASTERS),
			sourceStack -> sourceStack.hasPermission(Commands.LEVEL_GAMEMASTERS),
			false
	),

	/** Wilderness config */
	WILDERNESS(
			Component.translatable("gui.xaero_pac_config_type_wilderness"),
			null,
			true,
			PlayerSubConfigType.DIMENSION,
			"wilderness-config",
			sourceStack -> sourceStack.hasPermission(Commands.LEVEL_GAMEMASTERS),
			sourceStack -> sourceStack.hasPermission(Commands.LEVEL_GAMEMASTERS),
			false
	),

	/** The default player config */
	DEFAULT_PLAYER(
			Component.translatable("gui.xaero_pac_config_type_default_player"),
			null,
			true,
			PlayerSubConfigType.NONE,
			"default-player-config",
			sourceStack -> sourceStack.hasPermission(Commands.LEVEL_GAMEMASTERS),
			sourceStack -> sourceStack.hasPermission(Commands.LEVEL_GAMEMASTERS),
			false
	),

	/** A player config */
	PLAYER(
			Component.translatable("gui.xaero_pac_config_type_player"),
			PlayerConfigOptions.USED_SUBCLAIM,
			false,
			PlayerSubConfigType.SUBCLAIM,
			"player-config",
			sourceStack -> true,
			sourceStack -> true,
			false
	),

	/** Party claims config */
	PARTY_CLAIMS(
			Component.translatable("gui.xaero_pac_config_type_party_claims"),
			PlayerConfigOptions.USED_PARTY_SUBCLAIM,
			false,
			PlayerSubConfigType.SUBCLAIM,
			"party-claims-config",
			ConfigCommandUtil.getPartyClaimsRequirement(false),
			ConfigCommandUtil.getPartyClaimsRequirement(true),
			true
	);

	private final Component name;
	private final IPlayerConfigOptionSpecAPI<String> subClaimOption;
	private final boolean global;
	private final PlayerSubConfigType subConfigType;
	private final String commandPrefix;
	private final Predicate<CommandSourceStack> readCommandRequirement;
	private final Predicate<CommandSourceStack> writeCommandRequirement;
	private final boolean readAndWriteReqsDiffer;

	PlayerConfigType(
			Component name,
			IPlayerConfigOptionSpecAPI<String> subClaimOption,
			boolean global,
			PlayerSubConfigType subConfigType,
			String commandPrefix,
			Predicate<CommandSourceStack> readCommandRequirement,
			Predicate<CommandSourceStack> writeCommandRequirement,
			boolean readAndWriteReqsDiffer
	){
		this.name = name;
		this.subClaimOption = subClaimOption;
		this.global = global;
		this.subConfigType = subConfigType;
		this.commandPrefix = commandPrefix;
		this.readCommandRequirement = readCommandRequirement;
		this.writeCommandRequirement = writeCommandRequirement;
		this.readAndWriteReqsDiffer = readAndWriteReqsDiffer;
	}

	/**
	 * Gets the display name of the config type.
	 *
	 * @return the display name of the config type, not null
	 */
	@Nonnull
	public Component getName() {
		return name;
	}

	/**
	 * Gets the player config option used for storing the sub-config ID currently used for claiming for a config of
	 * this type.
	 *
	 * @return the player config option used for storing the sub-config ID used for claiming, can be null
	 */
	@Nullable
	public IPlayerConfigOptionSpecAPI<String> getSubClaimOption() {
		return subClaimOption;
	}

	/**
	 * Gets whether this config type corresponds to a global config, meaning it's the same for every player.
	 *
	 * @return true if this is a global config type, otherwise false
	 */
	public boolean isGlobal() {
		return global;
	}

	/**
	 * Gets the type of sub-configs that this config type uses.
	 * <p>
	 * All sub-config types can be found in {@link PlayerSubConfigType}.
	 *
	 * @return the sub-config type, not null
	 */
	@Nonnull
	public PlayerSubConfigType getSubConfigType() {
		return subConfigType;
	}

	/**
	 * Gets whether configs of this type support sub-configs.
	 *
	 * @return true if this config type supports sub-configs, otherwise false
	 */
	public boolean supportsSubConfigs() {
		return subConfigType != PlayerSubConfigType.NONE;
	}

	/**
	 * Gets whether configs of this type have dimension-based sub-configs, e.g. wilderness.
	 *
	 * @return true if this config type has dimension sub-configs, otherwise false
	 */
	public boolean hasDimensionSubConfigs() {
		return subConfigType == PlayerSubConfigType.DIMENSION;
	}

	/**
	 * The String literal prefix used for commands relating to this config type.
	 *
	 * @return the command prefix, not null
	 */
	public String getCommandPrefix() {
		return commandPrefix;
	}

	/**
	 * Gets the requirement for read-type commands related to this config type. Such commands don't modify anything.
	 *
	 * @return the read-type command requirement, not null
	 */
	public Predicate<CommandSourceStack> getReadCommandRequirement() {
		return readCommandRequirement;
	}

	/**
	 * Gets the requirement for write-type commands related to this config type. Such commands can modify data.
	 *
	 * @return the write-type command requirement, not null
	 */
	public Predicate<CommandSourceStack> getWriteCommandRequirement() {
		return writeCommandRequirement;
	}

	/**
	 * Gets whether the requirements for read and write command are different for this config type.
	 *
	 * @return true if the read and write requirements are different, otherwise false
	 */
	public boolean readAndWriteReqsDiffer() {
		return readAndWriteReqsDiffer;
	}

}
