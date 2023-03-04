/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2022-2023, Xaero <xaero1996@gmail.com> and contributors
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

import javax.annotation.Nonnull;

/**
 * All possible player config types
 */
public enum PlayerConfigType {

	/** Server claims config */
	SERVER(new TranslatableComponent("gui.xaero_pac_config_type_server")),

	/** Expired claims config */
	EXPIRED(new TranslatableComponent("gui.xaero_pac_config_type_expired")),

	/** Wilderness config */
	WILDERNESS(new TranslatableComponent("gui.xaero_pac_config_type_wilderness")),

	/** The default player config */
	DEFAULT_PLAYER(new TranslatableComponent("gui.xaero_pac_config_type_default_player")),

	/** A player config */
	PLAYER(new TranslatableComponent("gui.xaero_pac_config_type_player"));

	private final Component name;

	PlayerConfigType(Component name){
		this.name = name;
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
}
