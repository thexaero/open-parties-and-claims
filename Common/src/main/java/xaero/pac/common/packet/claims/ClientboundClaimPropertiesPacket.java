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
import xaero.pac.common.server.lazypackets.LazyPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

public class ClientboundClaimPropertiesPacket extends LazyPacket<LazyPacket.Encoder<ClientboundClaimPropertiesPacket>, ClientboundClaimPropertiesPacket> {
	
	public static final int MAX_PROPERTIES = 32;
	public static final Encoder<ClientboundClaimPropertiesPacket> ENCODER = new Encoder<>();
	
	private final List<PlayerProperties> properties;

	public ClientboundClaimPropertiesPacket(List<PlayerProperties> properties) {
		super();
		this.properties = properties;
	}

	@Override
	protected Encoder<ClientboundClaimPropertiesPacket> getEncoder() {
		return ENCODER;
	}

	@Override
	protected void writeOnPrepare(Encoder<ClientboundClaimPropertiesPacket> encoder, FriendlyByteBuf dest) {
		CompoundTag nbt = new CompoundTag();
		ListTag propertiesListTag = new ListTag();
		for (int i = 0; i < this.properties.size(); i++) {
			PlayerProperties propertiesEntry = this.properties.get(i);
			CompoundTag propertiesEntryNbt = new CompoundTag();
			propertiesEntryNbt.putUUID("p", propertiesEntry.playerId);
			propertiesEntryNbt.putString("u", propertiesEntry.username);
			propertiesEntryNbt.putString("n", propertiesEntry.claimsName);
			propertiesEntryNbt.putInt("c", propertiesEntry.claimsColor);
			propertiesListTag.add(propertiesEntryNbt);
		}
		nbt.put("l", propertiesListTag);
		dest.writeNbt(nbt);
	}
	
	public static class Decoder implements Function<FriendlyByteBuf, ClientboundClaimPropertiesPacket> {

		@Override
		public ClientboundClaimPropertiesPacket apply(FriendlyByteBuf input) {
			try {
				CompoundTag nbt = input.readNbt(new NbtAccounter(32768));
				ListTag propertiesListTag = nbt.getList("l", 10);
				if(propertiesListTag.size() > MAX_PROPERTIES) {
					OpenPartiesAndClaims.LOGGER.info("Received claim properties list is too large!");
					return null;
				}
				List<PlayerProperties> propertiesList = new ArrayList<>(propertiesListTag.size());
				for (int i = 0; i < propertiesListTag.size(); i++) {
					CompoundTag propertiesEntryNbt = propertiesListTag.getCompound(i);
					String username = propertiesEntryNbt.getString("u");
					if(username == null || username.isEmpty() || username.length() > 128) {
						OpenPartiesAndClaims.LOGGER.info("Received claim properties list with invalid player username!");
						return null;
					}
					String claimsName = propertiesEntryNbt.getString("n");
					if(claimsName.length() > 128) {
						OpenPartiesAndClaims.LOGGER.info("Received claim properties list with invalid claims name!");
						return null;
					}
					UUID playerId = propertiesEntryNbt.getUUID("p");
					int claimsColor = propertiesEntryNbt.getInt("c");
					propertiesList.add(new PlayerProperties(playerId, username, claimsName, claimsColor));
				}
				return new ClientboundClaimPropertiesPacket(propertiesList);
			} catch(Throwable t) {
				OpenPartiesAndClaims.LOGGER.error("invalid packet", t);
				return null;
			}
		}
		
	}
	
	public static class ClientHandler implements Consumer<ClientboundClaimPropertiesPacket> {
		
		@Override
		public void accept(ClientboundClaimPropertiesPacket t) {
			for (PlayerProperties propertiesEntry : t.properties) {
				OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getClientClaimsSyncHandler().
					onPlayerInfo(propertiesEntry.playerId, propertiesEntry.username, propertiesEntry.claimsName, propertiesEntry.claimsColor);
			}
		}
		
	}
	
	public static class PlayerProperties {
		
		private final UUID playerId;
		private final String username;
		private final String claimsName;
		private final int claimsColor;
		
		public PlayerProperties(UUID playerId, String username, String claimsName, int claimsColor) {
			super();
			this.playerId = playerId;
			this.username = username;
			this.claimsName = claimsName;
			this.claimsColor = claimsColor;
		}
		
		@Override
		public String toString() {
			return String.format("[%s, %s, %s, %d]", playerId, username, claimsName, claimsColor);
		}
		
	}

}
