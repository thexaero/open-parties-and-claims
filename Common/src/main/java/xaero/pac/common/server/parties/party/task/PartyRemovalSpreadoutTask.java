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

package xaero.pac.common.server.parties.party.task;

import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.claims.player.IPlayerClaimPosList;
import xaero.pac.common.claims.player.IPlayerDimensionClaims;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.ally.IPartyAlly;
import xaero.pac.common.parties.party.ally.PartyAlly;
import xaero.pac.common.parties.party.member.IPartyMember;
import xaero.pac.common.parties.party.member.PartyMember;
import xaero.pac.common.server.IServerData;
import xaero.pac.common.server.claims.IServerClaimsManager;
import xaero.pac.common.server.claims.IServerDimensionClaimsManager;
import xaero.pac.common.server.claims.IServerRegionClaims;
import xaero.pac.common.server.claims.player.IServerPlayerClaimInfo;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.parties.party.PartyManager;
import xaero.pac.common.server.parties.party.ServerParty;
import xaero.pac.common.server.task.IServerSpreadoutQueuedTask;

import java.util.Iterator;
import java.util.List;

public class PartyRemovalSpreadoutTask implements IServerSpreadoutQueuedTask<PartyRemovalSpreadoutTask> {

	private final PartyManager partyManager;
	private final ServerParty party;
	private Iterator<PartyMember> memberIterator;
	private Iterator<PartyAlly> allyIterator;
	private boolean done;

	public PartyRemovalSpreadoutTask(PartyManager partyManager, ServerParty party) {
		this.partyManager = partyManager;
		this.party = party;
	}

	@Override
	public void onQueued(IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData) {
		memberIterator = party.getPartyMemberIterator();
		allyIterator = party.getAllyPartiesIterator();
	}

	@Override
	public boolean shouldWork(IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData, PartyRemovalSpreadoutTask holder) {
		return !done;
	}

	@Override
	public boolean shouldDrop(IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData, PartyRemovalSpreadoutTask holder) {
		return !shouldWork(serverData, holder);
	}

	private boolean shouldBeDone(){
		return !memberIterator.hasNext() && !allyIterator.hasNext() && !partyManager.isAlliedByAnyone(party.getId());
	}

	@Override
	public void onTick(IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData, PartyRemovalSpreadoutTask holder, int perTick, List<PartyRemovalSpreadoutTask> tasksToAdd) {
		int stepsLeft = perTick;
		while(!(done = shouldBeDone()) && stepsLeft > 0){
			if(memberIterator.hasNext()){
				PartyMember mi = memberIterator.next();
				partyManager.onMemberRemoved(party, mi);
				stepsLeft--;
			} else if(allyIterator.hasNext()) {
				PartyAlly ally = allyIterator.next();
				partyManager.onAllyRemoved(party, ally.getPartyId(), true);
				stepsLeft--;
			} else {
				ServerParty alliedBy = partyManager.getPartiesThatAlly(party.getId()).findFirst().orElse(null);
				alliedBy.removeAllyParty(party.getId());
				stepsLeft--;
			}
		}
		if(done){
			memberIterator = null;
			allyIterator = null;
		}
	}

}
