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

package xaero.pac.common.server.parties.party.sync;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import xaero.pac.client.parties.party.ClientPartyAllyInfo;
import xaero.pac.common.packet.ClientboundLoadingPacket;
import xaero.pac.common.packet.parties.ClientboundPartyAllyPacket;
import xaero.pac.common.packet.parties.ClientboundPartyNamePacket;
import xaero.pac.common.packet.parties.ClientboundPartyPacket;
import xaero.pac.common.packet.parties.ClientboundPartyPlayerPacket;
import xaero.pac.common.packet.parties.ClientboundPartyPlayerPacket.Action;
import xaero.pac.common.packet.parties.ClientboundPartyPlayerPacket.Type;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.ally.PartyAlly;
import xaero.pac.common.parties.party.member.PartyInvite;
import xaero.pac.common.parties.party.member.PartyMember;
import xaero.pac.common.server.config.ServerConfig;
import xaero.pac.common.server.lazypacket.task.schedule.LazyPacketScheduleTaskHandler;
import xaero.pac.common.server.parties.party.PartyManager;
import xaero.pac.common.server.parties.party.ServerParty;
import xaero.pac.common.server.player.config.IPlayerConfig;
import xaero.pac.common.server.player.config.IPlayerConfigManager;
import xaero.pac.common.server.player.config.api.PlayerConfigOptions;
import xaero.pac.common.server.player.data.ServerPlayerData;

import java.util.List;
import java.util.UUID;

public class PartySynchronizer extends AbstractPartySynchronizer implements IPartySynchronizer<ServerParty> {

	public static final int PARTY_ELEMENTS_PER_TICK = 8192;
	public static final int PARTY_ELEMENTS_PER_TICK_PER_PLAYER = 512;

	private PartyManager partyManager;
	private final PartyMemberDynamicInfoSynchronizer dynamicInfoSync;
	private final List<LazyPacketScheduleTaskHandler> schedulers;
	
	private PartySynchronizer(MinecraftServer server, PartyMemberDynamicInfoSynchronizer dynamicInfoSync, List<LazyPacketScheduleTaskHandler> schedulers) {
		super(server);
		this.dynamicInfoSync = dynamicInfoSync;
		this.schedulers = schedulers;
	}

	public void setPartyManager(PartyManager partyManager) {
		if(this.partyManager != null)
			throw new IllegalAccessError();
		this.partyManager = partyManager;
		dynamicInfoSync.setPartyManager(partyManager);
	}

	private void syncToPartyPlayerInfo(ServerParty party, Type type, Action action, IPartyPlayerInfo playerInfo) {
		Object packet = new ClientboundPartyPlayerPacket(type, action, playerInfo);
		syncToParty(party, action == Action.ADD ? mi -> mi == playerInfo : mi -> false, packet, false);
	}

	public void syncToClientPlayerInfo(ServerPlayer player, Type type, Action action, IPartyPlayerInfo playerInfo) {
		Object packet = new ClientboundPartyPlayerPacket(type, action, playerInfo);
		sendToClient(player, packet, false);
	}
	
	private void syncToPartyAlly(ServerParty party, ClientboundPartyAllyPacket.Action action, ServerParty ally) {
		syncToPartyAlly(party, action, ally, fetchConfiguredPartyName(ally));
	}
	
	private void syncToPartyAlly(ServerParty party, ClientboundPartyAllyPacket.Action action, ServerParty ally, String fetchedName) {
		if(ally == null)
			return;
		Object packet = new ClientboundPartyAllyPacket(action, new ClientPartyAllyInfo(ally.getId(), fetchedName, ally.getDefaultName()));
		syncToParty(party, mi -> false, packet, false);
	}
	
	public void syncToClientAlly(ServerPlayer player, ClientboundPartyAllyPacket.Action action, ServerParty ally) {
		syncToClientAlly(player, action, ally, fetchConfiguredPartyName(ally));
	}
	
	private void syncToClientAlly(ServerPlayer player, ClientboundPartyAllyPacket.Action action, ServerParty ally, String fetchedName) {
		if(ally == null)
			return;
		Object packet = new ClientboundPartyAllyPacket(action, new ClientPartyAllyInfo(ally.getId(), fetchedName, ally.getDefaultName()));
		sendToClient(player, packet, false);
	}
	
	public void syncToPartyUpdateOwner(ServerParty party) {
		syncToPartyPlayerInfo(party, Type.OWNER, Action.UPDATE, party.getOwner());
	}
	
	public void syncToPartyAddMember(ServerParty party, PartyMember addedMember) {
		syncToPartyPlayerInfo(party, Type.MEMBER, Action.ADD, addedMember);
	}
	
	public void syncToPartyRemoveMember(ServerParty party, PartyMember removedMember) {
		syncToPartyPlayerInfo(party, Type.MEMBER, Action.REMOVE, removedMember);
		PlayerList playerList = server.getPlayerList();
		ServerPlayer onlinePlayer = playerList.getPlayer(removedMember.getUUID());
		if(onlinePlayer != null)
			dynamicInfoSync.handlePlayerLeave(party, onlinePlayer);
	}
	
	public void syncToPartyUpdateMember(ServerParty party, PartyMember member) {
		syncToPartyPlayerInfo(party, Type.MEMBER, Action.UPDATE, member);
	}
	
	public void syncToPartyAddInvite(ServerParty party, PartyInvite invite) {
		syncToPartyPlayerInfo(party, Type.INVITE, Action.ADD, invite);
	}
	
	public void syncToPartyRemoveInvite(ServerParty party, PartyInvite invite) {
		syncToPartyPlayerInfo(party, Type.INVITE, Action.REMOVE, invite);
	}
	
