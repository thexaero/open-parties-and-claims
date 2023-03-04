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

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.client.player.config.IPlayerConfigClientStorage;
import xaero.pac.client.player.config.IPlayerConfigClientStorageManager;
import xaero.pac.client.player.config.IPlayerConfigStringableOptionClientStorage;
import xaero.pac.common.server.player.config.PlayerConfig;
import xaero.pac.common.server.player.config.api.PlayerConfigType;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class ClientboundPlayerConfigAbstractStatePacket extends PlayerConfigPacket {

	private final PlayerConfigType type;
	private final boolean otherPlayer;
	private final String subId;

	protected ClientboundPlayerConfigAbstractStatePacket(PlayerConfigType type, boolean otherPlayer, String subId){
		this.type = type;
		this.otherPlayer = otherPlayer;
		this.subId = subId;
	}

	public PlayerConfigType getType() {
		return type;
	}

	public boolean isOtherPlayer() {
		return otherPlayer;
	}

	public String getSubId() {
		return subId;
	}

	public static abstract class Codec<P extends ClientboundPlayerConfigAbstractStatePacket> implements BiConsumer<P, FriendlyByteBuf>, Function<FriendlyByteBuf, P> {

		protected abstract P decode(CompoundTag nbt, PlayerConfigType type, boolean otherPlayer, String subId);
		protected abstract void encode(P packet, CompoundTag nbt);
		protected abstract int getExtraNbtAccounterSize();

		@Override
		public P apply(FriendlyByteBuf input) {
			try {
				CompoundTag nbt = input.readNbt(new NbtAccounter(2048 + getExtraNbtAccounterSize()));
				if(nbt == null)
					return null;
				String typeString = nbt.getString("t");
				if(typeString.length() > 100) {
					OpenPartiesAndClaims.LOGGER.info("Player config type string is too long!");
					return null;
				}
				PlayerConfigType type = null;
				try {
					type = PlayerConfigType.valueOf(typeString);
				} catch(IllegalArgumentException iae) {
				}
				if(type == null) {
					OpenPartiesAndClaims.LOGGER.info("Received unknown player config type!");
					return null;
				}
				boolean otherPlayer = false;
				if(type == PlayerConfigType.PLAYER){
					if(!nbt.contains("o", Tag.TAG_BYTE)) {
						OpenPartiesAndClaims.LOGGER.info("Unknown player config owner!");
						return null;
					}
					otherPlayer = nbt.getBoolean("o");
				}
				String subId = nbt.getString("si");
				if(subId.length() > 100) {
					OpenPartiesAndClaims.LOGGER.info("Player config sub ID string is too long!");
					return null;
				}
				return decode(nbt, type, otherPlayer, subId);
			} catch(Throwable t) {
				return null;
			}
		}

		@Override
		public void accept(P t, FriendlyByteBuf u) {
			CompoundTag nbt = new CompoundTag();
			nbt.putString("t", t.getType().toString());
			nbt.putString("si", t.getSubId());
			if(t.getType() == PlayerConfigType.PLAYER)
				nbt.putBoolean("o", t.isOtherPlayer());
			encode(t, nbt);
			u.writeNbt(nbt);
		}

	}

	public static abstract class ClientHandler<P extends ClientboundPlayerConfigAbstractStatePacket> implements Consumer<P> {

		protected abstract void accept(P t, IPlayerConfigClientStorageManager<IPlayerConfigClientStorage<IPlayerConfigStringableOptionClientStorage<?>>>
											   playerConfigStorageManager,
									   IPlayerConfigClientStorage<IPlayerConfigStringableOptionClientStorage<?>> storage);

		@Override
		public void accept(P t) {
			IPlayerConfigClientStorageManager<IPlayerConfigClientStorage<IPlayerConfigStringableOptionClientStorage<?>>>
					playerConfigStorageManager = OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getPlayerConfigStorageManager();
			IPlayerConfigClientStorage<IPlayerConfigStringableOptionClientStorage<?>> storage = PlayerConfigPacketUtil.getTargetConfig(t.isOtherPlayer(), t.getType(), playerConfigStorageManager);
			if(storage == null)
				return;
			String subId = t.getSubId();
			if(subId.isEmpty() || !subId.equals(PlayerConfig.MAIN_SUB_ID) && !storage.subConfigExists(subId))
				return;
			storage = storage.getEffectiveSubConfig(subId);
			accept(t, playerConfigStorageManager, storage);
		}

	}

}
