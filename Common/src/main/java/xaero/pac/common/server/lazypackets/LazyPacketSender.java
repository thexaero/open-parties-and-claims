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
import xaero.pac.common.packet.LazyPacketsConfirmationPacket;

public class LazyPacketSender {//sends packets over time with no unnecessary rushing
	
	private final static LazyPacketsConfirmationPacket CONFIRMATION_PACKET = new LazyPacketsConfirmationPacket();
	
	private final MinecraftServer server;
	private final LazyPacketManager manager;
	private final int bytesPerTickLimit;//can go over this because individual packets are sent in full
	private final int capacity;//at which point to stop being lazy
	private final int bytesPerConfirmation;

	private LazyPacketSender(MinecraftServer server, LazyPacketManager manager, int bytesPerTickLimit, int capacity, int bytesPerConfirmation) {
		super();
		this.server = server;
		this.manager = manager;
		this.bytesPerTickLimit = bytesPerTickLimit;
		this.capacity = capacity;
		this.bytesPerConfirmation = bytesPerConfirmation;
	}
	
	public void clearForPlayer(ServerPlayer player) {
		manager.clearForPlayer(player);
	}
	
	public void enqueue(ServerPlayer player, LazyPacket<?, ?> packet) {
		manager.enqueue(player, packet);
	}
	
	public void onConfirmation(ServerPlayer player) {
		manager.onConfirmation(player);
	}
	
	public void onServerTick() {
		int bytesSent = 0;
		int bytesToSend = bytesPerTickLimit;
		boolean overCapacity;
		while((overCapacity = manager.getTotalBytesEnqueued() > capacity) || bytesSent < bytesToSend) {
			PlayerLazyPacketManager playerPackets = manager.getNext(bytesPerConfirmation, overCapacity);
			if(playerPackets == null)
				break;
			LazyPacket<?, ?> packet = playerPackets.getNext();
			bytesSent += packet.getPreparedSize();
			ServerPlayer player = server.getPlayerList().getPlayer(playerPackets.getPlayerId());
			OpenPartiesAndClaims.INSTANCE.getPacketHandler().sendToPlayer(player, packet);
			
			manager.countSentBytes(packet);
			playerPackets.onSend(packet);
			if(!playerPackets.isWaitingForConfirmation() && playerPackets.needsConfirmation(bytesPerConfirmation)) {
				playerPackets.startWaitingForConfirmation();
				OpenPartiesAndClaims.INSTANCE.getPacketHandler().sendToPlayer(player, CONFIRMATION_PACKET);
			}
		}
		//if(bytesSent != 0 || manager.getTotalBytesEnqueued() != 0)
		//	OpenPartiesAndClaims.LOGGER.info("sent " + bytesSent + " with " + manager.getTotalBytesEnqueued() + " left");
	}
	
	public static final class Builder {
		
		private MinecraftServer server;
		private int bytesPerTickLimit;
		private int capacity;
		private int bytesPerConfirmation;
		
		public Builder setDefault() {
			setServer(null);
			setBytesPerTickLimit(0);
			setCapacity(0);
			setBytesPerConfirmation(0);
			return this;
		}
		
		public Builder setServer(MinecraftServer server) {
			this.server = server;
			return this;
		}
		
		public Builder setBytesPerTickLimit(int bytesPerTickLimit) {
			this.bytesPerTickLimit = bytesPerTickLimit;
			return this;
		}
		
		public Builder setCapacity(int capacity) {
			this.capacity = capacity;
			return this;
		}
		
		public Builder setBytesPerConfirmation(int bytesPerConfirmation) {
			this.bytesPerConfirmation = bytesPerConfirmation;
			return this;
		}
		
		public LazyPacketSender build() {
			if(server == null || bytesPerTickLimit == 0 || capacity == 0 || bytesPerConfirmation == 0)
				throw new IllegalStateException();
			return new LazyPacketSender(server, LazyPacketManager.Builder.begin().setServer(server).build(), bytesPerTickLimit, capacity, bytesPerConfirmation);
		}
		
		public static Builder begin() {
			return new Builder().setDefault();
		}
		
	}

}
