/*
 *     Open Parties and Claims - adds chunk claims and player parties to Minecraft
 *     Copyright (C) 2022, Xaero <xaero1996@gmail.com> and contributors
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of version 3 of the GNU Lesser General Public License
 *     (LGPL-3.0-only) as published by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received copies of the GNU Lesser General Public License
 *     and the GNU General Public License along with this program.
 *     If not, see <https://www.gnu.org/licenses/>.
 */

package xaero.pac.common.server.lazypackets;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.UUID;

public class PlayerLazyPacketManager {
	
	private final UUID playerId;
	private final Deque<LazyPacket<?,?>> storage;
	private int sentSinceConfirmation;

	private PlayerLazyPacketManager(UUID playerId, Deque<LazyPacket<?,?>> storage) {
		super();
		this.playerId = playerId;
		this.storage = storage;
	}
	
	public UUID getPlayerId() {
		return playerId;
	}
	
	LazyPacket<?,?> getNext(){
		return storage.removeFirst();
	}
	
	boolean hasNext(int bytesPerConfirmation) {
		if(sentSinceConfirmation >= bytesPerConfirmation)
			return false;//wait for client confirmation
		return !storage.isEmpty();
	}
	
	void enqueue(LazyPacket<?,?> packet) {
		storage.addLast(packet);
	}
	
	public void clientConfirm() {
		sentSinceConfirmation = 0;
	}
	
	public void onSend(LazyPacket<?,?> packet) {
		sentSinceConfirmation += packet.getPreparedSize();
	}
	
	public boolean needsConfirmation(int bytesPerConfirmation) {
		if(sentSinceConfirmation >= bytesPerConfirmation)
			return true;
		return false;
	}
	
	public static final class Builder {

		private UUID playerId;
		
		public Builder setDefault() {
			setPlayerId(null);
			return this;
		}
		
		public Builder setPlayerId(UUID playerId) {
			this.playerId = playerId;
			return this;
		}
		
		public PlayerLazyPacketManager build() {
			if(playerId == null)
				throw new IllegalStateException();
			return new PlayerLazyPacketManager(playerId, new ArrayDeque<>());
		}
		
		public static Builder begin() {
			return new Builder().setDefault();
		}
		
	}

}
