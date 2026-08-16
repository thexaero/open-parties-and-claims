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

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import xaero.pac.common.server.player.config.api.v2.IPlayerConfigOptionSpecAPI;
import xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * All possible player config types
 */
public enum PlayerConfigType {

	/** Server claims config */
	SERVER(Component.translatable("gui.xaero_pac_config_type_server"), PlayerConfigOptions.USED_SERVER_SUBCLAIM, true),

	/** Expired claims config */
	EXPIRED(Component.translatable("gui.xaero_pac_config_type_expired"), null, true),

	/** Wilderness config */
	WILDERNESS(Component.translatable("gui.xaero_pac_config_type_wilderness"), null, true),

	/** The default player config */
	DEFAULT_PLAYER(Component.translatable("gui.xaero_pac_config_type_default_player"), null, true),

	/** A player config */
	PLAYER(Component.translatable("gui.xaero_pac_config_type_player"), PlayerConfigOptions.USED_SUBCLAIM, false),

	/** Party claims config */
	PARTY_CLAIMS(Component.translatable("gui.xaero_pac_config_type_party_claims"), PlayerConfigOptions.USED_PARTY_SUBCLAIM, false);

	private final Component name;
	private final IPlayerConfigOptionSpecAPI<String> subClaimOption;
	private final boolean global;

	PlayerConfigType(Component name, IPlayerConfigOptionSpecAPI<String> subClaimOption, boolean global){
		this.name = name;
		this.subClaimOption = subClaimOption;
		this.global = global;
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

}
