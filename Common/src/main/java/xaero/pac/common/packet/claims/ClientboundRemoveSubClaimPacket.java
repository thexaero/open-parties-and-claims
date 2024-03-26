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
import net.minecraft.network.FriendlyByteBuf;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.server.lazypacket.LazyPacket;

import java.util.UUID;
import java.util.function.Function;

public class ClientboundRemoveSubClaimPacket extends LazyPacket<ClientboundRemoveSubClaimPacket> {

	public static final Encoder<ClientboundRemoveSubClaimPacket> ENCODER = new Encoder<>();
	public static final Decoder DECODER = new Decoder();

	private final UUID playerId;
	private final int subConfigIndex;

	public ClientboundRemoveSubClaimPacket(UUID playerId, int subConfigIndex) {
		super();
		this.playerId = playerId;
		this.subConfigIndex = subConfigIndex;
	}

	@Override
	protected Function<FriendlyByteBuf, ClientboundRemoveSubClaimPacket> getDecoder() {
		return DECODER;
	}

	@Override
	protected void writeOnPrepare(FriendlyByteBuf u) {
		CompoundTag nbt = new CompoundTag();
		nbt.putUUID("p", playerId);
		nbt.putInt("s", subConfigIndex);
		u.writeNbt(nbt);
	}
	
	@Override
	public String toString() {
		return String.format("[%s, %d]", playerId, subConfigIndex);
	}
	
	public static class Decoder implements Function<FriendlyByteBuf, ClientboundRemoveSubClaimPacket> {

		@Override
		public ClientboundRemoveSubClaimPacket apply(FriendlyByteBuf input) {
			try {
				if(input.readableBytes() > 1024)
					return null;
				CompoundTag nbt = input.readAnySizeNbt();
				if(nbt == null)
					return null;
				UUID playerId = nbt.getUUID("p");
				int subConfigIndex = nbt.getInt("s");
				return new ClientboundRemoveSubClaimPacket(playerId, subConfigIndex);
			} catch(Throwable t) {
				OpenPartiesAndClaims.LOGGER.error("invalid packet", t);
				return null;
			}
		}
		
	}
	
	public static class ClientHandler extends Handler<ClientboundRemoveSubClaimPacket> {
		
		@Override
		public void handle(ClientboundRemoveSubClaimPacket t) {
			OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getClientClaimsSyncHandler().onRemoveSubClaim(t.playerId, t.subConfigIndex);
		}
		
	}

}
