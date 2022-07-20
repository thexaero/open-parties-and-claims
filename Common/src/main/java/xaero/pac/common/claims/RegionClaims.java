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

import net.minecraft.resources.ResourceLocation;
import xaero.pac.common.claims.player.PlayerChunkClaim;
import xaero.pac.common.claims.player.PlayerClaimInfo;
import xaero.pac.common.claims.player.PlayerClaimInfoManager;
import xaero.pac.common.claims.storage.RegionClaimsPaletteStorage;
import xaero.pac.common.server.player.config.IPlayerConfigManager;
import xaero.pac.common.util.linked.ILinkedChainNode;

import javax.annotation.Nullable;
import java.util.Objects;

public abstract class RegionClaims
<
	M extends PlayerClaimInfoManager<?, M>,
	WRC extends RegionClaims<M, WRC>
> implements IRegionClaims, ILinkedChainNode<WRC> {//reflects what's in PlayerClaimInfoManager
	
	protected final ResourceLocation dimension;
	private final int x;
	private final int z;
	protected final RegionClaimsPaletteStorage storage;
	private boolean destroyed;
	private WRC nextInChain;
	private WRC previousInChain;
	
	public RegionClaims(ResourceLocation dimension, int x, int z, 
			RegionClaimsPaletteStorage storage) {
		if(storage == null)
			throw new IllegalArgumentException();
		this.storage = storage;
		this.dimension = dimension;
		this.x = x;
		this.z = z;
	}

	@Override
	public void onDestroyed() {
		this.destroyed = true;
	}

	@Override
	public boolean isDestroyed() {
		return destroyed;
	}

	@Override
	public WRC getNext() {
		return nextInChain;
	}

	@Override
	public WRC getPrevious() {
		return previousInChain;
	}

	@Override
	public void setNext(WRC nextInChain) {
		this.nextInChain = nextInChain;
	}

	@Override
	public void setPrevious(WRC previousInChain) {
		this.previousInChain = previousInChain;
	}

	public static int getIndex(int x, int z) {
		return (x << 5) | z;
	}

	@Nullable
	@Override
	public PlayerChunkClaim get(int x, int z) {
		return storage.get(x, z);
	}
	
	protected void set(int x, int z, PlayerChunkClaim value) {
		storage.set(x, z, value);
	}
	
	public RegionClaimsPaletteStorage getStorage() {
		return storage;
	}

	public PlayerChunkClaim claim(int x, int z, PlayerChunkClaim claim, M playerClaimsManager, IPlayerConfigManager<?> configManager) {
		PlayerChunkClaim currentClaim = get(x & 31, z & 31);
		if(onClaimSet(x, z, currentClaim, claim, playerClaimsManager, configManager))
			set(x & 31, z & 31, claim);
		return claim;
	}

	protected boolean onClaimSet(int x, int z, PlayerChunkClaim currentClaim, PlayerChunkClaim newClaim, M playerClaimsManager, IPlayerConfigManager<?> configManager){
		if(!Objects.equals(currentClaim, newClaim)) {
			PlayerClaimInfo<?,?> currentPlayerInfo = currentClaim == null ? null : playerClaimsManager.getInfo(currentClaim.getPlayerId());
			PlayerClaimInfo<?,?> newPlayerInfo = newClaim == null ? null : playerClaimsManager.getInfo(newClaim.getPlayerId());

			if (currentPlayerInfo != null)
				currentPlayerInfo.onUnclaim(configManager, dimension, x, z);
			if (newPlayerInfo != null)
				newPlayerInfo.onClaim(configManager, dimension, newClaim, x, z);
			return true;
		}
		return false;
	}
	
	@Override
	public int getX() {
		return x;
	}
	
	@Override
	public int getZ() {
		return z;
	}

	public boolean isEmpty() {
		return storage.isEmpty();
	}
	
	@Override
	public String toString() {
		return String.format("[%s, %d, %d]", dimension, x, z);
	}

	public static abstract class Builder
	<
		M extends PlayerClaimInfoManager<?, M>,
		WRC extends RegionClaims<M, WRC>,
		B extends Builder<M, WRC, B>
	> {
		
		protected final B self;
		protected ResourceLocation dimension;
		protected int x;
		protected int z;
		protected RegionClaimsPaletteStorage storage;
		
		@SuppressWarnings("unchecked")
		protected Builder() {
			this.self = (B)this;
		}
		
		public B setDefault() {
			setDimension(null);
			setX(0);
			setZ(0);
			setStorage(null);
			return self;
		}
		
		public B setDimension(ResourceLocation dimension) {
			this.dimension = dimension;
			return self;
		}
		
		public B setX(int x) {
			this.x = x;
			return self;
		}
		
		public B setZ(int z) {
			this.z = z;
			return self;
		}
		
		protected B setStorage(RegionClaimsPaletteStorage storage) {
			this.storage = storage;
			return self;
		}
		
		public RegionClaims<M, WRC> build(){
			if(dimension == null || storage == null)
				throw new IllegalStateException();
			return buildInternally();
		}
		
		protected abstract RegionClaims<M, WRC> buildInternally();
		
	}

}
