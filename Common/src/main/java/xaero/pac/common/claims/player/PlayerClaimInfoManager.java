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

package xaero.pac.common.claims.player;

import net.minecraft.resources.ResourceLocation;
import xaero.pac.common.util.linked.LinkedChain;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

//only used by ClaimsManager
public abstract class PlayerClaimInfoManager
<
	PCI extends PlayerClaimInfo<PCI, M>,
	M extends PlayerClaimInfoManager<PCI, M>
> {

	protected final M self;
	private Map<UUID, PCI> storage;
	private LinkedChain<PCI> linkedPlayerInfo;
	
	@SuppressWarnings("unchecked")
	public PlayerClaimInfoManager(Map<UUID, PCI> storage, LinkedChain<PCI> linkedPlayerInfo) {
		super();
		this.linkedPlayerInfo = linkedPlayerInfo;
		this.self = (M) this;
		this.storage = storage;
	}
	
	protected abstract PCI create(String username, UUID playerId, Map<ResourceLocation, PlayerDimensionClaims> claims);
	
	public boolean hasInfo(UUID playerId) {
		return storage.containsKey(playerId);
	}

	public PCI getInfo(UUID playerId) {
		PCI result = storage.get(playerId);
		if(result == null){
			result = create(playerId == null ? null : playerId.toString(), playerId, new HashMap<>());
			storage.put(playerId, result);
			onAdd(result);
		}
		return result;
	}
	
	public Stream<PCI> getInfoStream(){
		return linkedPlayerInfo.stream();
	}

	/**
	 * Gets an iterator that can be used across multiple game ticks without breaking.
	 *
	 * @return the region iterator, not null
	 */
	public Iterator<PCI> iterator(){
		return linkedPlayerInfo.iterator();
	}

	public void clear() {
		linkedPlayerInfo.destroy();
		linkedPlayerInfo = new LinkedChain<>();
		storage = new HashMap<>();
	}
	
	public void tryRemove(UUID playerId) {
		PCI info = storage.get(playerId);
		if(info != null && info.getClaimCount() == 0) {
			storage.remove(playerId);
			onRemove(info);
		}
	}

	protected void onAdd(PCI playerInfo){
		linkedPlayerInfo.add(playerInfo);
	}
	
	protected void onRemove(PCI playerInfo){
		linkedPlayerInfo.remove(playerInfo);
	}

}