	public void syncToClientUpdateName(ServerPlayer player, String name) {
		sendToClient(player, new ClientboundPartyNamePacket(name), false);
	}
	
	@Override
	public void syncToPartyAndAlliersUpdateName(ServerParty party, String name) {
		syncToParty(party, mi -> false, new ClientboundPartyNamePacket(name), false);
		partyManager.getPartiesThatAlly(party.getId()).forEach(allier -> syncToPartyAllyUpdate(allier, party, name));
	}
	
	public void syncToPartyAllyAdd(ServerParty party, ServerParty ally) {
		syncToPartyAlly(party, ClientboundPartyAllyPacket.Action.ADD, ally);
		if(ally.isAlly(party.getId())) {
			dynamicInfoSync.syncToPartyAnotherPartyDynamicInfo(party, ally, false);
			dynamicInfoSync.syncToPartyAnotherPartyDynamicInfo(ally, party, false);
		}
	}
	
	private void syncToPartyAllyUpdate(ServerParty party, ServerParty ally, String fetchedName) {
		syncToPartyAlly(party, ClientboundPartyAllyPacket.Action.UPDATE, ally, fetchedName);
	}
	
	public void syncToPartyAllyRemove(ServerParty party, UUID allyId, boolean onPartyRemoval) {
		if(!onPartyRemoval) {
			Object packet = new ClientboundPartyAllyPacket(ClientboundPartyAllyPacket.Action.REMOVE, new ClientPartyAllyInfo(allyId, "", ""));
			syncToParty(party, mi -> false, packet, false);
		}
		ServerParty ally = partyManager.getPartyById(allyId);
		if(ally != null && ally.isAlly(party.getId())) {
			if(!onPartyRemoval)//I don't think this would break anything otherwise but it's a waste of resources
				dynamicInfoSync.syncToPartyAnotherPartyDynamicInfo(party, ally, true);
			dynamicInfoSync.syncToPartyAnotherPartyDynamicInfo(ally, party, true);
		}
	}

	public void sendSyncStart(ServerPlayer player){
		sendToClient(player, ClientboundLoadingPacket.START_PARTY, false);
	}

	public void sendBasePartyPackets(ServerPlayer player, ServerParty party){
		IPlayerConfigManager playerConfigs = serverData.getPlayerConfigs();
		sendToClient(player,
				party == null ?
						new ClientboundPartyPacket(null, null, 0, 0, 0, 0, 0, 0) :
						new ClientboundPartyPacket(party.getId(), party.getOwner(), party.getMemberCount(), party.getInviteCount(), party.getAllyCount(),
								ServerConfig.CONFIG.maxPartyMembers.get(), ServerConfig.CONFIG.maxPartyInvites.get(), ServerConfig.CONFIG.maxPartyAllies.get())
				, false);
		syncToClientUpdateName(player, fetchConfiguredPartyName(playerConfigs, party));
	}

	public void sendToClientAllyAdd(ServerPlayer player, PartyAlly ally){
		syncToClientAlly(player, ClientboundPartyAllyPacket.Action.ADD, partyManager.getPartyById(ally.getPartyId()));
	}

	public void sendSyncEnd(ServerPlayer player){
		sendToClient(player, ClientboundLoadingPacket.END_PARTY, false);
	}
	
	@Override
	public void syncToClient(ServerPlayer player, ServerParty party) {
		if(party != null)
			dynamicInfoSync.syncToClientAllDynamicInfoIncludingMutualAllies(player, party);
		ServerPlayerData playerData = (ServerPlayerData) ServerPlayerData.from(player);
		playerData.getFullPartyPlayerSync().startPartySync(player, party);
	}
	
	public void syncToMember(PartyMember member, ServerParty party) {
		PlayerList playerList = server.getPlayerList();
		ServerPlayer player = playerList.getPlayer(member.getUUID());
		if(player == null)
			return;
		syncToClient(player, party);
	}
	
	private String fetchConfiguredPartyName(ServerParty party) {
		return fetchConfiguredPartyName(serverData.getPlayerConfigs(), party);
	}
	
	private String fetchConfiguredPartyName(IPlayerConfigManager playerConfigs, ServerParty party) {
		IPlayerConfig ownerConfig = party == null ? null : playerConfigs.getLoadedConfig(party.getOwner().getUUID());
		String configuredName = ownerConfig == null ? null : ownerConfig.getEffective(PlayerConfigOptions.PARTY_NAME);
		return configuredName;
	}

	@Override
	public void onLazyPacketsDropped(ServerPlayer player){
		schedulers.forEach(s -> s.onLazyPacketsDropped(player));
	}

	@Override
	public void onServerTick() {
		schedulers.forEach(s -> s.onTick(serverData));
	}

	@Override
	public PartyMemberDynamicInfoSynchronizer getOftenSyncedInfoSync() {
		return dynamicInfoSync;
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
		
		public PartySynchronizer build() {
			if(server == null)
				throw new IllegalStateException();
			LazyPacketScheduleTaskHandler fullPartySyncScheduler = LazyPacketScheduleTaskHandler.Builder.begin()
					.setPlayerTaskGetter(ServerPlayerData::getFullPartyPlayerSync)
					.setPerTickLimit(PARTY_ELEMENTS_PER_TICK)
					.setPerTickPerTaskLimit(PARTY_ELEMENTS_PER_TICK_PER_PLAYER)
					.build();
			List<LazyPacketScheduleTaskHandler> schedulers = List.of(fullPartySyncScheduler);
			return new PartySynchronizer(server, new PartyMemberDynamicInfoSynchronizer(server), schedulers);
		}
		
		public static Builder begin() {
			return new Builder().setDefault();
		}
		
	}

}
