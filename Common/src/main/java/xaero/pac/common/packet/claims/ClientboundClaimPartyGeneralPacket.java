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
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.server.lazypacket.LazyPacket;

import java.util.UUID;
import java.util.function.Function;

public class ClientboundClaimPartyGeneralPacket extends LazyPacket<ClientboundClaimPartyGeneralPacket> {

	public static final Encoder<ClientboundClaimPartyGeneralPacket> ENCODER = new Encoder<>();
	public static final Decoder DECODER = new Decoder();

	private final boolean partyOwnedClaims;
	private final UUID partyOwnerId;

	public ClientboundClaimPartyGeneralPacket(boolean partyOwnedClaims, UUID partyOwnerId) {
		super();
		this.partyOwnedClaims = partyOwnedClaims;
		this.partyOwnerId = partyOwnerId;
	}

	@Override
	protected void writeOnPrepare(FriendlyByteBuf u) {
		CompoundTag tag = new CompoundTag();
		tag.putBoolean("pm", partyOwnedClaims);
		if(partyOwnerId != null)
			tag.putUUID("po", partyOwnerId);
		u.writeNbt(tag);
	}

	@Override
	protected Function<FriendlyByteBuf, ClientboundClaimPartyGeneralPacket> getDecoder() {
		return DECODER;
	}
	
	public static class Decoder implements Function<FriendlyByteBuf, ClientboundClaimPartyGeneralPacket> {
		
		@Override
		public ClientboundClaimPartyGeneralPacket apply(FriendlyByteBuf input) {
			try {
				if(input.readableBytes() > 2048)
					return null;
				CompoundTag tag = (CompoundTag) input.readNbt(NbtAccounter.unlimitedHeap());
				if(tag == null)
					return null;
				boolean partyOwnedClaims = tag.getBoolean("pm");
				UUID partyOwnerId = tag.hasUUID("po") ? tag.getUUID("po") : null;
				return new ClientboundClaimPartyGeneralPacket(partyOwnedClaims, partyOwnerId);
			} catch(Throwable t) {
				OpenPartiesAndClaims.LOGGER.error("invalid packet ", t);
				return null;
			}
		}
		
	}
	
	public static class ClientHandler extends Handler<ClientboundClaimPartyGeneralPacket> {
		
		@Override
		public void handle(ClientboundClaimPartyGeneralPacket t) {
			OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getClientClaimsSyncHandler()
					.onPartyGeneral(t.partyOwnedClaims, t.partyOwnerId);
		}
		
	}
	
}
