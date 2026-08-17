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

package xaero.pac.common.server.claims.action.listener.api;

import javax.annotation.Nonnull;

/**
 * API for the claim action listener manager
 */
public interface IClaimActionListenerManagerAPI {

	/**
	 * Registers a new claim action listener.
	 * <p>
	 * Claim action listeners can be used to override whether a player is allowed to perform specific claiming actions
	 * and react to successful claiming actions. It is mainly meant for addon mods that implement a cost/payment system
	 * for claiming but can be used for other things as well.
	 * <p>
	 * You can create your own listener by implementing the {@link IClaimActionListenerAPI} interface.
	 *
	 * @param listener the listener to register, not null
	 */
	void register(@Nonnull IClaimActionListenerAPI listener);

}
