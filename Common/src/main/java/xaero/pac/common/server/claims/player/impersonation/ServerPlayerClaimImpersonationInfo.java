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

package xaero.pac.common.server.claims.player.impersonation;

import xaero.pac.common.claims.player.impersonation.PlayerClaimImpersonationInfo;
import xaero.pac.common.claims.player.mode.ClaimingMode;
import xaero.pac.common.claims.player.mode.api.IClaimingModeAPI;
import xaero.pac.common.server.IServerData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public class ServerPlayerClaimImpersonationInfo extends PlayerClaimImpersonationInfo {

	private final IServerData<?, ?> serverData;

	public ServerPlayerClaimImpersonationInfo(
			UUID claimsImpersonatedPlayerId,
			IServerData<?, ?> serverData
	) {
		super(claimsImpersonatedPlayerId);
		this.serverData = serverData;
	}

	@Nullable
	@Override
	public UUID getClaimPlayerId(@Nonnull IClaimingModeAPI claimingModeAPI) {
		UUID impersonatedId = getPlayerId();
		ClaimingMode claimingMode = (ClaimingMode) claimingModeAPI;
		if((claimingMode.isGlobal() || impersonatedId != null) && claimingMode.getForcedUUIDGetter() != null)
			return claimingMode.getForcedUUIDGetter().apply(impersonatedId, serverData.getServerClaimsManager());
		return impersonatedId;
	}

}
