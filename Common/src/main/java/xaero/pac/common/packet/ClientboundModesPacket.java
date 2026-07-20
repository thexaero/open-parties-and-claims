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

package xaero.pac.common.packet;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.claims.player.mode.ClaimingMode;
import xaero.pac.common.claims.player.mode.api.ClaimingModes;
import xaero.pac.common.server.player.data.api.ServerPlayerDataAPI;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class ClientboundModesPacket {

	private final boolean adminMode;
	private final ClaimingMode claimingMode;

	public ClientboundModesPacket(boolean adminMode, ClaimingMode claimingMode) {
		super();
		this.adminMode = adminMode;
		this.claimingMode = claimingMode;
	}
	
	public static class Codec implements BiConsumer<ClientboundModesPacket, FriendlyByteBuf>, Function<FriendlyByteBuf, ClientboundModesPacket> {
		
		@Override
		public ClientboundModesPacket apply(FriendlyByteBuf input) {
			try {
				if(input.readableBytes() > 1024)
					return null;
				CompoundTag tag = (CompoundTag) input.readNbt(NbtAccounter.unlimitedHeap());
				if(tag == null)
					return null;
				boolean adminMode = tag.getBooleanOr("am", false);
				ClaimingMode claimingMode = (ClaimingMode) ClaimingModes.get(tag.getStringOr("cm", ""));
				return new ClientboundModesPacket(adminMode, claimingMode);
			} catch(Throwable t) {
				OpenPartiesAndClaims.LOGGER.error("invalid packet ", t);
				return null;
			}
		}

		@Override
		public void accept(ClientboundModesPacket t, FriendlyByteBuf u) {
			CompoundTag tag = new CompoundTag();
			tag.putBoolean("am", t.adminMode);
			if(t.claimingMode != null)
				tag.putString("cm", t.claimingMode.getId());
			u.writeNbt(tag);
		}

	}
	
	public static class ClientHandler implements Consumer<ClientboundModesPacket> {
		
		@Override
		public void accept(ClientboundModesPacket t) {
			OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getClientClaimsSyncHandler().onClaimModes(t.adminMode, t.claimingMode);
		}
		
	}

	public static ClientboundModesPacket get(ServerPlayerDataAPI playerData){
		return new ClientboundModesPacket(playerData.isClaimsAdminMode(), (ClaimingMode) playerData.getRawClaimingMode());
	}
	
}
