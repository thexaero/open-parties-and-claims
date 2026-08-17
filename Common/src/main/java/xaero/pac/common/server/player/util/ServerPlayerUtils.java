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

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.CachedUserNameToIdResolver;
import net.minecraft.server.players.UserNameToIdResolver;
import xaero.pac.common.server.core.accessor.ICachedUserNameToIdResolver;
import xaero.pac.common.server.world.ServerLevelHelper;

import java.util.HashSet;
import java.util.Locale;

public class ServerPlayerUtils {

	public static boolean playerNameIsKnown(MinecraftServer server, String name){
		if(name == null)
			return false;
		UserNameToIdResolver idResolver = server.services().nameToIdCache();
		if(!(idResolver instanceof CachedUserNameToIdResolver))
			return false;
		return ((ICachedUserNameToIdResolver)idResolver).xaero_pac_PlayerNameIsKnown(name.toLowerCase(Locale.ROOT));
	}

	public static boolean isValidPlayerNameChar(char c){
		return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9' || c == '_';
	}

	public static void teleport(ServerPlayer player, double x, double y, double z, float yRot, float xRot){
		teleport(player, null, x, y, z, yRot, xRot);
	}

	public static void teleport(ServerPlayer player, Identifier dimension, double x, double y, double z, float yRot, float xRot){
		MinecraftServer server = ServerLevelHelper.getServer(player);
		server.schedule(server.wrapRunnable(() -> {
			ServerPlayer upToDatePlayer = server.getPlayerList().getPlayer(player.getUUID());
			if(upToDatePlayer != player)
				return;
			ServerLevel targetDimension = null;
			if(dimension != null) {
				targetDimension = server.getLevel(ResourceKey.create(Registries.DIMENSION, dimension));
				if(targetDimension == null)
					return;
			}
			player.stopRiding();
			if(targetDimension != null && targetDimension != ServerLevelHelper.getServerLevel(player.level())) {
				player.teleportTo(targetDimension, x, y, z, new HashSet<>(), yRot, xRot, true);
				return;
			}
			player.connection.teleport(x, y, z, yRot, xRot);
		}));
	}

}
