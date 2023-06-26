/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2023, Xaero <xaero1996@gmail.com> and contributors
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

package xaero.pac.common.claims.tracker.api;

import javax.annotation.Nonnull;

/**
 * API for registering claims manager listeners.
 */
public interface IClaimsManagerTrackerRegisterAPI {

	/**
	 * Registers a claim change listener.
	 * <p>
	 * You can create one by implementing {@link IClaimsManagerListenerAPI}.
	 * <p>
	 * You are not required to but it is recommended to register listeners, on the
	 * client side, during the OPACClientAddonRegister.EVENT on Fabric or OPACClientAddonRegisterEvent on Forge,
	 * and, on the server side, during the OPACServerAddonRegister.EVENT on Fabric or OPACServerAddonRegisterEvent on Forge.
	 *
	 * @param listener  the listener to register, not null
	 */
	void register(@Nonnull IClaimsManagerListenerAPI listener);

}
