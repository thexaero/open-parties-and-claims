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

package xaero.pac.common.server.lazypacket;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;

public class LazyPacketManager {

	private final MinecraftServer server;
	private final List<UUID> orderHolder;
	private final Map<UUID, PlayerLazyPacketManager> storage;
	private int currentIndex;
	private int totalBytesEnqueued;

	private LazyPacketManager(MinecraftServer server, List<UUID> orderHolder, Map<UUID, PlayerLazyPacketManager> storage) {
		super();
		this.server = server;
		this.orderHolder = orderHolder;
		this.storage = storage;
	}
	
	private PlayerLazyPacketManager getForPlayer(UUID id) {
		PlayerLazyPacketManager result = storage.get(id);
		if(result == null) {
			if(server.getPlayerList().getPlayer(id) == null)//TODO remove this stuff once fixed
				throw new RuntimeException("(Open Parties and Claims) Please report this! " + id);
			result = PlayerLazyPacketManager.Builder.begin().setServer(server).setPlayerId(id).build();
			int insertionIndex = Collections.binarySearch(orderHolder, id);
			if(insertionIndex < 0)
				insertionIndex = -1 - insertionIndex;
			orderHolder.add(insertionIndex, id);
			storage.put(id, result);
		}

		return result;
	}
	
	public void clearForPlayer(ServerPlayer player) {
		int insertionIndex = Collections.binarySearch(orderHolder, player.getUUID());
		if(insertionIndex >= 0) {
			orderHolder.remove(insertionIndex);
			storage.remove(player.getUUID()).onDropped(player);
		}
	}
	
	public void enqueue(ServerPlayer player, LazyPacket<?,?> packet) {
		if(getForPlayer(player.getUUID()).enqueue(packet))
			totalBytesEnqueued += packet.prepare();
	}
	
	public void countSentBytes(LazyPacket<?,?> packet) {
		totalBytesEnqueued -= packet.getPreparedSize();
	}
	
	public int getTotalBytesEnqueued() {
		return totalBytesEnqueued;
	}
	
	public PlayerLazyPacketManager getNext(int bytesPerConfirmation, boolean overCapacity) {
		if(!orderHolder.isEmpty()) {
			for(int i = 0; i < orderHolder.size(); i++) {//check the whole list once at most
				currentIndex = currentIndex % orderHolder.size();
				UUID id = orderHolder.get(currentIndex);
				currentIndex++;
				PlayerLazyPacketManager playerPackets = getForPlayer(id);
				if(playerPackets.hasNext(overCapacity, this)) {
					if(server.getPlayerList().getPlayer(id) == null) {//TODO remove this stuff once fixed
						boolean suchPlayerExists = false;
						for(ServerPlayer player : server.getPlayerList().getPlayers())
							if(player.getUUID().equals(id)) {
								suchPlayerExists = true;
								break;
							}
						throw new RuntimeException("(Open Parties and Claims) Please report this! " + id + " " + suchPlayerExists);
					}
					return playerPackets;
				}
			}
		}
		return null;
	}
	
	void onConfirmation(ServerPlayer player) {
		getForPlayer(player.getUUID()).clientConfirm();
	}

	public boolean isClogged(ServerPlayer player){
		return getForPlayer(player.getUUID()).isClogged();
	}
	
	public static final class Builder {

		private MinecraftServer server;
		
		public Builder setDefault() {
			setServer(null);
			return this;
		}

		public Builder setServer(MinecraftServer server) {
			this.server = server;
			return this;
		}

		public LazyPacketManager build() {
			if(server == null)
				throw new IllegalStateException();
			return new LazyPacketManager(server, new ArrayList<>(), new HashMap<>());
		}
		
		public static Builder begin() {
			return new Builder().setDefault();
		}
		
	}

}
