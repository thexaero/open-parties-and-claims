/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2026, Xaero <xaero1996@gmail.com> and contributors
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
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.claims.player.mode.ClaimingMode;
import xaero.pac.common.claims.player.mode.ClaimingModeLimits;
import xaero.pac.common.claims.player.mode.api.ClaimingModes;
import xaero.pac.common.server.lazypacket.LazyPacket;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class ClientboundClaimsResetPacket extends LazyPacket<ClientboundClaimsResetPacket> {

	public static final Encoder<ClientboundClaimsResetPacket> ENCODER = new Encoder<>();
	public static final Decoder DECODER = new Decoder();

	@Override
	protected void writeOnPrepare(FriendlyByteBuf u) {
	}

	@Override
	protected Function<FriendlyByteBuf, ClientboundClaimsResetPacket> getDecoder() {
		return DECODER;
	}
	
	public static class Decoder implements Function<FriendlyByteBuf, ClientboundClaimsResetPacket> {
		
		@Override
		public ClientboundClaimsResetPacket apply(FriendlyByteBuf input) {
			try {
				if(input.readableBytes() > 1024)
					return null;
				return new ClientboundClaimsResetPacket();
			} catch(Throwable t) {
				OpenPartiesAndClaims.LOGGER.error("invalid packet ", t);
				return null;
			}
		}
		
	}
	
	public static class ClientHandler extends Handler<ClientboundClaimsResetPacket> {
		
		@Override
		public void handle(ClientboundClaimsResetPacket t) {
			OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getClientClaimsSyncHandler().onClaimsReset();
		}
		
	}

}
