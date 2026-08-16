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

package xaero.pac.common.packet.config;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.server.config.ServerConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class ClientboundPlayerConfigConfigurableOptionsPacket {

	private final List<String> playerConfigurableOptions;
	private final List<String> opConfigurableOptions;

	public ClientboundPlayerConfigConfigurableOptionsPacket(
			List<String> playerConfigurableOptions,
			List<String> opConfigurableOptions
	) {
		super();
		this.playerConfigurableOptions = playerConfigurableOptions;
		this.opConfigurableOptions = opConfigurableOptions;
	}

	public static class Codec implements BiConsumer<ClientboundPlayerConfigConfigurableOptionsPacket, FriendlyByteBuf>, Function<FriendlyByteBuf, ClientboundPlayerConfigConfigurableOptionsPacket> {

		@Override
		public ClientboundPlayerConfigConfigurableOptionsPacket apply(FriendlyByteBuf input) {
			try {
				if(input.readableBytes() > 32768)
					return null;
				CompoundTag tag = input.readAnySizeNbt();
				if(tag == null)
					return null;
				ListTag playerConfigurableListTag = tag.getList("p", Tag.TAG_STRING);
				ListTag opConfigurableListTag = tag.getList("o", Tag.TAG_STRING);
				List<String> playerConfigurableOptions = new ArrayList<>();
				for (Tag t : playerConfigurableListTag) {
					if(!(t instanceof StringTag optionTag))
						return null;
					playerConfigurableOptions.add(optionTag.getAsString());
				}
				List<String> opConfigurableOptions = new ArrayList<>();
				for (Tag t : opConfigurableListTag) {
					if(!(t instanceof StringTag optionTag))
						return null;
					opConfigurableOptions.add(optionTag.getAsString());
				}
				return new ClientboundPlayerConfigConfigurableOptionsPacket(playerConfigurableOptions, opConfigurableOptions);
			} catch(Throwable t) {
				OpenPartiesAndClaims.LOGGER.error("invalid packet ", t);
				return null;
			}
		}

		@Override
		public void accept(ClientboundPlayerConfigConfigurableOptionsPacket t, FriendlyByteBuf u) {
			CompoundTag tag = new CompoundTag();
			ListTag playerConfigurableListTag = new ListTag();
			for (String option : t.playerConfigurableOptions)
				playerConfigurableListTag.add(StringTag.valueOf(option));
			tag.put("p", playerConfigurableListTag);
			ListTag opConfigurableListTag = new ListTag();
			for (String option : t.opConfigurableOptions)
				opConfigurableListTag.add(StringTag.valueOf(option));
			tag.put("o", opConfigurableListTag);
			u.writeNbt(tag);
		}

	}

	public static class ClientHandler implements Consumer<ClientboundPlayerConfigConfigurableOptionsPacket> {

		@Override
		public void accept(ClientboundPlayerConfigConfigurableOptionsPacket t) {
			OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getPlayerConfigStorageManager().setConfigurableOptions(t.playerConfigurableOptions, t.opConfigurableOptions);
		}

	}

	public static ClientboundPlayerConfigConfigurableOptionsPacket fromServerConfig(){
		List<String> playerConfigurableOptions = new ArrayList<>(ServerConfig.CONFIG.playerConfigurablePlayerConfigOptions.get());
		List<String> opConfigurableOptions = new ArrayList<>(ServerConfig.CONFIG.opConfigurablePlayerConfigOptions.get());
		return new ClientboundPlayerConfigConfigurableOptionsPacket(playerConfigurableOptions, opConfigurableOptions);
	}

}
