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

package xaero.pac.common.packet.config;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.client.gui.PlayerConfigScreen;
import xaero.pac.client.player.config.IPlayerConfigClientStorage;
import xaero.pac.client.player.config.IPlayerConfigClientStorageManager;
import xaero.pac.client.player.config.IPlayerConfigStringableOptionClientStorage;
import xaero.pac.common.server.player.config.api.IPlayerConfigOptionSpecAPI;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class ClientboundPlayerConfigHelpPacket {

	private final String optionId;

	public ClientboundPlayerConfigHelpPacket(String optionId) {
		this.optionId = optionId;
	}

	public static class Codec implements BiConsumer<ClientboundPlayerConfigHelpPacket, FriendlyByteBuf>, Function<FriendlyByteBuf, ClientboundPlayerConfigHelpPacket> {

		@Override
		public ClientboundPlayerConfigHelpPacket apply(FriendlyByteBuf friendlyByteBuf) {
			try {
				CompoundTag tag = friendlyByteBuf.readNbt(new NbtAccounter(10240));
				if(tag == null)
					return null;
				String optionId = tag.getString("i");
				if(optionId.length() > 1000) {
					OpenPartiesAndClaims.LOGGER.info("Received player config option id string is not allowed!");
					return null;
				}
				return new ClientboundPlayerConfigHelpPacket(optionId);
			} catch(Throwable t){
				return null;
			}
		}

		@Override
		public void accept(ClientboundPlayerConfigHelpPacket t, FriendlyByteBuf friendlyByteBuf) {
			CompoundTag tag = new CompoundTag();
			tag.putString("i", t.optionId);
			friendlyByteBuf.writeNbt(tag);
		}

	}

	public static class ClientHandler implements Consumer<ClientboundPlayerConfigHelpPacket> {

		@Override
		public void accept(ClientboundPlayerConfigHelpPacket t) {
			IPlayerConfigClientStorageManager<IPlayerConfigClientStorage<IPlayerConfigStringableOptionClientStorage<?>>>
					playerConfigStorageManager = OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getPlayerConfigStorageManager();
			IPlayerConfigOptionSpecAPI<?> option = playerConfigStorageManager.getOptionForId(t.optionId);
			if(option != null) {
				Minecraft.getInstance().gui.getChat().addMessage(Component.literal(""));
				Minecraft.getInstance().gui.getChat().addMessage(PlayerConfigScreen.getUICommentForOption(option));
			}
		}

	}

}
