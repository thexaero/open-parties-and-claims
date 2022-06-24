/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2022, Xaero <xaero1996@gmail.com> and contributors
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

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import xaero.pac.common.claims.player.PlayerChunkClaim;
import xaero.pac.common.claims.player.PlayerClaimInfoManager;
import xaero.pac.common.server.player.config.IPlayerConfigManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.stream.Stream;

public abstract class DimensionClaimsManager
<
	M extends PlayerClaimInfoManager<?, M>,
	WRC extends RegionClaims<M>
> implements IDimensionClaimsManager<WRC> {
	
	private final ResourceLocation dimension;
	private final Long2ObjectMap<WRC> claims;
	
	public DimensionClaimsManager(ResourceLocation dimension, Long2ObjectMap<WRC> claims) {
		this.dimension = dimension;
		this.claims = claims;
	}
	
	@Override
	public int getCount() {
		return claims.size();
	}
	
	@Nonnull
	@Override
	public Stream<WRC> getRegionStream(){
		return claims.values().stream();
	}
	
	private long getKey(int x, int z) {
		return PlayerChunkClaim.getLongCoordinatesFor(x, z);
	}

	@Nullable
	@Override
	public WRC getRegion(int x, int z) {
		long key = getKey(x, z);
		return claims.get(key);
	}
	
	private void setRegion(int x, int z, WRC region) {
		long key = getKey(x, z);
		claims.put(key, region);
	}
	
	public PlayerChunkClaim get(int x, int z) {
		WRC region = getRegion(x >> 5, z >> 5);
		if(region == null)
			return null;
		return region.get(x & 31, z & 31);
	}
	
	public PlayerChunkClaim get(ChunkPos chunkPos) {
		return get(chunkPos.x, chunkPos.z);
	}
	
	public PlayerChunkClaim get(BlockPos blockPos) {
		return get(blockPos.getX() >> 4, blockPos.getZ() >> 4);
	}
	
	private WRC ensureRegionForChunk(int chunkX, int chunkZ) {
		int regionX = chunkX >> 5;
		int regionZ = chunkZ >> 5;
		WRC region = getRegion(regionX, regionZ);
		if(region == null)
			setRegion(regionX, regionZ, region = create(dimension, regionX, regionZ)); 
		return region;
	}
	
	private WRC getRegionForChunk(int chunkX, int chunkZ) {
		int regionX = chunkX >> 5;
		int regionZ = chunkZ >> 5;
		return getRegion(regionX, regionZ);
	}
	
	protected abstract WRC create(ResourceLocation dimension, int x, int z);
	
	public PlayerChunkClaim claim(int x, int z, PlayerChunkClaim claim, M playerClaimInfoManager, IPlayerConfigManager<?> configManager) {
		WRC region = ensureRegionForChunk(x, z);
		return region.claim(x, z, claim, playerClaimInfoManager, configManager);
	}
	
	public void unclaim(int x, int z, M playerClaimInfoManager, IPlayerConfigManager<?> configManager) {
		WRC region = getRegionForChunk(x, z);
		if(region != null) {
			region.claim(x, z, null, playerClaimInfoManager, configManager);
			if(region.isEmpty()) {
				region.onRemoved();
				claims.remove(getKey(region.getX(), region.getZ()));
			}
		}
	}
	
	@Nonnull
	@Override
	public ResourceLocation getDimension() {
		return dimension;
	}

}
