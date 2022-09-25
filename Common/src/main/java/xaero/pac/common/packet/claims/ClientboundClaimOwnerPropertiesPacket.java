/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2022, Xaero <xaero1996@gmail.com> and contributors
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
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.network.FriendlyByteBuf;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.server.lazypacket.LazyPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

public class ClientboundClaimOwnerPropertiesPacket extends LazyPacket<LazyPacket.Encoder<ClientboundClaimOwnerPropertiesPacket>, ClientboundClaimOwnerPropertiesPacket> {

	public static final int MAX_PROPERTIES = 32;
	public static final Encoder<ClientboundClaimOwnerPropertiesPacket> ENCODER = new Encoder<>();

	private final List<PlayerProperties> properties;

	public ClientboundClaimOwnerPropertiesPacket(List<PlayerProperties> properties) {
		super();
		this.properties = properties;
	}

	@Override
	protected Encoder<ClientboundClaimOwnerPropertiesPacket> getEncoder() {
		return ENCODER;
	}

	@Override
	protected void writeOnPrepare(Encoder<ClientboundClaimOwnerPropertiesPacket> encoder, FriendlyByteBuf dest) {
		CompoundTag nbt = new CompoundTag();
		ListTag propertiesListTag = new ListTag();
		for (int i = 0; i < this.properties.size(); i++) {
			PlayerProperties propertiesEntry = this.properties.get(i);
			CompoundTag propertiesEntryNbt = new CompoundTag();
			propertiesEntryNbt.putUUID("p", propertiesEntry.playerId);
			propertiesEntryNbt.putString("u", propertiesEntry.username);
			propertiesListTag.add(propertiesEntryNbt);
		}
		nbt.put("l", propertiesListTag);
		dest.writeNbt(nbt);
	}
	
	public static class Decoder implements Function<FriendlyByteBuf, ClientboundClaimOwnerPropertiesPacket> {

		@Override
		public ClientboundClaimOwnerPropertiesPacket apply(FriendlyByteBuf input) {
			try {
				CompoundTag nbt = input.readNbt(new NbtAccounter(32768));
				if(nbt == null)
					return null;
				ListTag propertiesListTag = nbt.getList("l", 10);
				if(propertiesListTag.size() > MAX_PROPERTIES) {
					OpenPartiesAndClaims.LOGGER.info("Received claim owner properties list is too large!");
					return null;
				}
				List<PlayerProperties> propertiesList = new ArrayList<>(propertiesListTag.size());
				for (int i = 0; i < propertiesListTag.size(); i++) {
					CompoundTag propertiesEntryNbt = propertiesListTag.getCompound(i);
					String username = propertiesEntryNbt.getString("u");
					if(username.isEmpty() || username.length() > 128) {
						OpenPartiesAndClaims.LOGGER.info("Received claim owner properties list with invalid player username!");
						return null;
					}
					UUID playerId = propertiesEntryNbt.getUUID("p");
					propertiesList.add(new PlayerProperties(playerId, username));
				}
				return new ClientboundClaimOwnerPropertiesPacket(propertiesList);
			} catch(Throwable t) {
				OpenPartiesAndClaims.LOGGER.error("invalid packet", t);
				return null;
			}
		}
		
	}
	
	public static class ClientHandler implements Consumer<ClientboundClaimOwnerPropertiesPacket> {
		
		@Override
		public void accept(ClientboundClaimOwnerPropertiesPacket t) {
			for (PlayerProperties propertiesEntry : t.properties) {
				OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getClientClaimsSyncHandler().
					onPlayerInfo(propertiesEntry.playerId, propertiesEntry.username);
			}
		}
		
	}
	
	public static class PlayerProperties {
		
		private final UUID playerId;
		private final String username;
		
		public PlayerProperties(UUID playerId, String username) {
			super();
			this.playerId = playerId;
			this.username = username;
		}
		
		@Override
		public String toString() {
			return String.format("[%s, %s]", playerId, username);
		}
		
	}

}
