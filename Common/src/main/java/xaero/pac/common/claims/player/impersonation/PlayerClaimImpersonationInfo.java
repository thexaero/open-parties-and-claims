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

package xaero.pac.common.claims.player.impersonation;

import xaero.pac.common.claims.player.impersonation.api.IPlayerClaimImpersonationInfoAPI;
import xaero.pac.common.claims.player.mode.ClaimingMode;
import xaero.pac.common.claims.player.mode.api.ClaimingModes;
import xaero.pac.common.claims.player.mode.api.IClaimingModeAPI;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class PlayerClaimImpersonationInfo implements IPlayerClaimImpersonationInfoAPI {

	protected UUID playerId;
	protected final Map<ClaimingMode, Integer> subIndices;

	public PlayerClaimImpersonationInfo(UUID playerId) {
		this.playerId = playerId;
		this.subIndices = new HashMap<>();
	}

	@Override
	public UUID getPlayerId() {
		return playerId;
	}

	@Override
	public int getSubIndex(@Nonnull IClaimingModeAPI mode) {
		Integer subIndex = subIndices.get((ClaimingMode) mode);
		return subIndex == null ? -1 : subIndex;
	}

	public void setPlayerId(UUID playerId) {
		this.playerId = playerId;
	}

	public void setSubIndex(IClaimingModeAPI mode, int subIndex) {
		subIndices.put((ClaimingMode) mode, subIndex);
	}

	public Map<ClaimingMode, Integer> getSubIndices() {
		return subIndices;
	}

	public void reset(){
		setPlayerId(null);
		subIndices.clear();
	}

	public SimplePlayerClaimImpersonationInfo createSnapshot(){
		SimplePlayerClaimImpersonationInfo copy = new SimplePlayerClaimImpersonationInfo(getPlayerId());
		subIndices.forEach(copy::setSubIndex);
		for (IClaimingModeAPI claimingMode : ClaimingModes.ALL_IMMUTABLE.values()) {
			if(claimingMode == ClaimingModes.PLAYER || !claimingMode.canBeImpersonated())
				continue;
			UUID claimPlayerId = getClaimPlayerId(claimingMode);
			if(claimPlayerId == null)
				continue;
			copy.setClaimPlayerId(claimingMode, claimPlayerId);
		}
		return copy;
	}

}
