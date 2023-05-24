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

package xaero.pac.common.server.parties.party.sync;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import xaero.pac.common.parties.party.IPartyMemberDynamicInfoSyncable;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.PartyMemberDynamicInfoSyncable;
import xaero.pac.common.parties.party.ally.PartyAlly;
import xaero.pac.common.parties.party.member.PartyMember;
import xaero.pac.common.server.parties.party.PartyManager;
import xaero.pac.common.server.parties.party.ServerParty;
import xaero.pac.common.server.player.config.IPlayerConfigManager;
import xaero.pac.common.server.player.config.PlayerConfigOptionSpec;
import xaero.pac.common.server.player.config.api.PlayerConfigOptions;
import xaero.pac.common.server.player.data.ServerPlayerData;
import xaero.pac.common.server.player.data.api.ServerPlayerDataAPI;

import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class PartyMemberDynamicInfoSynchronizer extends AbstractPartySynchronizer implements IPartyMemberDynamicInfoSynchronizer<ServerParty> {

	private PartyManager partyManager;
	public PartyMemberDynamicInfoSynchronizer(MinecraftServer server) {
		super(server);
	}

	public void setPartyManager(PartyManager partyManager) {
		this.partyManager = partyManager;
	}

	@Override
	public void syncToPartyDynamicInfo(ServerParty party, IPartyMemberDynamicInfoSyncable syncedInfo, ServerParty fromParty) {
		IPlayerConfigManager configManager = partyManager.getPlayerConfigs();
		if(syncedInfo.isActive() && party == fromParty && !configManager.getLoadedConfig(syncedInfo.getPlayerId()).getEffective(PlayerConfigOptions.SHARE_LOCATION_WITH_PARTY))
			return;
		if(syncedInfo.isActive() && syncedInfo.getPartyId() == null)
			return;
		PartyMember exceptionMemberInfo = party != fromParty ? null : party.getMemberInfo(syncedInfo.getPlayerId());
		PlayerConfigOptionSpec<Boolean> receiveConfigOption = (PlayerConfigOptionSpec<Boolean>) (party == fromParty ? PlayerConfigOptions.RECEIVE_LOCATIONS_FROM_PARTY : PlayerConfigOptions.RECEIVE_LOCATIONS_FROM_PARTY_MUTUAL_ALLIES);
		Predicate<IPartyPlayerInfo> exception = mi -> mi == exceptionMemberInfo || !configManager.getLoadedConfig(mi.getUUID()).getEffective(receiveConfigOption);
		syncToParty(party, exception, syncedInfo, true);
	}

	@Override
	public void syncToPartyMutualAlliesDynamicInfo(ServerParty party, IPartyMemberDynamicInfoSyncable syncedInfo) {
		IPlayerConfigManager configManager = partyManager.getPlayerConfigs();
		if(syncedInfo.isActive() && !configManager.getLoadedConfig(syncedInfo.getPlayerId()).getEffective(PlayerConfigOptions.SHARE_LOCATION_WITH_PARTY_MUTUAL_ALLIES))
			return;
		if(syncedInfo.isActive() && syncedInfo.getPartyId() == null)
			return;
		Iterator<PartyAlly> allyIterator = party.getAllyPartiesStream().iterator();
		while(allyIterator.hasNext()){
			ServerParty allyParty = partyManager.getPartyById(allyIterator.next().getPartyId());
			if(allyParty != null && allyParty.isAlly(party.getId()))
				syncToPartyDynamicInfo(allyParty, syncedInfo, party);
		}
	}
	
	@Override
	public void syncToPartiesDynamicInfo(ServerParty party, IPartyMemberDynamicInfoSyncable syncedInfo) {
		syncToPartyDynamicInfo(party, syncedInfo, party);
		syncToPartyMutualAlliesDynamicInfo(party, syncedInfo);
	}

	private void syncToClientAllDynamicInfo(IPlayerConfigManager configManager, ServerPlayer player, ServerParty party, ServerParty toParty, boolean removers) {
		if(!removers && !configManager.getLoadedConfig(player.getUUID()).getEffective(party == toParty ? PlayerConfigOptions.RECEIVE_LOCATIONS_FROM_PARTY : PlayerConfigOptions.RECEIVE_LOCATIONS_FROM_PARTY_MUTUAL_ALLIES))
			return;
		PlayerConfigOptionSpec<Boolean> shareConfigOption = (PlayerConfigOptionSpec<Boolean>) (party == toParty ? PlayerConfigOptions.SHARE_LOCATION_WITH_PARTY : PlayerConfigOptions.SHARE_LOCATION_WITH_PARTY_MUTUAL_ALLIES);
		Consumer<ServerPlayer> onlineMemberConsumer = onlineMember -> {
			if(onlineMember != player && configManager.getLoadedConfig(onlineMember.getUUID()).getEffective(shareConfigOption)) {
				ServerPlayerData partyMemberMainCap = (ServerPlayerData) ServerPlayerDataAPI.from(onlineMember);
				IPartyMemberDynamicInfoSyncable syncedInfo = removers ? partyMemberMainCap.getPartyMemberDynamicInfo().getRemover() : partyMemberMainCap.getPartyMemberDynamicInfo();
				if(removers || syncedInfo.getPartyId() != null)
					sendToClient(player, syncedInfo, true);
			}
		};
		party.getOnlineMemberStream().forEach(onlineMemberConsumer);
	}

	@Override
	public void syncToClientAllDynamicInfo(ServerPlayer player, ServerParty party, boolean removers) {
		syncToClientAllDynamicInfo(partyManager.getPlayerConfigs(), player, party, party, removers);
	}

	@Override
	public void syncToClientMutualAlliesDynamicInfo(ServerPlayer player, ServerParty party, boolean removers) {
		IPlayerConfigManager configManager = partyManager.getPlayerConfigs();
		party.getAllyPartiesStream().forEach(ally -> {
			ServerParty allyParty = partyManager.getPartyById(ally.getPartyId());
			if(allyParty != null && allyParty.isAlly(party.getId()))
				syncToClientAllDynamicInfo(configManager, player, allyParty, party, removers);
		});
	}
	
	public void syncToClientAllDynamicInfoIncludingMutualAllies(ServerPlayer player, ServerParty party) {
		syncToClientAllDynamicInfo(player, party, false);
		syncToClientMutualAlliesDynamicInfo(player, party, false);
	}
	
	public void syncToPartyAnotherPartyDynamicInfo(ServerParty party, ServerParty anotherParty, boolean removers) {
		if(party == anotherParty)
			throw new IllegalArgumentException();
		IPlayerConfigManager configManager = partyManager.getPlayerConfigs();
		Consumer<ServerPlayer> onlineAllyMemberConsumer = anotherPartyMember -> {
			if(configManager.getLoadedConfig(anotherPartyMember.getUUID()).getEffective(PlayerConfigOptions.SHARE_LOCATION_WITH_PARTY_MUTUAL_ALLIES)) {
				ServerPlayerData anotherPartyMemberMainCap = (ServerPlayerData) ServerPlayerDataAPI.from(anotherPartyMember);
				IPartyMemberDynamicInfoSyncable syncedInfo = removers ? anotherPartyMemberMainCap.getPartyMemberDynamicInfo().getRemover() : anotherPartyMemberMainCap.getPartyMemberDynamicInfo();
				syncToPartyDynamicInfo(party, syncedInfo, anotherParty);
			}
		};
		anotherParty.getOnlineMemberStream().forEach(onlineAllyMemberConsumer);
	}
	
	@Override
	public void handlePlayerLeave(ServerParty playerParty, ServerPlayer player) {
		if(playerParty != null) {
			ServerPlayerData playerMainCap = (ServerPlayerData) ServerPlayerDataAPI.from(player);
			PartyMemberDynamicInfoSyncable remover = playerMainCap.getPartyMemberDynamicInfo().getRemover();
			IPlayerConfigManager configManager = partyManager.getPlayerConfigs();
			if(configManager.getLoadedConfig(player.getUUID()).getEffective(PlayerConfigOptions.SHARE_LOCATION_WITH_PARTY))
				syncToPartyDynamicInfo(playerParty, remover, playerParty);
			if(configManager.getLoadedConfig(player.getUUID()).getEffective(PlayerConfigOptions.SHARE_LOCATION_WITH_PARTY_MUTUAL_ALLIES))
				syncToPartyMutualAlliesDynamicInfo(playerParty, remover);
		}
	}

	@Override
	public void onPlayerTick(ServerPlayerData mainCap, ServerPlayer player){
		ServerParty playerParty = partyManager.getPartyByMember(player.getUUID());
		if(playerParty != null) {
			mainCap.getPartyMemberDynamicInfo().setPartyId(playerParty.getId());
			mainCap.getPartyMemberDynamicInfo().update(player.level().dimension().location(), player.getX(), player.getY(), player.getZ());
			if(mainCap.getPartyMemberDynamicInfo().isDirty())
				syncToPartiesDynamicInfo(playerParty, mainCap.getPartyMemberDynamicInfo());
		} else
			mainCap.getPartyMemberDynamicInfo().setPartyId(null);
	}

}
