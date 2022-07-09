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

package xaero.pac.common.server.claims.player;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerPlayer;
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.claims.player.IPlayerClaimInfo;
import xaero.pac.common.claims.player.IPlayerClaimPosList;
import xaero.pac.common.claims.player.IPlayerDimensionClaims;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.member.IPartyMember;
import xaero.pac.common.server.IServerData;
import xaero.pac.common.server.claims.IServerClaimsManager;
import xaero.pac.common.server.claims.IServerDimensionClaimsManager;
import xaero.pac.common.server.claims.IServerRegionClaims;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.player.config.IPlayerConfig;
import xaero.pac.common.server.player.config.PlayerConfig;
import xaero.pac.common.server.player.data.ServerPlayerData;

import java.util.Objects;
import java.util.UUID;

public class ServerPlayerClaimWelcomer {

	public void onPlayerTick(ServerPlayerData mainCap, ServerPlayer player, IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo>> serverData){
		IPlayerChunkClaim lastClaimCheck = mainCap.getLastClaimCheck();
		IServerClaimsManager<?, ?, ?> claimsManager = serverData.getServerClaimsManager();
		IPlayerChunkClaim currentClaim = claimsManager.get(player.getLevel().dimension().location(), player.chunkPosition());
		if (!Objects.equals(lastClaimCheck, currentClaim)) {
			UUID currentClaimId = currentClaim == null ? null : currentClaim.getPlayerId();
			IPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>> playerClaimInfo = currentClaim == null ? null : serverData.getServerClaimsManager().getPlayerInfo(currentClaimId);
			boolean isOwner = !mainCap.isClaimsNonallyMode() && currentClaim != null && Objects.equals(currentClaimId, player.getUUID());
			boolean hasAccess = isOwner || serverData.getChunkProtection().hasChunkAccess(serverData.getChunkProtection().getClaimConfig(serverData.getPlayerConfigs(), currentClaim), player);

			IPlayerConfig claimConfig = serverData.getPlayerConfigs().getLoadedConfig(currentClaimId);
			String customName = claimConfig.getEffective(PlayerConfig.CLAIMS_NAME);
			int claimColor = claimConfig.getEffective(PlayerConfig.CLAIMS_COLOR);
			MutableComponent subTitleText;
			if (playerClaimInfo == null)
				subTitleText = customName == null || customName.isEmpty() ? new TranslatableComponent("gui.xaero_pac_title_entered_wilderness") : new TranslatableComponent(customName);
			else {
				TranslatableComponent properDesc;
				Component forceloadedComponent = currentClaim.isForceloadable() ? new TranslatableComponent("gui.xaero_pac_marked_for_forceload") : new TextComponent("");
				if (Objects.equals(currentClaimId, PlayerConfig.SERVER_CLAIM_UUID))
					properDesc = new TranslatableComponent("gui.xaero_pac_title_entered_server_claim", forceloadedComponent);
				else if (Objects.equals(currentClaimId, PlayerConfig.EXPIRED_CLAIM_UUID))
					properDesc = new TranslatableComponent("gui.xaero_pac_title_entered_expired_claim", forceloadedComponent);
				else
					properDesc = new TranslatableComponent("gui.xaero_pac_title_entered_claim", playerClaimInfo.getPlayerUsername(), forceloadedComponent);
				if (customName != null && !customName.isEmpty()) {
					subTitleText = new TextComponent(customName + " - ");
					subTitleText.getSiblings().add(properDesc);
				} else
					subTitleText = properDesc;
			}
			subTitleText = subTitleText.withStyle(s -> s.withColor(isOwner ? ChatFormatting.DARK_GREEN : hasAccess ? ChatFormatting.GOLD : ChatFormatting.DARK_RED));

			MutableComponent subTitle = new TextComponent("□ ").withStyle(s -> s.withColor(claimColor));
			subTitle.getSiblings().add(subTitleText);
			subTitle.getSiblings().add(new TextComponent(" □").withStyle(s -> s.withColor(claimColor)));
			ClientboundSetActionBarTextPacket packet = new ClientboundSetActionBarTextPacket(subTitle);
			player.connection.send(packet);

			mainCap.setLastClaimCheck(currentClaim);
		}
	}

}
