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

package xaero.pac.common.claims.tracker;

import net.minecraft.resources.ResourceLocation;
import xaero.pac.common.claims.player.api.IPlayerChunkClaimAPI;
import xaero.pac.common.claims.tracker.api.IClaimsManagerListenerAPI;

import java.util.Set;

public class ClaimsManagerTracker implements IClaimsManagerTracker {
	
	private Set<IClaimsManagerListenerAPI> listeners;

	public ClaimsManagerTracker(Set<IClaimsManagerListenerAPI> listeners) {
		super();
		this.listeners = listeners;
	}

	@Override
	public void register(IClaimsManagerListenerAPI listener) {
		listeners.add(listener);
	}

	public void onWholeRegionChange(ResourceLocation dimension, int regionX, int regionZ) {
		for(IClaimsManagerListenerAPI listener : listeners)
			listener.onWholeRegionChange(dimension, regionX, regionZ);
	}
	
	public void onChunkChange(ResourceLocation dimension, int chunkX, int chunkZ, IPlayerChunkClaimAPI claim) {
		for(IClaimsManagerListenerAPI listener : listeners)
			listener.onChunkChange(dimension, chunkX, chunkZ, claim);
	}

	public void onDimensionChange(ResourceLocation dimension) {
		for(IClaimsManagerListenerAPI listener : listeners)
			listener.onDimensionChange(dimension);
	}

}
