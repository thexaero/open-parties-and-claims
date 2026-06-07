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

package xaero.pac.client.claims.player.mode;

import xaero.pac.common.claims.player.mode.ClaimingMode;
import xaero.pac.common.claims.player.mode.ClaimingModeLimits;
import xaero.pac.common.claims.player.mode.ClaimingModeSubInfo;
import xaero.pac.common.claims.player.mode.api.IClaimingModeAPI;
import xaero.pac.common.server.player.config.sub.PlayerSubConfig;

public class ClientClaimingModeInfo {

	private final ClaimingMode mode;
	private ClaimingModeLimits limits;
	private ClaimingModeSubInfo subInfo;

	public ClientClaimingModeInfo(IClaimingModeAPI mode) {
		this((ClaimingMode) mode);
	}

	public ClientClaimingModeInfo(ClaimingMode mode) {
		this.mode = mode;
	}

	public void setLimits(ClaimingModeLimits limits) {
		this.limits = limits;
	}

	public void setSubInfo(ClaimingModeSubInfo subInfo) {
		this.subInfo = subInfo;
	}

	public ClaimingModeLimits getLimits() {
		return limits;
	}

	public ClaimingModeSubInfo getSubInfo() {
		return subInfo;
	}

	public void reset() {
		subInfo = new ClaimingModeSubInfo(mode, -1, PlayerSubConfig.MAIN_SUB_ID);
		limits = null;
	}

}
