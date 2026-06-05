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

package xaero.pac.common.server.player.config.group.party;

import com.google.common.collect.Sets;
import net.minecraft.server.level.ServerPlayer;
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.claims.player.IPlayerClaimPosList;
import xaero.pac.common.claims.player.IPlayerDimensionClaims;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.ally.IPartyAlly;
import xaero.pac.common.parties.party.member.IPartyMember;
import xaero.pac.common.player.config.PlayerConfigConstants;
import xaero.pac.common.server.IServerData;
import xaero.pac.common.server.ServerData;
import xaero.pac.common.server.claims.IServerClaimsManager;
import xaero.pac.common.server.claims.IServerDimensionClaimsManager;
import xaero.pac.common.server.claims.IServerRegionClaims;
import xaero.pac.common.server.claims.player.IServerPlayerClaimInfo;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.parties.system.IPlayerPartySystemManager;
import xaero.pac.common.server.player.config.IPlayerConfig;
import xaero.pac.common.server.player.config.api.PlayerConfigType;
import xaero.pac.common.server.player.config.group.SimpleBuiltInPlayerConfigGroup;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public class AllyPlayerConfigGroup extends SimpleBuiltInPlayerConfigGroup {

	public AllyPlayerConfigGroup(String id) {
		super(id, Sets.newHashSet(PlayerConfigConstants.PARTY_EXCEPTION_ID));
	}

	@Override
	public boolean supportsConfigType(@Nonnull PlayerConfigType type) {
		return type == PlayerConfigType.PLAYER || type == PlayerConfigType.PARTY_CLAIMS;
	}

	@Override
	public boolean isDirectlyInGroup(IPlayerConfig contextConfig, @Nullable ServerPlayer player, @Nullable UUID playerId) {
		if(!supportsConfigType(contextConfig.getType()))
			return false;
		if(playerId == null){
			if(player == null)
				return false;
			playerId = player.getUUID();
		}
		UUID configOwnerId = contextConfig.getPlayerId();
		IServerData<
				IServerClaimsManager<
						IPlayerChunkClaim,
						IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>,
						IServerDimensionClaimsManager<IServerRegionClaims>
						>,
				IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>
				> serverData = ServerData.from(contextConfig.getManager().getServer());
		IPlayerPartySystemManager partySystemManager = serverData.getPlayerPartySystemManager();
		return partySystemManager.isPlayerAllying(configOwnerId, playerId);
	}

}
