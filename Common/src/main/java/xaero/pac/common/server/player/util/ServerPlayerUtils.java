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

package xaero.pac.common.server.player.util;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import xaero.pac.common.server.core.accessor.IGameProfileCache;

import java.util.Locale;

public class ServerPlayerUtils {

	public static boolean playerNameIsKnown(MinecraftServer server, String name){
		if(name == null)
			return false;
		return ((IGameProfileCache)server.getProfileCache()).xaero_pac_PlayerNameIsKnown(name.toLowerCase(Locale.ROOT));
	}

	public static boolean isValidPlayerNameChar(char c){
		return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9' || c == '_';
	}

	public static void teleport(ServerPlayer player, double x, double y, double z, float yRot, float xRot){
		teleport(player, null, x, y, z, yRot, xRot);
	}

	public static void teleport(ServerPlayer player, ResourceLocation dimension, double x, double y, double z, float yRot, float xRot){
		MinecraftServer server = player.getServer();
		server.execute(() -> {
			ServerPlayer upToDatePlayer = server.getPlayerList().getPlayer(player.getUUID());
			if(upToDatePlayer != player)
				return;
			ServerLevel targetDimension = null;
			if(dimension != null) {
				targetDimension = player.getServer().getLevel(ResourceKey.create(Registry.DIMENSION_REGISTRY, dimension));
				if(targetDimension == null)
					return;
			}
			player.stopRiding();
			if(targetDimension != null && targetDimension != player.getLevel()) {
				player.teleportTo(targetDimension, x, y, z, yRot, xRot);
				return;
			}
			player.connection.teleport(x, y, z, yRot, xRot);
		});
	}

}
