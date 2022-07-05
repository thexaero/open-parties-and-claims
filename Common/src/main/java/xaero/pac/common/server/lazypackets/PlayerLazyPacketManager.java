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

package xaero.pac.common.server.lazypackets;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.server.player.data.IOpenPACServerPlayer;
import xaero.pac.common.server.player.data.ServerPlayerData;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.UUID;

public class PlayerLazyPacketManager {

	private final MinecraftServer server;
	private final UUID playerId;
	private final Deque<LazyPacket<?,?>> storage;
	private int sentSinceConfirmation;
	private boolean waitingForConfirmation;
	private long startedWaitingAt;
	private boolean dropped;

	private PlayerLazyPacketManager(MinecraftServer server, UUID playerId, Deque<LazyPacket<?,?>> storage) {
		super();
		this.server = server;
		this.playerId = playerId;
		this.storage = storage;
	}
	
	public UUID getPlayerId() {
		return playerId;
	}
	
	LazyPacket<?,?> getNext(){
		return storage.removeFirst();
	}
	
	boolean hasNext(int bytesPerConfirmation, boolean overCapacity, LazyPacketManager manager) {
		if(dropped)
			return false;
		if(waitingForConfirmation && System.currentTimeMillis() - startedWaitingAt > 60000 /*no response in a minute*/){
			storage.forEach(manager::countSentBytes);
			storage.clear();
			ServerPlayer serverPlayer = server.getPlayerList().getPlayer(playerId);
			ServerPlayerData playerData = (ServerPlayerData) ((IOpenPACServerPlayer)serverPlayer).getXaero_OPAC_PlayerData();
			playerData.getClaimsManagerPlayerSyncHandler().onLazyPacketsDropped();
			OpenPartiesAndClaims.LOGGER.info("Dropped lazy packets for player " + serverPlayer.getGameProfile().getName() + " because the client isn't responding. Probably no mod on their side.");
			dropped = true;//won't send lazy packets to this player anymore
			//doing this instead of a client->server handshake because it is more secure (clients can send a fake handshake)
			return false;
		}
		if(!overCapacity && waitingForConfirmation)
			return false;//wait for client confirmation
		return !storage.isEmpty();
	}
	
	boolean enqueue(LazyPacket<?,?> packet) {
		if(dropped)
			return false;
		storage.addLast(packet);
		return true;
	}
	
	public void clientConfirm() {
		sentSinceConfirmation = 0;
		waitingForConfirmation = false;
	}

	public void startWaitingForConfirmation(){
		waitingForConfirmation = true;
		startedWaitingAt = System.currentTimeMillis();
	}

	public boolean isWaitingForConfirmation() {
		return waitingForConfirmation;
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

		private MinecraftServer server;
		private UUID playerId;
		
		public Builder setDefault() {
			setServer(null);
			setPlayerId(null);
			return this;
		}

		public Builder setServer(MinecraftServer server) {
			this.server = server;
			return this;
		}

		public Builder setPlayerId(UUID playerId) {
			this.playerId = playerId;
			return this;
		}
		
		public PlayerLazyPacketManager build() {
			if(server == null || playerId == null)
				throw new IllegalStateException();
			return new PlayerLazyPacketManager(server, playerId, new ArrayDeque<>());
		}
		
		public static Builder begin() {
			return new Builder().setDefault();
		}
		
	}

}
