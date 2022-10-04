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

package xaero.pac.common.server.parties.party.sync.player;

import net.minecraft.server.level.ServerPlayer;
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.claims.player.IPlayerClaimPosList;
import xaero.pac.common.claims.player.IPlayerDimensionClaims;
import xaero.pac.common.packet.parties.ClientboundPartyPlayerPacket;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.ally.IPartyAlly;
import xaero.pac.common.parties.party.ally.PartyAlly;
import xaero.pac.common.parties.party.member.IPartyMember;
import xaero.pac.common.parties.party.member.PartyInvite;
import xaero.pac.common.parties.party.member.PartyMember;
import xaero.pac.common.server.IServerData;
import xaero.pac.common.server.claims.IServerClaimsManager;
import xaero.pac.common.server.claims.IServerDimensionClaimsManager;
import xaero.pac.common.server.claims.IServerRegionClaims;
import xaero.pac.common.server.claims.player.IServerPlayerClaimInfo;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.parties.party.ServerParty;
import xaero.pac.common.server.parties.party.sync.PartySynchronizer;

import java.util.Iterator;

public final class PlayerFullPartySync extends PartyPlayerLazyPacketScheduler {

	private State currentState;

	public PlayerFullPartySync(PartySynchronizer synchronizer) {
		super(synchronizer);
	}

	@Override
	public void onLazyPacketsDropped() {
		currentState = null;
	}

	@Override
	protected boolean shouldWorkNotClogged(IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData, ServerPlayer player) {
		return currentState != null && !currentState.done;
	}

	@Override
	public void onTick(IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData, ServerPlayer player, int limit) {
		int stepsLeft = limit;
		if(currentState.memberIterator == null){
			currentState.start();
			synchronizer.sendBasePartyPackets(player, currentState.party);
			stepsLeft -= 5;
		}
		while(!currentState.done && stepsLeft > 0) {
			if(currentState.memberIterator.hasNext()){
				PartyMember memberInfo = currentState.memberIterator.next();
				synchronizer.syncToClientPlayerInfo(player, ClientboundPartyPlayerPacket.Type.MEMBER, ClientboundPartyPlayerPacket.Action.ADD, memberInfo);
				stepsLeft--;
			} else if(currentState.inviteIterator.hasNext()){
				PartyInvite invite = currentState.inviteIterator.next();
				synchronizer.syncToClientPlayerInfo(player, ClientboundPartyPlayerPacket.Type.INVITE, ClientboundPartyPlayerPacket.Action.ADD, invite);
				stepsLeft--;
			} else if(currentState.allyIterator.hasNext()){
				PartyAlly ally = currentState.allyIterator.next();
				synchronizer.sendToClientAllyAdd(player, ally);
				stepsLeft--;
			} else
				currentState.end();
		}
		if(currentState.done) {
			synchronizer.sendSyncEnd(player);
			currentState = null;
		}
	}

	public void startPartySync(ServerPlayer player, ServerParty party){
		currentState = new State(party);
		synchronizer.sendSyncStart(player);
	}

	private static class State {

		private final ServerParty party;
		private Iterator<PartyMember> memberIterator;
		private Iterator<PartyInvite> inviteIterator;
		private Iterator<PartyAlly> allyIterator;
		private boolean done;

		private State(ServerParty party){
			this.party = party;
		}

		private void start(){
			if(party == null) {
				done = true;
				return;
			}
			memberIterator = party.getPartyMemberIterator();
			inviteIterator = party.getPartyInviteIterator();
			allyIterator = party.getAllyPartiesIterator();
		}

		private void end(){
			done = true;
			memberIterator = null;
			inviteIterator = null;
			allyIterator = null;
		}

	}

}
