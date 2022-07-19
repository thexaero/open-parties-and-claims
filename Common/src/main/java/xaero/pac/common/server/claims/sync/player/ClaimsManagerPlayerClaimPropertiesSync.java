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

package xaero.pac.common.server.claims.sync.player;

import net.minecraft.server.level.ServerPlayer;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.packet.claims.ClientboundClaimPropertiesPacket;
import xaero.pac.common.server.IServerData;
import xaero.pac.common.server.claims.player.ServerPlayerClaimInfo;
import xaero.pac.common.server.claims.sync.ClaimsManagerSynchronizer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public final class ClaimsManagerPlayerClaimPropertiesSync extends ClaimsManagerPlayerLazyPacketScheduler {

	//no field for the player because this handler can be moved to another one (e.g. on respawn)
	private Iterator<ServerPlayerClaimInfo> toSync;

	private ClaimsManagerPlayerClaimPropertiesSync(Iterator<ServerPlayerClaimInfo> toSync, ClaimsManagerSynchronizer synchronizer) {
		super(synchronizer);
		this.toSync = toSync;
	}

	@Override
	public void doSchedule(IServerData<?,?> serverData, ServerPlayer player, int limit){
		List<ClientboundClaimPropertiesPacket.PlayerProperties> packetBuilder = new ArrayList<>(ClientboundClaimPropertiesPacket.MAX_PROPERTIES);
		int canSync = limit;
		while(canSync > 0 && toSync.hasNext()) {
			buildClaimPropertiesPacket(packetBuilder, toSync.next(), player);
			canSync--;
		}
		if(!packetBuilder.isEmpty())
			synchronizer.syncClaimProperties(packetBuilder, player);
	}

	private void buildClaimPropertiesPacket(List<ClientboundClaimPropertiesPacket.PlayerProperties> packetBuilder, ServerPlayerClaimInfo pi, ServerPlayer player) {
		UUID playerId = pi.getPlayerId();
		String username = pi.getPlayerUsername();
		String claimsName = pi.getClaimsName();
		int claimsColor = pi.getClaimsColor();
		packetBuilder.add(new ClientboundClaimPropertiesPacket.PlayerProperties(playerId, username, claimsName, claimsColor));
		if(packetBuilder.size() == ClientboundClaimPropertiesPacket.MAX_PROPERTIES) {
			synchronizer.syncClaimProperties(packetBuilder, player);
			packetBuilder.clear();
		}
	}

	@Override
	public void onLazyPacketsDropped() {
		toSync = null;
	}

	public boolean isFinished(){
		return toSync == null || !toSync.hasNext();
	}

	@Override
	public boolean shouldWork() {
		return started && !isFinished();
	}

	public static final class Builder {

		private ClaimsManagerSynchronizer synchronizer;
		private ServerPlayer player;

		private Builder(){}

		public Builder setDefault() {
			setSynchronizer(null);
			setPlayer(null);
			return this;
		}

		public Builder setSynchronizer(ClaimsManagerSynchronizer synchronizer) {
			this.synchronizer = synchronizer;
			return this;
		}

		public Builder setPlayer(ServerPlayer player) {
			this.player = player;
			return this;
		}

		public ClaimsManagerPlayerClaimPropertiesSync build(){
			if(synchronizer == null || player == null)
				throw new IllegalStateException();
			Iterator<ServerPlayerClaimInfo> toSync = synchronizer.getClaimPropertiesToSync(player);
			return new ClaimsManagerPlayerClaimPropertiesSync(toSync, synchronizer);
		}

		public static Builder begin(){
			return new Builder().setDefault();
		}

	}

}
