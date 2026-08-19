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

package xaero.pac.common.packet.claims;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import xaero.pac.common.claims.action.api.ClaimingAction;
import xaero.pac.common.claims.player.mode.ClaimingMode;
import xaero.pac.common.claims.player.mode.api.ClaimingModes;
import xaero.pac.common.claims.action.request.ClaimActionRequest;
import xaero.pac.common.server.player.data.ServerPlayerData;
import xaero.pac.common.server.player.data.api.ServerPlayerDataAPI;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class ServerboundClaimActionRequestPacket {
	
	private final ClaimActionRequest request;
	
	public ServerboundClaimActionRequestPacket(ClaimActionRequest request) {
		super();
		this.request = request;
	}

	public static class Codec implements BiConsumer<ServerboundClaimActionRequestPacket, FriendlyByteBuf>, Function<FriendlyByteBuf, ServerboundClaimActionRequestPacket> {

		@Override
		public ServerboundClaimActionRequestPacket apply(FriendlyByteBuf input) {
			try {
				if(input.readableBytes() > 2048)
					return null;
				CompoundTag tag = (CompoundTag) input.readNbt(NbtAccounter.unlimitedHeap());
				if(tag == null)
					return null;
				byte actionByte = tag.getByteOr("a", (byte)0);
				ClaimingAction action;
				try {
					action = ClaimingAction.values()[actionByte];
				} catch(ArrayIndexOutOfBoundsException aioobe) {
					return null;
				}
				ResourceLocation dimension = ResourceLocation.parse(tag.getStringOr("d", ""));
				int left = tag.getIntOr("l", 0);
				int top = tag.getIntOr("t", 0);
				int right = tag.getIntOr("r", 0);
				int bottom = tag.getIntOr("b", 0);
				if(left > right || top > bottom)
					return null;
				String claimingModeId = tag.getStringOr("m", null);
				ClaimingMode claimingMode = null;
				if(claimingModeId != null)
					claimingMode = (ClaimingMode) ClaimingModes.get(claimingModeId);
				return new ServerboundClaimActionRequestPacket(new ClaimActionRequest(action, dimension, left, top, right, bottom, claimingMode));
			} catch(Throwable t) {
				return null;
			}
		}

		@Override
		public void accept(ServerboundClaimActionRequestPacket t, FriendlyByteBuf u) {
			CompoundTag tag = new CompoundTag();
			tag.putByte("a", (byte) t.request.getAction().ordinal());
			tag.putString("d", t.request.getDimension().toString());
			tag.putInt("l", t.request.getLeft());
			tag.putInt("t", t.request.getTop());
			tag.putInt("r", t.request.getRight());
			tag.putInt("b", t.request.getBottom());
			if(t.request.getMode() != null)
				tag.putString("m", t.request.getMode().getId());
			u.writeNbt(tag);
		}
		
	}
	
	public static class ServerHandler implements BiConsumer<ServerboundClaimActionRequestPacket,ServerPlayer> {
		
		@Override
		public void accept(ServerboundClaimActionRequestPacket t, ServerPlayer serverPlayer) {
			if(t == null)
				return;
			ServerPlayerData playerData = (ServerPlayerData) ServerPlayerDataAPI.from(serverPlayer);
			playerData.getClaimActionRequestHandler().onReceive(serverPlayer, t.request);
		}
		
	}
	
}
