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

package xaero.pac.common.packet.parties;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.server.lazypacket.LazyPacket;

import java.util.function.Function;

public class ClientboundPartyNamePacket extends LazyPacket<ClientboundPartyNamePacket> {
	
	public static final Encoder<ClientboundPartyNamePacket> ENCODER = new Encoder<>();
	public static final Decoder DECODER = new Decoder();
	
	private final String name;
	
	public ClientboundPartyNamePacket(String name) {
		super();
		this.name = name;
	}

	@Override
	protected void writeOnPrepare(FriendlyByteBuf u) {
		CompoundTag tag = new CompoundTag();
		tag.putString("n", name == null ? "" : name);
		u.writeNbt(tag);
	}

	@Override
	protected Function<FriendlyByteBuf, ClientboundPartyNamePacket> getDecoder() {
		return DECODER;
	}
	
	public static class Decoder implements Function<FriendlyByteBuf, ClientboundPartyNamePacket> {
		
		@Override
		public ClientboundPartyNamePacket apply(FriendlyByteBuf input) {
			try {
				if(input.readableBytes() > 16384)
					return null;
				CompoundTag tag = input.readAnySizeNbt();
				if(tag == null)
					return null;
				String name = tag.getString("n");
				if(name.length() > 512)
					return null;
				return new ClientboundPartyNamePacket(name);
			} catch(Throwable t) {
				OpenPartiesAndClaims.LOGGER.error("invalid packet ", t);
				return null;
			}
		}
		
	}
	
	public static class ClientHandler extends Handler<ClientboundPartyNamePacket> {
		
		@Override
		public void handle(ClientboundPartyNamePacket t) {
			OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getClientPartyStorage().setPartyName(t.name);
		}
		
	}
	
}
