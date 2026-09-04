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

package xaero.pac.common.server.claims.player;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
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
import xaero.pac.common.server.config.ServerConfig;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.player.data.ServerPlayerData;
import xaero.pac.common.server.player.localization.AdaptiveLocalizer;

import java.util.Objects;
import java.util.UUID;

public class ServerPlayerClaimWelcomer {

	public void onPlayerTick(ServerPlayerData playerData, ServerPlayer player, IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData){
		IPlayerChunkClaim lastClaimCheck = playerData.getLastClaimCheck();
		ResourceKey<Level> lastClaimCheckDim = playerData.getLastClaimCheckDim();
		IServerClaimsManager<?, ?, ?> claimsManager = serverData.getServerClaimsManager();
		ResourceKey<Level> playerDimKey = player.getLevel().dimension();
		ResourceLocation playerDim = playerDimKey.location();
		IPlayerChunkClaim currentClaim = claimsManager.get(playerDim, player.chunkPosition());
		if (Objects.equals(lastClaimCheck, currentClaim) && lastClaimCheckDim == playerDimKey)
			return;
		if(!ServerConfig.CONFIG.claimWelcomeMessages.get()){
			playerData.setLastClaimCheck(currentClaim);
			playerData.setLastClaimCheckDim(playerDimKey);
			return;
		}
		AdaptiveLocalizer adaptiveLocalizer = serverData.getAdaptiveLocalizer();
		UUID currentClaimId = currentClaim == null ? null : currentClaim.getPlayerId();
		boolean isOwner = !playerData.isClaimsNonallyMode() && currentClaim != null && Objects.equals(currentClaimId, player.getUUID());
		boolean hasAccess = isOwner ||
				serverData.getChunkProtection().hasChunkAccess(
						serverData.getChunkProtection().getClaimConfig(serverData.getPlayerConfigManager(), currentClaim, playerDim),
						player, null, playerDim, player.chunkPosition().x, player.chunkPosition().z
				);

		int claimColor = claimsManager.getColor(currentClaim, playerDim);
		claimsManager.getPermissionHandler().ensureModeratorModeStatusPermission(player, playerData);
		boolean moderatorMode = playerData.isClaimsModeratorMode();
		MutableComponent subTitleText = adaptiveLocalizer.getFor(player, claimsManager.getFullName(currentClaim, playerDim, !moderatorMode)).copy();
		subTitleText = subTitleText.withStyle(s -> s.withColor(isOwner ? ChatFormatting.DARK_GREEN : hasAccess ? ChatFormatting.GOLD : ChatFormatting.DARK_RED));

		MutableComponent subTitle = new TextComponent("□ ").withStyle(s -> s.withColor(claimColor));
		subTitle.getSiblings().add(subTitleText);
		subTitle.getSiblings().add(new TextComponent(" □").withStyle(s -> s.withColor(claimColor)));
		ClientboundSetActionBarTextPacket packet = new ClientboundSetActionBarTextPacket(subTitle);
		player.connection.send(packet);

		playerData.setLastClaimCheck(currentClaim);
		playerData.setLastClaimCheckDim(playerDimKey);
	}

}
