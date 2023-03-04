/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2022-2023, Xaero <xaero1996@gmail.com> and contributors
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

package xaero.pac.common.claims;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import xaero.pac.common.claims.api.IClaimsManagerAPI;
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.claims.player.IPlayerClaimInfo;
import xaero.pac.common.claims.tracker.IClaimsManagerTracker;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IClaimsManager
<
	PCI extends IPlayerClaimInfo<?>,
	WCM extends IDimensionClaimsManager<?>
> extends IClaimsManagerAPI<PCI, WCM> {
	//internal API

	@Nullable
	public IPlayerChunkClaim get(@Nonnull ResourceLocation dimension, int x, int z);

	@Nullable
	public IPlayerChunkClaim get(@Nonnull ResourceLocation dimension, @Nonnull ChunkPos chunkPos);

	@Nullable
	public IPlayerChunkClaim get(@Nonnull ResourceLocation dimension, @Nonnull BlockPos blockPos);

	@Nonnull
	public IClaimsManagerTracker getTracker();
	
}
