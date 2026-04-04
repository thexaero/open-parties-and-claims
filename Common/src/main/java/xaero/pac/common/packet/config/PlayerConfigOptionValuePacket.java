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

package xaero.pac.common.packet.config;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.packet.util.PacketUtils;
import xaero.pac.common.server.player.config.PlayerConfigOptionSpec;
import xaero.pac.common.server.player.config.api.PlayerConfigType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;

public class PlayerConfigOptionValuePacket extends PlayerConfigPacket {

	protected final PlayerConfigType type;
	protected final String subId;
	protected final UUID owner;
	protected final List<Entry> entries;

	public PlayerConfigOptionValuePacket(PlayerConfigType type, String subId, UUID owner, List<Entry> entries) {
		super();
		this.type = type;
		this.subId = subId;
		this.owner = owner;
		this.entries = entries;
	}
	
	public PlayerConfigType getType() {
		return type;
	}
	
	public UUID getOwner() {
		return owner;
	}
	
	public Stream<Entry> entryStream(){
		return entries.stream();
	}
	
	public int getSize() {
		return entries.size();
	}

	public String getSubId() {
		return subId;
	}

	public static abstract class Codec<P extends PlayerConfigOptionValuePacket> implements BiConsumer<P, FriendlyByteBuf>, Function<FriendlyByteBuf, P> {

		protected abstract int getSizeLimit();
		protected abstract P create(PlayerConfigType type, String subId, UUID owner, List<Entry> entries);

		@Override
		public P apply(FriendlyByteBuf input) {
			try {
				if(input.readableBytes() > getSizeLimit())
					return null;
				CompoundTag nbt = input.readAnySizeNbt();
				if(nbt == null)
					return null;
				String typeString = nbt.getString("t");
				if(typeString.length() > 100) {
					if(PacketUtils.shouldLogDeserializationError())
						OpenPartiesAndClaims.LOGGER.info("Player config type string is too long!");
					return null;
				}
				PlayerConfigType type = null;
				try {
					type = PlayerConfigType.valueOf(typeString);
				} catch(IllegalArgumentException iae) {
				}
				if(type == null) {
					if(PacketUtils.shouldLogDeserializationError())
						OpenPartiesAndClaims.LOGGER.info("Received unknown player config type!");
					return null;
				}
				String subID = nbt.contains("si") ? nbt.getString("si") : null;
				if(subID != null && subID.length() > 100) {
					if(PacketUtils.shouldLogDeserializationError())
						OpenPartiesAndClaims.LOGGER.info("Player config sub ID string is too long!");
					return null;
				}
				UUID owner = type != PlayerConfigType.PLAYER || nbt.getBoolean("co") ? null : nbt.getUUID("o");
				ListTag entryListTag = nbt.getList("e", Tag.TAG_COMPOUND);
				if(entryListTag.size() < 0 || entryListTag.size() > 512) {//there are other max size checks when reading the nbt tag, but an extra one here won't hurt
					if(PacketUtils.shouldLogDeserializationError())
						OpenPartiesAndClaims.LOGGER.info("Received an illegal player config option entry number: " + entryListTag.size());
					return null;
				}
				List<Entry> entries = new ArrayList<>(entryListTag.size());
				for(Tag e : entryListTag) {
					CompoundTag entryTag = (CompoundTag) e;
					String optionId = entryTag.getString("i");
					if(optionId.length() > 1000) {
						if(PacketUtils.shouldLogDeserializationError())
							OpenPartiesAndClaims.LOGGER.info("Received player config option id string is not allowed!");
						return null;
					}
					Tag valueTag = null;
					if(entryTag.contains("v"))
						valueTag = entryTag.get("v");
					if(valueTag instanceof StringTag stringTag) {
						if (stringTag.getAsString().length() > 1000) {
							if(PacketUtils.shouldLogDeserializationError())
								OpenPartiesAndClaims.LOGGER.info("Received a string option value that is too long: " + stringTag.getAsString().length());
							return null;
						}
					}
					boolean mutable = entryTag.getBoolean("m");
					boolean defaulted = entryTag.getBoolean("d");
					Entry entry = new Entry(optionId, valueTag, mutable, defaulted);
					entries.add(entry);
				}
				return create(type, subID, owner, entries);
			} catch(Throwable t) {
				return null;
			}
		}

		@Override
		public void accept(P t, FriendlyByteBuf u) {
			CompoundTag nbt = new CompoundTag();
			nbt.putString("t", t.getType().name());
			if(t.subId != null)
				nbt.putString("si", t.subId);
			if(t.getType() == PlayerConfigType.PLAYER) {
				nbt.putBoolean("co", t.owner == null);
				if(t.owner != null)
					nbt.putUUID("o", t.owner);
			}
			
			ListTag entryListTag = new ListTag();
			
			for(Entry entry : t.entries) {
				CompoundTag entryTag = new CompoundTag();
				entryTag.putString("i", entry.getId());
				if(entry.valueTag != null)
					entryTag.put("v", entry.valueTag);
				entryTag.putBoolean("m", entry.isMutable());
				entryTag.putBoolean("d", entry.isDefaulted());
				entryListTag.add(entryTag);
			}
			
			nbt.put("e", entryListTag);
			u.writeNbt(nbt);
		}
		
	}

	public static final class Entry {

		private final String id;
		private final Tag valueTag;
		private final boolean mutable;
		private final boolean defaulted;

		public Entry(String id, Tag valueTag, boolean mutable, boolean defaulted) {
			this.id = id;
			this.valueTag = valueTag;
			this.mutable = mutable;
			this.defaulted = defaulted;
		}

		public String getId() {
			return id;
		}

		public Tag getValueTag() {
			return valueTag;
		}

		public boolean isMutable() {
			return mutable;
		}

		public boolean isDefaulted() {
			return defaulted;
		}

		public static <T> Entry of(PlayerConfigOptionSpec<T> option, T value, boolean mutable, boolean defaulted){
			Tag valueTag = value == null ? null : option.getValueType().getSyncEncoder().apply(value);
			return new Entry(option.getId(), valueTag, mutable, defaulted);
		}

	}
	
}
