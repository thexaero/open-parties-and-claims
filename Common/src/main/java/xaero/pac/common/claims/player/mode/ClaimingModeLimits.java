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

package xaero.pac.common.claims.player.mode;

import xaero.pac.common.claims.player.mode.api.IClaimingModeAPI;

public final class ClaimingModeLimits {

	public final ClaimingMode mode;
	public final int claimCount;
	public final int forceloadCount;
	public final int claimLimit;
	public final int forceloadLimit;

	public ClaimingModeLimits(IClaimingModeAPI mode, int claimCount, int forceloadCount, int claimLimit, int forceloadLimit) {
		this((ClaimingMode) mode, claimCount, forceloadCount, claimLimit, forceloadLimit);
	}

	public ClaimingModeLimits(ClaimingMode mode, int claimCount, int forceloadCount, int claimLimit, int forceloadLimit) {
		this.mode = mode;
		this.claimCount = claimCount;
		this.forceloadCount = forceloadCount;
		this.claimLimit = claimLimit;
		this.forceloadLimit = forceloadLimit;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) return false;
		ClaimingModeLimits that = (ClaimingModeLimits) o;
		return claimCount == that.claimCount && forceloadCount == that.forceloadCount &&
				claimLimit == that.claimLimit && forceloadLimit == that.forceloadLimit && mode == that.mode;
	}

}
