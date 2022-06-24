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

package xaero.pac.common.claims.storage;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.util.Mth;
import net.minecraft.util.SimpleBitStorage;
import xaero.pac.common.claims.player.PlayerChunkClaim;
import xaero.pac.common.server.claims.ServerPlayerChunkClaimHolder;

import java.util.ArrayList;
import java.util.Map;

public class RegionClaimsPaletteStorage {
	
	protected final Map<PlayerChunkClaim, Integer> paletteHelper;
	private final IntList paletteInts;
	protected final ArrayList<ServerPlayerChunkClaimHolder> palette;
	protected SimpleBitStorage storage;
	private boolean constantBits;
	
	public RegionClaimsPaletteStorage(Map<PlayerChunkClaim, Integer> paletteHelper, IntList paletteInts,
			ArrayList<ServerPlayerChunkClaimHolder> palette, SimpleBitStorage storage, boolean constantBits) {
		super();
		if(storage.getSize() != 1024 || constantBits && storage.getBits() != 11 || palette.isEmpty() || palette.get(0) != null)
			throw new IllegalArgumentException();
		this.paletteHelper = paletteHelper;
		this.paletteInts = paletteInts;
		this.palette = palette;
		this.storage = storage;
		this.constantBits = constantBits;
	}
	
	private ServerPlayerChunkClaimHolder getHolder(int x, int z) {
		return palette.get(storage.get(getIndex(x, z)));
	}
	
	public PlayerChunkClaim get(int x, int z) {
		ServerPlayerChunkClaimHolder claimHolder = getHolder(x, z);
		return claimHolder == null ? null : claimHolder.getClaim();
	}

	public void set(int x, int z, PlayerChunkClaim value) {
		ServerPlayerChunkClaimHolder currentHolder = getHolder(x, z);
		ServerPlayerChunkClaimHolder newHolder;
		Integer newPaletteIndex;
		if(value == null) {
			newHolder = null;
			newPaletteIndex = 0;
		} else {
			newPaletteIndex = paletteHelper.get(value);
			if(newPaletteIndex == null) {
				paletteHelper.put(value, newPaletteIndex = palette.size());
				palette.add(newHolder = new ServerPlayerChunkClaimHolder(value, new byte[32]));
				if(paletteInts != null)
					paletteInts.add(value.getSyncIndex());
			} else
				newHolder = palette.get(newPaletteIndex);
		}
		if(newHolder != currentHolder) {
			int index = getIndex(x, z);
			if(currentHolder != null) {
				currentHolder.decrement(x);
				if(currentHolder.getCount() == 0) {
					int currentPaletteIndex = storage.get(index);
					removePaletteElement(currentPaletteIndex);
					if(newPaletteIndex > currentPaletteIndex)
						newPaletteIndex--;
				}
			}
			if(newHolder != null)
				newHolder.increment(x);
			
			ensureSyncableStorageBits();
			storage.set(index, newPaletteIndex);
		}
	}
	
	private void ensureSyncableStorageBits() {
		if(constantBits)
			return;
		int neededBits = Mth.ceillog2(palette.size());
		if(neededBits <= 1)
			neededBits = 1;
		else if(neededBits < 11)
			neededBits = (neededBits + 1) / 2 * 2;//always a multiple of 2 except 1 and 11
		if(storage.getBits() < neededBits || storage.getBits() >= 6 && storage.getBits() / neededBits >= 2 /*used bits are at least 2 times too much*/) {
			SimpleBitStorage newStorage = new SimpleBitStorage(neededBits, 1024);
			int allowedValueLimit = 1 << neededBits;
			SimpleBitStorage oldStorage = this.storage;
			for (int i = 0; i < 1024; i++) {
				int oldValue = oldStorage.get(i);
				if(oldValue < allowedValueLimit)
					newStorage.set(i, oldValue);
			}
			storage = newStorage;
		}
	}
	
	protected void removePaletteElement(int paletteIndex) {
		if(paletteIndex == 0)
			return;

		Map<PlayerChunkClaim, Integer> paletteHelper = this.paletteHelper;
		IntList paletteInts = this.paletteInts;
		ArrayList<ServerPlayerChunkClaimHolder> palette = this.palette;
		SimpleBitStorage storage = this.storage;
		if(paletteIndex < palette.size() - 1) {
			boolean[] fixedStorageColumns = new boolean[32];
			for(int i = paletteIndex + 1; i < palette.size(); i++) {
				ServerPlayerChunkClaimHolder holder = palette.get(i);
				paletteHelper.put(holder.getClaim(), i - 1);
				for(int x = holder.getMinX(); x <= holder.getMaxX(); x++) {
					if(!fixedStorageColumns[x]) {
						fixedStorageColumns[x] = true;
						for(int z = 0; z < 32; z++) {
							int index = getIndex(x, z);
							int storedPaletteIndex = storage.get(index);
							if(storedPaletteIndex > paletteIndex)
								storage.set(index, storedPaletteIndex - 1);
						}
					}
				}
			}
		}
		paletteHelper.remove(palette.remove(paletteIndex).getClaim());
		if(paletteInts != null)
			paletteInts.removeInt(paletteIndex - 1);
	}
	
	public static int getIndex(int x, int z) {
		return (x << 5) | z;
	}
	
	public int[] getPaletteArray(){
		return paletteInts == null ? null : paletteInts.toIntArray();
	}
	
	public int getStorageBits() {
		return storage.getBits();
	}
	
	public long[] getStorageData() {
		return storage.getRaw();
	}
	
	public boolean isEmpty() {
		return palette.size() <= 1;
	}

}
