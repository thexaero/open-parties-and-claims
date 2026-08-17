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

package xaero.pac.common.claims.action.api;

import xaero.pac.common.claims.result.api.ClaimResult;

/**
 * All possible types of claiming actions
 */
public enum ClaimingAction {
	/**
	 * Claiming a chunk
	 */
	CLAIM(ClaimResult.Type.SUCCESSFUL_CLAIM),
	/**
	 * Unclaiming a chunk
	 */
	UNCLAIM(ClaimResult.Type.SUCCESSFUL_UNCLAIM),
	/**
	 * Marking a claim for forceloading
	 */
	FORCELOAD(ClaimResult.Type.SUCCESSFUL_FORCELOAD),
	/**
	 * Unmarking a claim for forceloading
	 */
	UNFORCELOAD(ClaimResult.Type.SUCCESSFUL_UNFORCELOAD);

	private final ClaimResult.Type successType;

	ClaimingAction(ClaimResult.Type successType) {
		this.successType = successType;
	}

	/**
	 * Gets the result type that is used when this type of action succeeds.
	 *
	 * @return the success result type, not null
	 */
	public ClaimResult.Type getSuccessType() {
		return successType;
	}

}
