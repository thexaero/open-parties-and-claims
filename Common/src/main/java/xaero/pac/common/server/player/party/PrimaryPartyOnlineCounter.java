/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2026, Xaero <xaero1996@gmail.com> and contributors
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

package xaero.pac.common.server.player.party;

import xaero.pac.common.server.claims.forceload.ForceLoadTicketManager;
import xaero.pac.common.server.config.ServerConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PrimaryPartyOnlineCounter {

	private final Map<UUID, PrimaryPartyOnlineCounterData> partyInfoMap;
	private final ForceLoadTicketManager forceLoadTicketManager;

	private PrimaryPartyOnlineCounter(
			Map<UUID, PrimaryPartyOnlineCounterData> partyInfoMap,
			ForceLoadTicketManager forceLoadTicketManager
	) {
		this.partyInfoMap = partyInfoMap;
		this.forceLoadTicketManager = forceLoadTicketManager;
	}

	private PrimaryPartyOnlineCounterData get(UUID partyOwner){
		PrimaryPartyOnlineCounterData partyOnlineCounter = partyInfoMap.get(partyOwner);
		if(partyOnlineCounter == null)
			partyInfoMap.put(partyOwner, partyOnlineCounter = new PrimaryPartyOnlineCounterData(partyOwner));
		return partyOnlineCounter;
	}

	public boolean isPartyOnline(UUID partyOwner) {
		PrimaryPartyOnlineCounterData partyOnlineCounter = get(partyOwner);
		return partyOnlineCounter.getOnlineMemberCount() > 0;
	}

	public void registerOnlinePartyMember(UUID partyOwner){
		PrimaryPartyOnlineCounterData partyOnlineCounter = get(partyOwner);
		partyOnlineCounter.incrementCount();
		if(ServerConfig.CONFIG.partyOwnedClaims.get() && partyOnlineCounter.getOnlineMemberCount() == 1)
			forceLoadTicketManager.updateTicketsFor(partyOwner, false);
	}

	public void unregisterOnlinePartyMember(UUID partyOwner){
		PrimaryPartyOnlineCounterData partyOnlineCounter = get(partyOwner);
		partyOnlineCounter.decrementCount();
		if(ServerConfig.CONFIG.partyOwnedClaims.get() && partyOnlineCounter.getOnlineMemberCount() == 0)
			forceLoadTicketManager.updateTicketsFor(partyOwner, false);
	}

	public static final class Builder {

		private ForceLoadTicketManager forceloadManager;

		private Builder(){}

		public Builder setDefault(){
			setForceloadManager(null);
			return this;
		}

		public Builder setForceloadManager(ForceLoadTicketManager forceloadManager){
			this.forceloadManager = forceloadManager;
			return this;
		}

		public PrimaryPartyOnlineCounter build(){
			if(forceloadManager == null)
				throw new IllegalStateException();
			return new PrimaryPartyOnlineCounter(new HashMap<>(), forceloadManager);
		}

		public static Builder begin(){
			return new Builder().setDefault();
		}

	}


}
