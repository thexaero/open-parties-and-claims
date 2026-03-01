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

package xaero.pac.common.server.player.config.change;

import net.minecraft.server.level.ServerPlayer;
import xaero.pac.common.parties.party.IPartyMemberDynamicInfoSyncable;
import xaero.pac.common.server.claims.IServerClaimsManager;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.player.config.PlayerConfig;
import xaero.pac.common.server.player.config.PlayerConfigManager;
import xaero.pac.common.server.player.config.PlayerConfigOptionSpec;
import xaero.pac.common.server.player.data.ServerPlayerData;
import xaero.pac.common.server.player.data.api.ServerPlayerDataAPI;

public class PlayerConfigCommonChangeHandlers {

	public static <P extends IServerParty<?, ?, ?>> void handleAbstractBonusClaims(
			PlayerConfigManager<P, ?> manager,
			PlayerConfig<P> config,
			PlayerConfigOptionSpec<Integer> option,
			Integer oldValue,
			Integer newValue
	){
		ServerPlayer onlinePlayer = config.getOnlinePlayer();
		if(onlinePlayer == null)
			return;
		IServerClaimsManager<?, ?, ?> claimsManager = manager.getClaimsManager();
		claimsManager.getClaimsManagerSynchronizer().syncClaimLimits(config, onlinePlayer);
	}

	public static <P extends IServerParty<?, ?, ?>> void handleForceloading(
			PlayerConfigManager<P, ?> manager,
			PlayerConfig<P> config,
			PlayerConfigOptionSpec<?> option,
			Object oldValue,
			Object newValue
	){
		manager.getForceLoadTicketManager().updateTicketsFor(manager, config.getPlayerId(), false);
	}

	public static <P extends IServerParty<?, ?, ?>> void handleBonusForceloads(
			PlayerConfigManager<P, ?> manager,
			PlayerConfig<P> config,
			PlayerConfigOptionSpec<Integer> option,
			Integer oldValue,
			Integer newValue
	){
		handleAbstractBonusClaims(manager, config, option, oldValue, newValue);
		handleForceloading(manager, config, option, oldValue, newValue);
	}

	public static <P extends IServerParty<?, ?, ?>> void handlePartyName(
			PlayerConfigManager<P, ?> manager,
			PlayerConfig<P> config,
			PlayerConfigOptionSpec<String> option,
			String oldValue,
			String newValue
	){
		P party = manager.getPartyManager().getPartyByOwner(config.getPlayerId());
		if(party == null)
			return;
		manager.getPartyManager().getPartySynchronizer().syncToPartyAndAlliersUpdateName(party, newValue);
	}

	public static <P extends IServerParty<?, ?, ?>> void handleShareLocationWithParty(
			PlayerConfigManager<P, ?> manager,
			PlayerConfig<P> config,
			PlayerConfigOptionSpec<Boolean> option,
			Boolean oldValue,
			Boolean newValue
	){
		P party = manager.getPartyManager().getPartyByMember(config.getPlayerId());
		if(party == null)
			return;
		ServerPlayer onlinePlayer = config.getOnlinePlayer();
		if(onlinePlayer == null)
			return;
		ServerPlayerData mainCap = (ServerPlayerData) ServerPlayerDataAPI.from(onlinePlayer);
		IPartyMemberDynamicInfoSyncable syncedInfo = newValue ?
				mainCap.getPartyMemberDynamicInfo() : mainCap.getPartyMemberDynamicInfo().getRemover();
		manager.getPartyManager().getPartySynchronizer().getOftenSyncedInfoSync()
				.syncToPartyDynamicInfo(party, syncedInfo, party);
	}

	public static <P extends IServerParty<?, ?, ?>> void handleShareLocationWithAllies(
			PlayerConfigManager<P, ?> manager,
			PlayerConfig<P> config,
			PlayerConfigOptionSpec<Boolean> option,
			Boolean oldValue,
			Boolean newValue
	){
		P party = manager.getPartyManager().getPartyByMember(config.getPlayerId());
		if(party == null)
			return;
		ServerPlayer onlinePlayer = config.getOnlinePlayer();
		if(onlinePlayer == null)
			return;
		ServerPlayerData mainCap = (ServerPlayerData) ServerPlayerDataAPI.from(onlinePlayer);
		IPartyMemberDynamicInfoSyncable syncedInfo = newValue ?
				mainCap.getPartyMemberDynamicInfo() : mainCap.getPartyMemberDynamicInfo().getRemover();
		manager.getPartyManager().getPartySynchronizer().getOftenSyncedInfoSync()
				.syncToPartyMutualAlliesDynamicInfo(party, syncedInfo);
	}

	public static <P extends IServerParty<?, ?, ?>> void handleReceiveLocationsFromParty(
			PlayerConfigManager<P, ?> manager,
			PlayerConfig<P> config,
			PlayerConfigOptionSpec<Boolean> option,
			Boolean oldValue,
			Boolean newValue
	){
		P party = manager.getPartyManager().getPartyByMember(config.getPlayerId());
		if(party == null)
			return;
		ServerPlayer onlinePlayer = config.getOnlinePlayer();
		if(onlinePlayer == null)
			return;
		manager.getPartyManager().getPartySynchronizer().getOftenSyncedInfoSync()
				.syncToClientAllDynamicInfo(onlinePlayer, party, !newValue);
	}

	public static <P extends IServerParty<?, ?, ?>> void handleReceiveLocationsFromAllies(
			PlayerConfigManager<P, ?> manager,
			PlayerConfig<P> config,
			PlayerConfigOptionSpec<Boolean> option,
			Boolean oldValue,
			Boolean newValue
	){
		P party = manager.getPartyManager().getPartyByMember(config.getPlayerId());
		if(party == null)
			return;
		ServerPlayer onlinePlayer = config.getOnlinePlayer();
		if(onlinePlayer == null)
			return;
		manager.getPartyManager().getPartySynchronizer().getOftenSyncedInfoSync()
				.syncToClientMutualAlliesDynamicInfo(onlinePlayer, party, !newValue);
	}

	public static <P extends IServerParty<?, ?, ?>> void handleClaimsProperty(
			PlayerConfigManager<P, ?> manager,
			PlayerConfig<P> config,
			PlayerConfigOptionSpec<?> option,
			Object oldValue,
			Object newValue
	){
		manager.getClaimsManager().getClaimsManagerSynchronizer().syncToPlayersSubClaimPropertiesUpdate(config);
	}

	public static <P extends IServerParty<?, ?, ?>> void handleUsedSubClaim(
			PlayerConfigManager<P, ?> manager,
			PlayerConfig<P> config,
			PlayerConfigOptionSpec<String> option,
			String oldValue,
			String newValue
	){
		ServerPlayer onlinePlayer = config.getOnlinePlayer();
		if(onlinePlayer == null)
			return;
		manager.getClaimsManager().getClaimsManagerSynchronizer().syncCurrentSubClaim(config, onlinePlayer);
	}

}
