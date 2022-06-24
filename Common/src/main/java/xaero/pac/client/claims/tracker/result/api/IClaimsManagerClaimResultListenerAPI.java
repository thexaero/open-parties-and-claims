/*
 *     Open Parties and Claims - adds chunk claims and player parties to Minecraft
 *     Copyright (C) 2022, Xaero <xaero1996@gmail.com> and contributors
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of version 3 of the GNU Lesser General Public License
 *     (LGPL-3.0-only) as published by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received copies of the GNU Lesser General Public License
 *     and the GNU General Public License along with this program.
 *     If not, see <https://www.gnu.org/licenses/>.
 */

package xaero.pac.client.claims.tracker.result.api;

import xaero.pac.common.claims.result.api.AreaClaimResult;

import javax.annotation.Nonnull;

/**
 * The interface to be implemented by all claim result listeners
 * <p>
 * Register your listeners in {@link IClaimsManagerClaimResultTrackerAPI}.
 */
public interface IClaimsManagerClaimResultListenerAPI {

	/**
	 * Called when the client receives a claim result from the server.
	 * <p>
	 * Override this method and register the listener in {@link IClaimsManagerClaimResultTrackerAPI} to handle claim results
	 * however you'd like.
	 *
	 * @param result  the area claim result, not null
	 */
	void onClaimResult(@Nonnull AreaClaimResult result);

}
