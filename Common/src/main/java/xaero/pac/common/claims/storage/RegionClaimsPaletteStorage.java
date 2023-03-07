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

package xaero.pac.common.claims.storage;

import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.util.BitStorage;
import net.minecraft.util.Mth;
import net.minecraft.util.SimpleBitStorage;
import xaero.pac.common.claims.PlayerChunkClaimHolder;
import xaero.pac.common.claims.RegionClaims;
import xaero.pac.common.claims.player.PlayerChunkClaim;

import java.util.ArrayList;

public class RegionClaimsPaletteStorage {
	
	protected final Object2IntMap<PlayerChunkClaim> paletteHelper;
	private final IntList paletteInts;
	protected final ArrayList<PlayerChunkClaimHolder> palette;
	protected BitStorage storage;
	private boolean constantBits;
	private boolean needsHolderRecalculation;
	
	public RegionClaimsPaletteStorage(Object2IntMap<PlayerChunkClaim> paletteHelper, IntList paletteInts,
									  ArrayList<PlayerChunkClaimHolder> palette, BitStorage storage, boolean constantBits) {
		super();
		if(storage.getSize() != 1024 || constantBits && storage.getBits() != 11 || palette.isEmpty() || palette.get(0) != null)
			throw new IllegalArgumentException();
		this.paletteHelper = paletteHelper;
		this.paletteInts = paletteInts;
		this.palette = palette;
		this.storage = storage;
		this.constantBits = constantBits;
	}
	
	private PlayerChunkClaimHolder getHolder(int x, int z) {
		return palette.get(storage.get(getIndex(x, z)));
	}
	
	public PlayerChunkClaim get(int x, int z) {
		PlayerChunkClaimHolder claimHolder = getHolder(x, z);
		return claimHolder == null ? null : claimHolder.getClaim();
	}

	public void set(int x, int z, PlayerChunkClaim value, RegionClaims<?,?> region) {
		if(needsHolderRecalculation)
			recalculateHolders();
		PlayerChunkClaimHolder currentHolder = getHolder(x, z);
		PlayerChunkClaimHolder newHolder;
		int newPaletteIndex;
		if(value == null) {
			newHolder = null;
			newPaletteIndex = 0;
		} else {
			newPaletteIndex = paletteHelper.getInt(value);
			if(newPaletteIndex == 0) {
				newPaletteIndex = palette.size();
				boolean add = true;
				for(int i = 1; i < palette.size(); i++){
					if(palette.get(i) == null) {
						newPaletteIndex = i;
						add = false;
						break;
					}
				}
				paletteHelper.put(value, newPaletteIndex);
				newHolder = new PlayerChunkClaimHolder(value);
				if(add)
					palette.add(newHolder);
				else
					palette.set(newPaletteIndex, newHolder);
				region.onAddedToPalette(this, value);
				if(paletteInts != null) {
					if(add)
						paletteInts.add(value.getSyncIndex());
					else
						paletteInts.set(newPaletteIndex - 1, value.getSyncIndex());
				}
			} else
				newHolder = palette.get(newPaletteIndex);
		}
		if(newHolder != currentHolder) {
			int index = getIndex(x, z);
			if(currentHolder != null) {
				currentHolder.decrement();
				if(currentHolder.getCount() == 0) {
					int currentPaletteIndex = storage.get(index);
					removePaletteElement(currentPaletteIndex, region);
				}
			}
			if(newHolder != null)
				newHolder.increment();
			
			ensureSyncableStorageBits();
			storage.set(index, newPaletteIndex);
		}
	}

	private void recalculateHolders() {
		for(int i = 0; i < storage.getSize(); i++) {
			int storageValue = storage.get(i);
			if(storageValue > 0)
				palette.get(storageValue).increment();
		}
		needsHolderRecalculation = false;
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
			BitStorage newStorage = new SimpleBitStorage(neededBits, 1024);
			int allowedValueLimit = 1 << neededBits;
			BitStorage oldStorage = this.storage;
			for (int i = 0; i < 1024; i++) {
				int oldValue = oldStorage.get(i);
				if(oldValue < allowedValueLimit)
					newStorage.set(i, oldValue);
			}
			storage = newStorage;
		}
	}
	
	private void removePaletteElement(int paletteIndex, RegionClaims<?,?> region) {
		if(paletteIndex == 0)
			return;

		PlayerChunkClaim removedState = palette.set(paletteIndex, null).getClaim();
		paletteHelper.removeInt(removedState);
		region.onRemovedFromPalette(this, removedState);
		if(paletteInts != null)
			paletteInts.set(paletteIndex - 1, -1);
		if(paletteIndex == palette.size() - 1){
			while(palette.size() > 1 && palette.get(palette.size() - 1) == null) {
				palette.remove(palette.size() - 1);
				if(paletteInts != null)
					paletteInts.removeInt(paletteInts.size() - 1);
			}
		}
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

	public boolean containsState(PlayerChunkClaim state) {
		return paletteHelper.containsKey(state);
	}

	public void setNeedsHolderRecalculation(boolean needsHolderRecalculation) {
		this.needsHolderRecalculation = needsHolderRecalculation;
	}

}
