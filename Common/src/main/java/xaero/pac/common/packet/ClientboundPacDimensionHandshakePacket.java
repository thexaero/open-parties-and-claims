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

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.client.gui.ConfigMenu;
import xaero.pac.client.gui.PlayerConfigScreen;
import xaero.pac.client.world.capability.ClientWorldMainCapability;
import xaero.pac.client.world.capability.api.ClientWorldCapabilityTypes;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class ClientboundPacDimensionHandshakePacket {

	private final boolean claimsEnabled;
	private final boolean partiesEnabled;

	public ClientboundPacDimensionHandshakePacket(boolean claimsEnabled, boolean partiesEnabled) {
		super();
		this.claimsEnabled = claimsEnabled;
		this.partiesEnabled = partiesEnabled;
	}
	
	public static class Codec implements BiConsumer<ClientboundPacDimensionHandshakePacket, FriendlyByteBuf>, Function<FriendlyByteBuf, ClientboundPacDimensionHandshakePacket> {

		@Override
		public ClientboundPacDimensionHandshakePacket apply(FriendlyByteBuf input) {
			try {
				if(input.readableBytes() > 512)
					return null;
				CompoundTag tag = input.readAnySizeNbt();
				if(tag == null)
					return null;
				boolean claimsEnabled = tag.getBoolean("c");
				boolean partiesEnabled = tag.getBoolean("p");
				return new ClientboundPacDimensionHandshakePacket(claimsEnabled, partiesEnabled);
			} catch(Throwable t){
				OpenPartiesAndClaims.LOGGER.error("invalid packet", t);
				return null;
			}
		}

		@Override
		public void accept(ClientboundPacDimensionHandshakePacket t, FriendlyByteBuf u) {
			CompoundTag tag = new CompoundTag();
			tag.putBoolean("c", t.claimsEnabled);
			tag.putBoolean("p", t.partiesEnabled);
			u.writeNbt(tag);
		}
		
	}
	
	public static class ClientHandler implements Consumer<ClientboundPacDimensionHandshakePacket> {
		
		@Override
		public void accept(ClientboundPacDimensionHandshakePacket t) {
			//OpenPartiesAndClaims.LOGGER.info("Received handshake for Open Parties and Claims!");
			ClientLevel world = Minecraft.getInstance().level;
			ClientWorldMainCapability mainCap = (ClientWorldMainCapability) OpenPartiesAndClaims.INSTANCE.getCapabilityHelper().getCapability((ClientLevel)world, ClientWorldCapabilityTypes.MAIN_CAP);
			mainCap.getClientWorldDataInternal().setServerHasClaimsEnabled(t.claimsEnabled);
			mainCap.getClientWorldDataInternal().setServerHasPartiesEnabled(t.partiesEnabled);
			mainCap.getClientWorldDataInternal().setServerHasMod(true);
			if(Minecraft.getInstance().screen instanceof ConfigMenu || Minecraft.getInstance().screen instanceof PlayerConfigScreen)
				Minecraft.getInstance().setScreen(Minecraft.getInstance().screen);
		}
		
	}
	
}
