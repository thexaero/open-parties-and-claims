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

package xaero.pac.common.packet.config;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.claims.player.IPlayerClaimPosList;
import xaero.pac.common.claims.player.IPlayerDimensionClaims;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.ally.IPartyAlly;
import xaero.pac.common.parties.party.member.IPartyMember;
import xaero.pac.common.server.IServerData;
import xaero.pac.common.server.ServerData;
import xaero.pac.common.server.claims.IServerClaimsManager;
import xaero.pac.common.server.claims.IServerDimensionClaimsManager;
import xaero.pac.common.server.claims.IServerRegionClaims;
import xaero.pac.common.server.claims.player.IServerPlayerClaimInfo;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.player.config.IPlayerConfig;
import xaero.pac.common.server.player.config.IPlayerConfigManager;
import xaero.pac.common.server.player.data.ServerPlayerData;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class ServerboundOtherPlayerConfigPacket extends PlayerConfigPacket {
	
	private final String ownerName;

	public ServerboundOtherPlayerConfigPacket(String ownerName) {
		super();
		this.ownerName = ownerName;
	}
	
	public static class Codec implements BiConsumer<ServerboundOtherPlayerConfigPacket, FriendlyByteBuf>, Function<FriendlyByteBuf, ServerboundOtherPlayerConfigPacket> {

		@Override
		public ServerboundOtherPlayerConfigPacket apply(FriendlyByteBuf input) {
			try {
				if(input.readableBytes() > 1024)
					return null;
				CompoundTag nbt = (CompoundTag) input.readNbt(NbtAccounter.unlimitedHeap());
				if(nbt == null)
					return null;
				String ownerName = nbt.getString("ownerName");
				if(ownerName.isEmpty() || !ownerName.matches("^[a-zA-Z0-9_]+$"))
					return null;
				return new ServerboundOtherPlayerConfigPacket(ownerName);
			} catch(Throwable t) {
				return null;
			}
		}

		@Override
		public void accept(ServerboundOtherPlayerConfigPacket t, FriendlyByteBuf u) {
			CompoundTag nbt = new CompoundTag();
			nbt.putString("ownerName", t.ownerName);
			u.writeNbt(nbt);
		}
		
	}
	
	public static class ServerHandler implements BiConsumer<ServerboundOtherPlayerConfigPacket,ServerPlayer> {
		
		@Override
		public void accept(ServerboundOtherPlayerConfigPacket t, ServerPlayer serverPlayer) {
			if(!serverPlayer.hasPermissions(2)) {
				OpenPartiesAndClaims.LOGGER.info("Non-op player is attempting to requesting another player's config! Name: " + serverPlayer.getGameProfile().getName());
				return;
			}
			serverPlayer.getServer().getProfileCache().get(t.ownerName).ifPresent(gp -> {
				IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(serverPlayer.getServer());
				IPlayerConfigManager playerConfigs = serverData.getPlayerConfigs();
				IPlayerConfig config = playerConfigs.getLoadedConfig(gp.getId());
				ServerPlayerData playerData = (ServerPlayerData) ServerPlayerData.from(serverPlayer);
				playerData.getConfigSyncSpreadoutTask().addConfigToSync(config);
			});
		}
		
	}
}
