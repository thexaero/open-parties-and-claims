/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2022-2026, Xaero <xaero1996@gmail.com> and contributors
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

package xaero.pac.common.server.player;

import net.minecraft.server.level.ServerPlayer;
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.claims.player.IPlayerClaimPosList;
import xaero.pac.common.claims.player.IPlayerDimensionClaims;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.ally.IPartyAlly;
import xaero.pac.common.parties.party.member.IPartyMember;
import xaero.pac.common.server.IServerData;
import xaero.pac.common.server.claims.IServerClaimsManager;
import xaero.pac.common.server.claims.IServerDimensionClaimsManager;
import xaero.pac.common.server.claims.IServerRegionClaims;
import xaero.pac.common.server.claims.player.IServerPlayerClaimInfo;
import xaero.pac.common.server.claims.player.ServerPlayerClaimPartyUpdater;
import xaero.pac.common.server.claims.player.ServerPlayerClaimPartyNameUpdater;
import xaero.pac.common.server.claims.player.ServerPlayerClaimWelcomer;
import xaero.pac.common.server.config.ServerConfig;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.player.data.ServerPlayerData;
import xaero.pac.common.server.player.data.api.ServerPlayerDataAPI;
import xaero.pac.common.server.player.party.ServerPlayerPartyOnlineCounterUpdater;

public class PlayerTickHandler {

	private final ServerPlayerClaimWelcomer claimWelcomer;
	private final ServerPlayerClaimPartyNameUpdater claimPartyNameUpdater;
	private final ServerPlayerClaimPartyUpdater claimPartyConfigUpdater;
	private final ServerPlayerPartyOnlineCounterUpdater playerPartyOnlineCounterUpdater;

	private PlayerTickHandler(
			ServerPlayerClaimWelcomer claimWelcomer,
			ServerPlayerClaimPartyNameUpdater claimPartyNameUpdater,
			ServerPlayerClaimPartyUpdater claimPartyConfigUpdater,
			ServerPlayerPartyOnlineCounterUpdater playerPartyOnlineCounterUpdater
	) {
		this.claimWelcomer = claimWelcomer;
		this.claimPartyNameUpdater = claimPartyNameUpdater;
		this.claimPartyConfigUpdater = claimPartyConfigUpdater;
		this.playerPartyOnlineCounterUpdater = playerPartyOnlineCounterUpdater;
	}

	public void onTick(
			ServerPlayer player,
			IServerData<
					IServerClaimsManager<
							IPlayerChunkClaim,
							IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>,
							IServerDimensionClaimsManager<IServerRegionClaims>
					>,
					IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>
			> serverData
	) {
		ServerPlayerData playerData = (ServerPlayerData) ServerPlayerDataAPI.from(player);
		if(!playerData.hasHandledLogin())
			return;
		playerData.onTick();
		if(playerData.shouldResyncPlayerConfigs()) {
			serverData.getPlayerConfigManager().getSynchronizer().syncAllToClient(player);
			playerData.setShouldResyncPlayerConfigs(false);
		}
		serverData.getPlayerConfigPermissionUpdater().update(playerData, player, serverData, true, true);
		if(ServerConfig.CONFIG.claimsEnabled.get()) {
			claimWelcomer.onPlayerTick(playerData, player, serverData);
			claimPartyNameUpdater.onPlayerTick(playerData, player, serverData);
			claimPartyConfigUpdater.onPlayerTick(playerData, player, serverData);
			IServerClaimsManager<?, ?, ?> claimsManager = serverData.getServerClaimsManager();
			claimsManager.getClaimsManagerSynchronizer().updateClaimLimitsSyncOnTick(playerData, player);
		}
		playerPartyOnlineCounterUpdater.onPlayerTick(playerData, player, serverData);

		serverData.getPartyManager().getPartySynchronizer().getOftenSyncedInfoSync().onPlayerTick(playerData, player);
	}

	public static final class Builder {

		private ServerPlayerPartyOnlineCounterUpdater playerClaimPartyForceloadUpdater;

		private Builder(){}

		public Builder setDefault(){
			setPlayerClaimPartyForceloadUpdater(null);
			return this;
		}

		public Builder setPlayerClaimPartyForceloadUpdater(ServerPlayerPartyOnlineCounterUpdater playerClaimPartyForceloadUpdater) {
			this.playerClaimPartyForceloadUpdater = playerClaimPartyForceloadUpdater;
			return this;
		}

		public PlayerTickHandler build(){
			if(playerClaimPartyForceloadUpdater == null)
				throw new IllegalStateException();
			return new PlayerTickHandler(
					new ServerPlayerClaimWelcomer(), new ServerPlayerClaimPartyNameUpdater(),
					new ServerPlayerClaimPartyUpdater(), playerClaimPartyForceloadUpdater
			);
		}

		public static Builder begin(){
			return new Builder().setDefault();
		}

	}

}
