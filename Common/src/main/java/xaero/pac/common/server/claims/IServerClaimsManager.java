/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2022-2025, Xaero <xaero1996@gmail.com> and contributors
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

package xaero.pac.common.server.claims;

import net.minecraft.resources.ResourceLocation;
import xaero.pac.common.claims.IClaimsManager;
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.claims.player.api.IPlayerChunkClaimAPI;
import xaero.pac.common.claims.result.api.ClaimResult;
import xaero.pac.common.server.claims.api.IServerClaimsManagerAPI;
import xaero.pac.common.server.claims.api.IServerDimensionClaimsManagerAPI;
import xaero.pac.common.server.claims.player.IServerPlayerClaimInfo;
import xaero.pac.common.server.claims.player.api.IServerPlayerClaimInfoAPI;
import xaero.pac.common.server.claims.player.task.PlayerClaimReplaceSpreadoutTask;
import xaero.pac.common.server.claims.sync.IClaimsManagerSynchronizer;
import xaero.pac.common.server.task.ServerSpreadoutQueuedTaskHandler;

import javax.annotation.Nonnull;
import java.util.UUID;
import java.util.stream.Stream;

public interface IServerClaimsManager
<
	C extends IPlayerChunkClaim, 
	PCI extends IServerPlayerClaimInfo<?>,
	WCM extends IServerDimensionClaimsManager<?>
> extends IClaimsManager<PCI, WCM>, IServerClaimsManagerAPI {
	//internal API
	
	public IClaimsManagerSynchronizer getClaimsManagerSynchronizer();
	public ServerSpreadoutQueuedTaskHandler<PlayerClaimReplaceSpreadoutTask> getClaimReplaceTaskHandler();
	public ServerClaimsPermissionHandler getPermissionHandler();

	@Nonnull
	@Override
	@SuppressWarnings("unchecked")
	default Stream<IServerDimensionClaimsManagerAPI> getDimensionStream() {
		return (Stream<IServerDimensionClaimsManagerAPI>)(Object)getTypedDimensionStream();
	}

	@Nonnull
	@Override
	@SuppressWarnings("unchecked")
	default Stream<IServerPlayerClaimInfoAPI> getPlayerInfoStream(){
		return (Stream<IServerPlayerClaimInfoAPI>)(Object)getTypedPlayerInfoStream();
	}

	@Nonnull
	public ClaimResult<C> tryToClaimTyped(@Nonnull ResourceLocation dimension, @Nonnull UUID playerId, int subConfigIndex, int fromX, int fromZ, int x, int z, boolean replace);

	@Nonnull
	public ClaimResult<C> tryToUnclaimTyped(@Nonnull ResourceLocation dimension, @Nonnull UUID playerId, int fromX, int fromZ, int x, int z, boolean replace);

	@Nonnull
	public ClaimResult<C> tryToForceloadTyped(@Nonnull ResourceLocation dimension, @Nonnull UUID playerId, int fromX, int fromZ, int x, int z, boolean enable, boolean replace);

	@Nonnull
	@Override
	@SuppressWarnings("unchecked")
	default ClaimResult<IPlayerChunkClaimAPI> tryToClaim(@Nonnull ResourceLocation dimension, @Nonnull UUID playerId, int subConfigIndex, int fromX, int fromZ, int x, int z, boolean replace) {
		return (ClaimResult<IPlayerChunkClaimAPI>)(Object)tryToClaimTyped(dimension, playerId, subConfigIndex, fromX, fromZ, x, z, replace);
	}

	@Nonnull
	@Override
	@SuppressWarnings("unchecked")
	default ClaimResult<IPlayerChunkClaimAPI> tryToUnclaim(@Nonnull ResourceLocation dimension, @Nonnull UUID playerId, int fromX, int fromZ, int x, int z, boolean replace) {
		return (ClaimResult<IPlayerChunkClaimAPI>)(Object)tryToUnclaimTyped(dimension, playerId, fromX, fromZ, x, z, replace);
	}

	@Nonnull
	@Override
	@SuppressWarnings("unchecked")
	default ClaimResult<IPlayerChunkClaimAPI> tryToForceload(@Nonnull ResourceLocation dimension, @Nonnull UUID playerId, int fromX, int fromZ, int x, int z, boolean enable, boolean replace) {
		return (ClaimResult<IPlayerChunkClaimAPI>)(Object)tryToForceloadTyped(dimension, playerId, fromX, fromZ, x, z, enable, replace);
	}

}
