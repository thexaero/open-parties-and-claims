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
import net.minecraft.network.FriendlyByteBuf;
import xaero.pac.OpenPartiesAndClaims;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class ClientboundPlayerConfigAdminPacket {

	private final boolean admin;

	public ClientboundPlayerConfigAdminPacket(boolean admin) {
		super();
		this.admin = admin;
	}

	public static class Codec implements BiConsumer<ClientboundPlayerConfigAdminPacket, FriendlyByteBuf>, Function<FriendlyByteBuf, ClientboundPlayerConfigAdminPacket> {

		@Override
		public ClientboundPlayerConfigAdminPacket apply(FriendlyByteBuf input) {
			try {
				if(input.readableBytes() > 1024)
					return null;
				CompoundTag tag = input.readAnySizeNbt();
				if(tag == null)
					return null;
				boolean admin = tag.getBoolean("a");
				return new ClientboundPlayerConfigAdminPacket(admin);
			} catch(Throwable t) {
				OpenPartiesAndClaims.LOGGER.error("invalid packet ", t);
				return null;
			}
		}

		@Override
		public void accept(ClientboundPlayerConfigAdminPacket t, FriendlyByteBuf u) {
			CompoundTag tag = new CompoundTag();
			tag.putBoolean("a", t.admin);
			u.writeNbt(tag);
		}

	}

	public static class ClientHandler implements Consumer<ClientboundPlayerConfigAdminPacket> {

		@Override
		public void accept(ClientboundPlayerConfigAdminPacket t) {
			OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getPlayerConfigStorageManager().setAdmin(t.admin);
		}

	}

}
