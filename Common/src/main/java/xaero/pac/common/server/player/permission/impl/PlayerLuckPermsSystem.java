/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2023, Xaero <xaero1996@gmail.com> and contributors
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

package xaero.pac.common.server.player.permission.impl;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedDataManager;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.cacheddata.CachedPermissionData;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.query.QueryOptions;
import net.minecraft.server.level.ServerPlayer;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.server.player.permission.api.IPermissionNodeAPI;
import xaero.pac.common.server.player.permission.api.IPlayerPermissionSystemAPI;

import javax.annotation.Nonnull;
import java.util.OptionalInt;

public class PlayerLuckPermsSystem implements IPlayerPermissionSystemAPI {

	private QueryOptions queryOptions;

	@Nonnull
	@Override
	public OptionalInt getIntPermission(@Nonnull ServerPlayer player, @Nonnull IPermissionNodeAPI node) {
		User user = getUser(player);
		if(user == null)
			return OptionalInt.empty();
		ensureQueryOptions();
		CachedDataManager cachedDataManager = user.getCachedData();
		CachedMetaData cachedMetaData = cachedDataManager.getMetaData(queryOptions);
		Integer parsedInteger = cachedMetaData.getMetaValue(node.getNodeString(), Integer::parseInt).orElse(null);
		if(parsedInteger == null)
			return OptionalInt.empty();
		return OptionalInt.of(parsedInteger);
	}

	@Override
	public boolean getPermission(@Nonnull ServerPlayer player, @Nonnull IPermissionNodeAPI node) {
		User user = getUser(player);
		if(user == null)
			return false;
		ensureQueryOptions();
		CachedDataManager cachedDataManager = user.getCachedData();
		CachedPermissionData cachedPermissionData = cachedDataManager.getPermissionData(queryOptions);
		return cachedPermissionData.checkPermission(node.getNodeString()).asBoolean();
	}

	private void ensureQueryOptions(){
		if(queryOptions == null)
			queryOptions = QueryOptions.defaultContextualOptions();
	}

	private User getUser(ServerPlayer player){
		LuckPerms luckPerms = LuckPermsProvider.get();
		UserManager userManager = luckPerms.getUserManager();
		User user = null;
		if(!userManager.isLoaded(player.getUUID())) {
			try {
				user = userManager.loadUser(player.getUUID()).join();
			} catch(Throwable t){
				OpenPartiesAndClaims.LOGGER.error("suppressed exception", t);
			}
		} else
			user = userManager.getUser(player.getUUID());
		return user;
	}

}
