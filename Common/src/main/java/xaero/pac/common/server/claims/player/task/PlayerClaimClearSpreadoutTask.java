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

package xaero.pac.common.server.claims.player.task;

import com.mojang.authlib.GameProfile;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
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
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.player.localization.AdaptiveLocalizer;

import java.util.UUID;
import java.util.function.Predicate;

public final class PlayerClaimClearSpreadoutTask extends PlayerClaimReplaceSpreadoutTask {

	private PlayerClaimClearSpreadoutTask(IPlayerClaimReplaceSpreadoutTaskCallback callback, UUID claimOwnerId, Predicate<IPlayerChunkClaim> matcher, IPlayerChunkClaim with) {
		super(callback, claimOwnerId, matcher, with);
	}

	public static final class Builder {

		private MinecraftServer server;
		private GameProfile targetPlayerProfile;
		private UUID callerUUID;

		private Builder(){}

		public Builder setDefault(){
			setServer(null);
			setTargetPlayerProfile(null);
			setCallerUUID(null);
			return this;
		}

		public Builder setServer(MinecraftServer server) {
			this.server = server;
			return this;
		}

		public Builder setTargetPlayerProfile(GameProfile targetPlayerProfile) {
			this.targetPlayerProfile = targetPlayerProfile;
			return this;
		}

		public Builder setCallerUUID(UUID callerUUID) {
			this.callerUUID = callerUUID;
			return this;
		}

		public PlayerClaimClearSpreadoutTask build(){
			if(server == null || targetPlayerProfile == null)
				throw new IllegalStateException();
			Callback callback = new Callback(server, callerUUID, targetPlayerProfile);
			UUID claimOwnerId = targetPlayerProfile.getId();
			Predicate<IPlayerChunkClaim> matcher = c -> true;
			return new PlayerClaimClearSpreadoutTask(callback, claimOwnerId, matcher, null);
		}

		public static Builder begin(){
			return new Builder().setDefault();
		}

	}

	private static final class Callback implements IPlayerClaimReplaceSpreadoutTaskCallback {

		private final MinecraftServer server;
		private final UUID callerUUID;
		private final GameProfile targetPlayerProfile;

		public Callback(MinecraftServer server, UUID callerUUID, GameProfile targetPlayerProfile) {
			this.server = server;
			this.callerUUID = callerUUID;
			this.targetPlayerProfile = targetPlayerProfile;
		}

		@Override
		public void onWork(int tickCount) {
		}

		@Override
		public void onFinish(ResultType resultType, int tickCount, int totalCount, IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData) {
			ServerPlayer onlinePlayer = callerUUID == null ? null : server.getPlayerList().getPlayer(callerUUID);
			AdaptiveLocalizer adaptiveLocalizer = serverData.getAdaptiveLocalizer();
			if (resultType.isSuccess()) {
				if (onlinePlayer != null) {
					Component targetName = Component.literal(targetPlayerProfile.getName()).withStyle(ChatFormatting.GREEN);
					onlinePlayer.sendMessage(adaptiveLocalizer.getFor(onlinePlayer, "gui.xaero_claims_clear_complete", targetName), onlinePlayer.getUUID());
				}
				return;
			}
			if (onlinePlayer != null)
				onlinePlayer.sendMessage(adaptiveLocalizer.getFor(onlinePlayer, resultType.getMessage()), onlinePlayer.getUUID());
		}

	}

}
