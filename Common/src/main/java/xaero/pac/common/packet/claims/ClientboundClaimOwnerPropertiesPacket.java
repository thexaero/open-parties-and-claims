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
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.server.lazypacket.LazyPacket;
import xaero.pac.common.util.json.XaeroJsonUtils;
import xaero.pac.common.util.nbt.XaeroNbtUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

public class ClientboundClaimOwnerPropertiesPacket extends LazyPacket<ClientboundClaimOwnerPropertiesPacket> {

	public static final int MAX_PROPERTIES = 32;
	public static final Encoder<ClientboundClaimOwnerPropertiesPacket> ENCODER = new Encoder<>();
	public static final Decoder DECODER = new Decoder();

	private final List<PlayerProperties> properties;

	public ClientboundClaimOwnerPropertiesPacket(List<PlayerProperties> properties) {
		super();
		this.properties = properties;
	}

	@Override
	protected Function<FriendlyByteBuf, ClientboundClaimOwnerPropertiesPacket> getDecoder() {
		return DECODER;
	}

	@Override
	protected void writeOnPrepare(FriendlyByteBuf dest) {
		CompoundTag nbt = new CompoundTag();
		ListTag propertiesListTag = new ListTag();
		for (int i = 0; i < this.properties.size(); i++) {
			PlayerProperties propertiesEntry = this.properties.get(i);
			CompoundTag propertiesEntryNbt = new CompoundTag();
			XaeroNbtUtil.putUUID(propertiesEntryNbt, "p", propertiesEntry.playerId);
			propertiesEntryNbt.putString("u", propertiesEntry.username);
			if(propertiesEntry.partyName != null){
				String partyNameJson = XaeroJsonUtils.toJson(propertiesEntry.partyName);
				propertiesEntryNbt.putString("pn", partyNameJson);
			}
			propertiesEntryNbt.putBoolean("po", propertiesEntry.partyOwned);
			propertiesListTag.add(propertiesEntryNbt);
		}
		nbt.put("l", propertiesListTag);
		dest.writeNbt(nbt);
	}
	
	public static class Decoder implements Function<FriendlyByteBuf, ClientboundClaimOwnerPropertiesPacket> {

		@Override
		public ClientboundClaimOwnerPropertiesPacket apply(FriendlyByteBuf input) {
			try {
				if(input.readableBytes() > 65536)
					return null;
				CompoundTag nbt = (CompoundTag) input.readNbt(NbtAccounter.unlimitedHeap());
				if(nbt == null)
					return null;
				ListTag propertiesListTag = nbt.getListOrEmpty("l");
				if(propertiesListTag.size() > MAX_PROPERTIES) {
					OpenPartiesAndClaims.LOGGER.info("Received claim owner properties list is too large!");
					return null;
				}
				List<PlayerProperties> propertiesList = new ArrayList<>(propertiesListTag.size());
				for (int i = 0; i < propertiesListTag.size(); i++) {
					CompoundTag propertiesEntryNbt = propertiesListTag.getCompoundOrEmpty(i);
					String username = propertiesEntryNbt.getStringOr("u", null);
					if(username == null || username.length() > 128) {
						OpenPartiesAndClaims.LOGGER.info("Received claim owner properties list with invalid player username!");
						return null;
					}
					UUID playerId = XaeroNbtUtil.getUUID(propertiesEntryNbt, "p").orElse(null);
					String partyNameJson = propertiesEntryNbt.getStringOr("pn", null);
					Component partyName = partyNameJson == null ? null : XaeroJsonUtils.fromJson(partyNameJson);
					boolean partyOwned = propertiesEntryNbt.getBooleanOr("po", false);
					propertiesList.add(new PlayerProperties(playerId, username, partyName, partyOwned));
				}
				return new ClientboundClaimOwnerPropertiesPacket(propertiesList);
			} catch(Throwable t) {
				OpenPartiesAndClaims.LOGGER.error("invalid packet", t);
				return null;
			}
		}
		
	}
	
	public static class ClientHandler extends Handler<ClientboundClaimOwnerPropertiesPacket> {
		
		@Override
		public void handle(ClientboundClaimOwnerPropertiesPacket t) {
			for (PlayerProperties propertiesEntry : t.properties) {
				OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getClientClaimsSyncHandler().
					onPlayerInfo(propertiesEntry.playerId, propertiesEntry.username, propertiesEntry.partyName, propertiesEntry.partyOwned);
			}
		}
		
	}
	
	public static class PlayerProperties {
		
		private final UUID playerId;
		private final String username;
		private final Component partyName;
		private final boolean partyOwned;
		
		public PlayerProperties(UUID playerId, String username, Component partyName, boolean partyOwned) {
			super();
			this.playerId = playerId;
			this.username = username;
			this.partyName = partyName;
			this.partyOwned = partyOwned;
		}
		
		@Override
		public String toString() {
			String partyNameString = partyName == null ? null : partyName.getString();
			return String.format("[%s, %s, %s, %s]", playerId, username, partyNameString, partyOwned);
		}
		
	}

}
