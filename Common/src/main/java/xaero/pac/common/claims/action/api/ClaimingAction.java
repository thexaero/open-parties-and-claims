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
	CLAIM(ClaimResult.Type.SUCCESSFUL_CLAIM, false, false, true),
	/**
	 * Unclaiming a chunk
	 */
	UNCLAIM(ClaimResult.Type.SUCCESSFUL_UNCLAIM, false, false, true),
	/**
	 * Marking a claim for forceloading
	 */
	FORCELOAD(ClaimResult.Type.SUCCESSFUL_FORCELOAD, false, false, true),
	/**
	 * Unmarking a claim for forceloading
	 */
	UNFORCELOAD(ClaimResult.Type.SUCCESSFUL_UNFORCELOAD, false, false, true),
	/**
	 * Unclaiming a chunk if it's not anchored
	 */
	UNCLAIM_UNANCHORED(ClaimResult.Type.SUCCESSFUL_UNCLAIM, true, true, true),
	/**
	 * Adding a claiming anchor
	 */
	ANCHOR(ClaimResult.Type.SUCCESSFUL_ANCHOR, false, false, false),
	/**
	 * Removing a claiming anchor
	 */
	UNANCHOR(ClaimResult.Type.BEGAN_UNANCHOR, false, false, false);

	private final ClaimResult.Type successType;
	private final boolean uninterruptible;
	private final boolean alwaysChecksAnchors;
	private final boolean canAffectArea;

	ClaimingAction(ClaimResult.Type successType, boolean uninterruptible, boolean alwaysChecksAnchors, boolean canAffectArea) {
		this.successType = successType;
		this.uninterruptible = uninterruptible;
		this.alwaysChecksAnchors = alwaysChecksAnchors;
		this.canAffectArea = canAffectArea;
	}

	/**
	 * Gets the result type that is used when this type of action succeeds.
	 *
	 * @return the success result type, not null
	 */
	public ClaimResult.Type getSuccessType() {
		return successType;
	}

	/**
	 * Gets whether this claiming action is uninterruptible with the interrupt command.
	 *
	 * @return true if it's uninterruptible, otherwise false
	 */
	public boolean isUninterruptible() {
		return uninterruptible;
	}

	/**
	 * Gets whether this action always checks anchor ranges, even when used forcefully.
	 *
	 * @return true if this action always checks anchor ranges, otherwise false
	 */
	public boolean getAlwaysChecksAnchors() {
		return alwaysChecksAnchors;
	}

	/**
	 * Gets whether this action can be used to affect an area larger than 1 chunk.
	 *
	 * @return true if this action can be used to affect an area, otherwise false
	 */
	public boolean canAffectArea() {
		return canAffectArea;
	}

}
