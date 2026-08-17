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

package xaero.pac.common.claims.player.mode.api;

import javax.annotation.Nonnull;

/**
 * A claiming mode
 */
public interface IClaimingModeAPI {

	/**
	 * Gets the String ID of the claiming mode.
	 *
	 * @return the String ID, not null
	 */
	@Nonnull
	String getId();

	/**
	 * Checks whether this is a global claiming mode that is exactly the same for every player.
	 * <p>
	 * For example, player and party modes are not global because they depend on the player while server mode is global.
	 *
	 * @return true if this is a global claiming mode, otherwise false
	 */
	boolean isGlobal();

	/**
	 * Checks whether this claiming mode can be used when impersonating another player.
	 * <p>
	 * The use of the claiming mode won't be prevented but the impersonation will be ignored if this method returns false.
	 *
	 * @return true if this claiming mode can be impersonated, otherwise false
	 */
	boolean canBeImpersonated();

}
