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
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.server.lazypacket.LazyPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

public class ClientboundSubClaimPropertiesPacket extends LazyPacket<ClientboundSubClaimPropertiesPacket> {
	
	public static final int MAX_PROPERTIES = 32;
	public static final Encoder<ClientboundSubClaimPropertiesPacket> ENCODER = new Encoder<>();
	public static final Decoder DECODER = new Decoder();
	
	private final List<SubClaimProperties> properties;

	public ClientboundSubClaimPropertiesPacket(List<SubClaimProperties> properties) {
		super();
		this.properties = properties;
	}

	@Override
	protected Function<FriendlyByteBuf, ClientboundSubClaimPropertiesPacket> getDecoder() {
		return DECODER;
	}

	@Override
	protected void writeOnPrepare(FriendlyByteBuf dest) {
		CompoundTag nbt = new CompoundTag();
		ListTag propertiesListTag = new ListTag();
		for (int i = 0; i < this.properties.size(); i++) {
			SubClaimProperties propertiesEntry = this.properties.get(i);
			CompoundTag propertiesEntryNbt = new CompoundTag();
			propertiesEntryNbt.putUUID("p", propertiesEntry.playerId);
			propertiesEntryNbt.putInt("i", propertiesEntry.subConfigIndex);
			if(propertiesEntry.claimsName != null)
				propertiesEntryNbt.putString("n", propertiesEntry.claimsName);
			if(propertiesEntry.claimsColor != null)
				propertiesEntryNbt.putInt("c", propertiesEntry.claimsColor);
			propertiesListTag.add(propertiesEntryNbt);
		}
		nbt.put("l", propertiesListTag);
		dest.writeNbt(nbt);
	}
	
	public static class Decoder implements Function<FriendlyByteBuf, ClientboundSubClaimPropertiesPacket> {

		@Override
		public ClientboundSubClaimPropertiesPacket apply(FriendlyByteBuf input) {
			try {
				if(input.readableBytes() > 32768)
					return null;
				CompoundTag nbt = (CompoundTag) input.readNbt(NbtAccounter.unlimitedHeap());
				if(nbt == null)
					return null;
				ListTag propertiesListTag = nbt.getList("l", 10);
				if(propertiesListTag.size() > MAX_PROPERTIES) {
					OpenPartiesAndClaims.LOGGER.info("Received sub-claim properties list is too large!");
					return null;
				}
				List<SubClaimProperties> propertiesList = new ArrayList<>(propertiesListTag.size());
				for (int i = 0; i < propertiesListTag.size(); i++) {
					CompoundTag propertiesEntryNbt = propertiesListTag.getCompound(i);
					String claimsName = propertiesEntryNbt.getString("n");
					if(claimsName.length() > 128) {
						OpenPartiesAndClaims.LOGGER.info("Received sub-claim properties list with invalid claims name!");
						return null;
					}
					UUID playerId = propertiesEntryNbt.getUUID("p");
					int subConfigIndex = propertiesEntryNbt.getInt("i");
					Integer claimsColor = propertiesEntryNbt.contains("c", Tag.TAG_INT) ?
							propertiesEntryNbt.getInt("c") : null;
					propertiesList.add(new SubClaimProperties(playerId, subConfigIndex, claimsName, claimsColor));
				}
				return new ClientboundSubClaimPropertiesPacket(propertiesList);
			} catch(Throwable t) {
				OpenPartiesAndClaims.LOGGER.error("invalid packet", t);
				return null;
			}
		}
		
	}
	
	public static class ClientHandler extends Handler<ClientboundSubClaimPropertiesPacket> {
		
		@Override
		public void handle(ClientboundSubClaimPropertiesPacket t) {
			for (SubClaimProperties propertiesEntry : t.properties) {
				OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getClientClaimsSyncHandler().
					onSubClaimInfo(propertiesEntry.playerId, propertiesEntry.subConfigIndex, propertiesEntry.claimsName, propertiesEntry.claimsColor);
			}
		}
		
	}
	
	public static class SubClaimProperties {
		
		private final UUID playerId;
		private final int subConfigIndex;
		private final String claimsName;
		private final Integer claimsColor;
		
		public SubClaimProperties(UUID playerId, int subConfigIndex, String claimsName, Integer claimsColor) {
			super();
			this.playerId = playerId;
			this.subConfigIndex = subConfigIndex;
			this.claimsName = claimsName;
			this.claimsColor = claimsColor;
		}
		
		@Override
		public String toString() {
			return String.format("[%s, %d, %s, %d]", playerId, subConfigIndex, claimsName, claimsColor);
		}
		
	}

}
