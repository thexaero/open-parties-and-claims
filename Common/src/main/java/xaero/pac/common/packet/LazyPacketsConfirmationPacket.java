/*
 *     Open Parties and Claims - adds chunk claims and player parties to Minecraft
 *     Copyright (C) 2022, Xaero <xaero1996@gmail.com> and contributors
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of version 3 of the GNU Lesser General Public License
 *     (LGPL-3.0-only) as published by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received copies of the GNU Lesser General Public License
 *     and the GNU General Public License along with this program.
 *     If not, see <https://www.gnu.org/licenses/>.
 */

package xaero.pac.common.packet;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.server.ServerData;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class LazyPacketsConfirmationPacket {

	public static class Codec implements BiConsumer<LazyPacketsConfirmationPacket, FriendlyByteBuf>, Function<FriendlyByteBuf, LazyPacketsConfirmationPacket> {
		
		@Override
		public LazyPacketsConfirmationPacket apply(FriendlyByteBuf input) {
			try {
				input.readNbt(new NbtAccounter(2048));
				return new LazyPacketsConfirmationPacket();
			} catch(Throwable t) {
				return null;
			}
		}

		@Override
		public void accept(LazyPacketsConfirmationPacket t, FriendlyByteBuf u) {
			CompoundTag tag = new CompoundTag();
			u.writeNbt(tag);
		}
		
	}
	
	public static class ServerHandler implements BiConsumer<LazyPacketsConfirmationPacket,ServerPlayer> {
		
		@Override
		public void accept(LazyPacketsConfirmationPacket t, ServerPlayer player) {
			ServerData.from(player.getServer()).getServerTickHandler().getLazyPacketSender().onConfirmation(player);
		}
		
	}

	public static class ClientHandler implements Consumer<LazyPacketsConfirmationPacket> {

		@Override
		public void accept(LazyPacketsConfirmationPacket t) {
			OpenPartiesAndClaims.INSTANCE.getPacketHandler().sendToServer(t);
		}

	}
	
}
