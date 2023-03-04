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

package xaero.pac.common.packet;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.network.FriendlyByteBuf;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.server.lazypacket.LazyPacket;

import java.util.function.Consumer;
import java.util.function.Function;

public class ClientboundLoadingPacket extends LazyPacket<LazyPacket.Encoder<ClientboundLoadingPacket>, ClientboundLoadingPacket> {
	
	public static final Encoder<ClientboundLoadingPacket> ENCODER = new Encoder<>();
	
	public static final ClientboundLoadingPacket START_PARTY = new ClientboundLoadingPacket(true, false);
	public static final ClientboundLoadingPacket END_PARTY = new ClientboundLoadingPacket(false, false);
	
	public static final ClientboundLoadingPacket START_CLAIMS = new ClientboundLoadingPacket(true, true);
	public static final ClientboundLoadingPacket END_CLAIMS = new ClientboundLoadingPacket(false, true);
	
	private final boolean start;
	private final boolean claims;
	
	private ClientboundLoadingPacket(boolean start, boolean claims) {
		super();
		this.start = start;
		this.claims = claims;
	}

	@Override
	protected void writeOnPrepare(LazyPacket.Encoder<ClientboundLoadingPacket> encoder, FriendlyByteBuf u) {
		CompoundTag tag = new CompoundTag();
		tag.putBoolean("s", start);
		tag.putBoolean("c", claims);
		u.writeNbt(tag);
	}

	@Override
	protected Encoder<ClientboundLoadingPacket> getEncoder() {
		return ENCODER;
	}
	
	public static class Decoder implements Function<FriendlyByteBuf, ClientboundLoadingPacket> {
		
		@Override
		public ClientboundLoadingPacket apply(FriendlyByteBuf input) {
			try {
				CompoundTag tag = input.readNbt(new NbtAccounter(1024));
				if(tag == null)
					return null;
				boolean start = tag.getBoolean("s");
				boolean claims = tag.getBoolean("c");
				return new ClientboundLoadingPacket(start, claims);
			} catch(Throwable t) {
				OpenPartiesAndClaims.LOGGER.error("invalid packet", t);
				return null;
			}
		}
		
	}
	
	public static class ClientHandler implements Consumer<ClientboundLoadingPacket> {
		
		@Override
		public void accept(ClientboundLoadingPacket t) {
			if(!t.claims) {
				OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getClientPartyStorage().setLoading(t.start);
			} else {
				OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getClientClaimsSyncHandler().onLoading(t.start);
			}
		}
		
	}
	
}
