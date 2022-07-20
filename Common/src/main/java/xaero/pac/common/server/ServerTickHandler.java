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

package xaero.pac.common.server;

import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.claims.player.IPlayerClaimPosList;
import xaero.pac.common.claims.player.IPlayerDimensionClaims;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.member.IPartyMember;
import xaero.pac.common.server.claims.IServerClaimsManager;
import xaero.pac.common.server.claims.IServerDimensionClaimsManager;
import xaero.pac.common.server.claims.IServerRegionClaims;
import xaero.pac.common.server.claims.player.IServerPlayerClaimInfo;
import xaero.pac.common.server.lazypacket.LazyPacketSender;
import xaero.pac.common.server.parties.party.IServerParty;

public class ServerTickHandler {
	
	private final LazyPacketSender lazyPacketSender;
	private long lastUseTimeUpdate;
	private long tickCounter;
	
	public ServerTickHandler(LazyPacketSender lazyPacketSender) {
		this.lazyPacketSender = lazyPacketSender;
		this.lastUseTimeUpdate = System.currentTimeMillis();
	}
	
	public void onTick(IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo>> serverData) throws Throwable {
		serverData.getIoThreadWorker().checkCrashes();
		serverData.getPartyManagerIO().onServerTick();
		serverData.getPlayerConfigsIO().onServerTick();

		
		@SuppressWarnings("unused")
		boolean hasSaved = //finish saving 1 live saver before starting with another
				serverData.getPartyLiveSaver().onServerTick() || 
				serverData.getPlayerConfigLiveSaver().onServerTick() || 
				serverData.getPlayerClaimInfoLiveSaver().onServerTick();
		
		lazyPacketSender.onServerTick();
		
		serverData.getServerClaimsManager().getClaimsManagerSynchronizer().onServerTick();
		
		long time = System.currentTimeMillis();
		if(time - lastUseTimeUpdate > 600000/*10 minutes*/) {
			serverData.getServerInfo().setUseTime(serverData.getServerInfo().getUseTime() + time - lastUseTimeUpdate);
			lastUseTimeUpdate = time;
			serverData.getServerInfoIO().save();
		}
		@SuppressWarnings("unused")
		boolean expirationCheck = serverData.getPartyExpirationHandler().onServerTick() || //prevents doing both on the same tick
									serverData.getServerPlayerClaimsExpirationHandler().onServerTick();
		
		tickCounter++;
	}
	
	public LazyPacketSender getLazyPacketSender() {
		return lazyPacketSender;
	}
	
	public long getTickCounter() {
		return tickCounter;
	}

}
