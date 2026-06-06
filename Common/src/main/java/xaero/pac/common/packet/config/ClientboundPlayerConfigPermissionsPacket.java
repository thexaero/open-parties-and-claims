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
import xaero.pac.client.player.config.IPlayerConfigClientStorage;
import xaero.pac.client.player.config.IPlayerConfigClientStorageManager;
import xaero.pac.client.player.config.IPlayerConfigStringableOptionClientStorage;
import xaero.pac.common.server.player.config.PlayerConfig;
import xaero.pac.common.server.player.config.api.PlayerConfigType;

public class ClientboundPlayerConfigPermissionsPacket extends ClientboundPlayerConfigAbstractStatePacket {

	private final boolean view;
	private final boolean edit;
	private final boolean includePlayersInGroups;
	private final boolean includeGroupsInGroups;
	private final boolean createGroups;
	private final boolean claimAs;

	public ClientboundPlayerConfigPermissionsPacket(
			PlayerConfigType type,
			boolean view,
			boolean edit,
			boolean includePlayersInGroups,
			boolean includeGroupsInGroups,
			boolean createGroups,
			boolean claimAs
	) {
		super(type, false, PlayerConfig.MAIN_SUB_ID);
		this.view = view;
		this.edit = edit;
		this.includePlayersInGroups = includePlayersInGroups;
		this.includeGroupsInGroups = includeGroupsInGroups;
		this.createGroups = createGroups;
		this.claimAs = claimAs;
	}

	public static class Codec extends ClientboundPlayerConfigAbstractStatePacket.Codec<ClientboundPlayerConfigPermissionsPacket> {

		@Override
		protected ClientboundPlayerConfigPermissionsPacket decode(CompoundTag nbt, PlayerConfigType type, boolean otherPlayer, String subId) {
			boolean view = nbt.getBooleanOr("v", false);
			boolean edit = nbt.getBooleanOr("e", false);
			boolean includePlayersInGroups = nbt.getBooleanOr("gp", false);
			boolean includeGroupsInGroups = nbt.getBooleanOr("gg", false);
			boolean createGroups = nbt.getBooleanOr("g", false);
			boolean claimAs = nbt.getBooleanOr("c", false);
			return new ClientboundPlayerConfigPermissionsPacket(
					type, view, edit, includePlayersInGroups,
					includeGroupsInGroups, createGroups,
					claimAs);
		}

		@Override
		protected void encode(ClientboundPlayerConfigPermissionsPacket packet, CompoundTag nbt) {
			nbt.putBoolean("v", packet.view);
			nbt.putBoolean("e", packet.edit);
			nbt.putBoolean("gp", packet.includePlayersInGroups);
			nbt.putBoolean("gg", packet.includeGroupsInGroups);
			nbt.putBoolean("g", packet.createGroups);
			nbt.putBoolean("c", packet.claimAs);
		}

		@Override
		protected int getExtraSizeLimit() {
			return 0;
		}

	}

	public static class ClientHandler extends ClientboundPlayerConfigAbstractStatePacket.ClientHandler<ClientboundPlayerConfigPermissionsPacket> {

		@Override
		protected void accept(
				ClientboundPlayerConfigPermissionsPacket t,
				IPlayerConfigClientStorageManager<IPlayerConfigClientStorage<IPlayerConfigStringableOptionClientStorage<?>>> playerConfigStorageManager, IPlayerConfigClientStorage<IPlayerConfigStringableOptionClientStorage<?>>
						storage
		) {
			if(storage == null)
				return;
			storage.getPermissions().update(
					t.view, t.edit, t.includePlayersInGroups,
					t.includeGroupsInGroups, t.createGroups, t.claimAs
			);
		}

	}

}
