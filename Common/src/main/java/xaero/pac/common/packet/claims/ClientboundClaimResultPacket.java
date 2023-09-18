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

package xaero.pac.common.packet.claims;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.network.FriendlyByteBuf;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.claims.result.api.AreaClaimResult;
import xaero.pac.common.claims.result.api.ClaimResult;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class ClientboundClaimResultPacket {
	
	private final AreaClaimResult result;
	
	public ClientboundClaimResultPacket(AreaClaimResult result) {
		super();
		this.result = result;
	}

	public static class Codec implements BiConsumer<ClientboundClaimResultPacket, FriendlyByteBuf>, Function<FriendlyByteBuf, ClientboundClaimResultPacket> {

		@Override
		public ClientboundClaimResultPacket apply(FriendlyByteBuf input) {
			try {
				if(input.readableBytes() > 2048)
					return null;
				CompoundTag tag = (CompoundTag) input.readNbt(NbtAccounter.unlimitedHeap());
				if(tag == null)
					return null;
				byte[] resultTypesArray = tag.getByteArray("ta");
				Set<ClaimResult.Type> resultTypes = new HashSet<>();
				for(byte ordinal : resultTypesArray) {
					ClaimResult.Type resultType;
					try {
						resultType = ClaimResult.Type.values()[ordinal];
					} catch(ArrayIndexOutOfBoundsException aioobe) {
						OpenPartiesAndClaims.LOGGER.error("illegal claim result id in packet", aioobe);
						return null;
					}
					resultTypes.add(resultType);
				}
				int left = tag.getInt("l");
				int top = tag.getInt("t");
				int right = tag.getInt("r");
				int bottom = tag.getInt("b");
				return new ClientboundClaimResultPacket(new AreaClaimResult(resultTypes, left, top, right, bottom));
			} catch(Throwable t) {
				OpenPartiesAndClaims.LOGGER.error("invalid packet", t);
				return null;
			}
		}

		@Override
		public void accept(ClientboundClaimResultPacket t, FriendlyByteBuf u) {
			CompoundTag tag = new CompoundTag();
			Iterator<ClaimResult.Type> iterator = t.result.getResultTypesIterable().iterator();
			byte[] resultTypes = new byte[t.result.getSize()];
			int index = 0;
			while (iterator.hasNext()) {
				resultTypes[index] = (byte) iterator.next().ordinal();
				index++;
			}
			tag.putByteArray("ta", resultTypes);
			tag.putInt("l", t.result.getLeft());
			tag.putInt("t", t.result.getTop());
			tag.putInt("r", t.result.getRight());
			tag.putInt("b", t.result.getBottom());
			u.writeNbt(tag);
		}
		
	}
	
	public static class ClientHandler implements Consumer<ClientboundClaimResultPacket> {
		
		@Override
		public void accept(ClientboundClaimResultPacket t) {
			OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getClientClaimsSyncHandler().onClaimResult(t.result);
		}
		
	}
	
}
