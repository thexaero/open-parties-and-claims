/*
 *     Open Parties and Claims - adds chunk claims and player parties to Minecraft
 *     Copyright (C) 2022, Xaero <xaero1996@gmail.com> and contributors
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of version 3 of the GNU Lesser General Public License
 *     (LGPL-3.0-only) as published by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received copies of the GNU Lesser General Public License
 *     and the GNU General Public License along with this program.
 *     If not, see <https://www.gnu.org/licenses/>.
 */

package xaero.pac.common.packet.claims.owned;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.network.FriendlyByteBuf;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.server.lazypackets.LazyPacket;

import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

public class ClientboundPlayerClaimsOwnerPacket extends LazyPacket<LazyPacket.Encoder<ClientboundPlayerClaimsOwnerPacket>, ClientboundPlayerClaimsOwnerPacket>{
	
	public static final Encoder<ClientboundPlayerClaimsOwnerPacket> ENCODER = new Encoder<>();

	private final UUID ownerId;

	public ClientboundPlayerClaimsOwnerPacket(UUID ownerId) {
		super();
		this.ownerId = ownerId;
	}

	@Override
	protected Encoder<ClientboundPlayerClaimsOwnerPacket> getEncoder() {
		return ENCODER;
	}

	@Override
	protected void writeOnPrepare(Encoder<ClientboundPlayerClaimsOwnerPacket> encoder, FriendlyByteBuf u) {
		CompoundTag nbt = new CompoundTag();
		nbt.putUUID("o", ownerId);
		u.writeNbt(nbt);
	}
	
	public static class Decoder implements Function<FriendlyByteBuf, ClientboundPlayerClaimsOwnerPacket> {

		@Override
		public ClientboundPlayerClaimsOwnerPacket apply(FriendlyByteBuf input) {
			try {
				CompoundTag nbt = input.readNbt(new NbtAccounter(4096));
				if(nbt == null)
					return null;
				UUID ownerId = nbt.getUUID("o");
				return new ClientboundPlayerClaimsOwnerPacket(ownerId);
			} catch(Throwable t) {
				OpenPartiesAndClaims.LOGGER.error("invalid packet", t);
				return null;
			}
		}
		
	}
	
	public static class ClientHandler implements Consumer<ClientboundPlayerClaimsOwnerPacket> {
		
		@Override
		public void accept(ClientboundPlayerClaimsOwnerPacket t) {
			OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getClientClaimsSyncHandler().onOwner(t.ownerId);
		}
		
	}
	
}
