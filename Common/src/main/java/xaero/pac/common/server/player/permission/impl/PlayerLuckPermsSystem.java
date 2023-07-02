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
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.server.player.permission.api.IPermissionNodeAPI;
import xaero.pac.common.server.player.permission.api.IPlayerPermissionSystemAPI;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Function;

public class PlayerLuckPermsSystem implements IPlayerPermissionSystemAPI {

	private QueryOptions queryOptions;
	private static final Map<Class<?>, Function<String, ?>> PARSERS = Map.of(
			Integer.class, Integer::valueOf,
			Double.class, Double::valueOf,
			Float.class, Float::valueOf,
			Long.class, Long::valueOf,
			Short.class, Short::valueOf,
			Byte.class, Byte::valueOf,
			String.class, Function.identity(),
			Component.class, TextComponent::new
	);

	@Nonnull
	@Override
	public OptionalInt getIntPermission(@Nonnull ServerPlayer player, @Nonnull IPermissionNodeAPI<Integer> node) {
		CachedMetaData cachedMetaData = getCachedMetaData(player);
		if(cachedMetaData == null)
			return OptionalInt.empty();
		Integer parsedInteger = cachedMetaData.getMetaValue(node.getNodeString(), Integer::parseInt).orElse(null);
		if(parsedInteger == null)
			return OptionalInt.empty();
		return OptionalInt.of(parsedInteger);
	}

	@Override
	public boolean getPermission(@Nonnull ServerPlayer player, @Nonnull IPermissionNodeAPI<Boolean> node) {
		User user = getUser(player);
		if(user == null)
			return false;
		ensureQueryOptions();
		CachedDataManager cachedDataManager = user.getCachedData();
		CachedPermissionData cachedPermissionData = cachedDataManager.getPermissionData(queryOptions);
		return cachedPermissionData.checkPermission(node.getNodeString()).asBoolean();
	}

	@Nonnull
	@Override
	public <T> Optional<T> getPermissionTyped(@Nonnull ServerPlayer player, @Nonnull IPermissionNodeAPI<T> node) {
		if(node.getType() == Boolean.class) {
			@SuppressWarnings("unchecked")
			Optional<T> booleanValue = (Optional<T>) Optional.of(getPermission(player, (IPermissionNodeAPI<Boolean>) node));
			return booleanValue;
		}
		CachedMetaData cachedMetaData = getCachedMetaData(player);
		if(cachedMetaData == null)
			return Optional.empty();
		@SuppressWarnings("unchecked")
		Function<String, T> parser = (Function<String, T>)PARSERS.get(node.getType());
		if(parser == null)
			return Optional.empty();
		T parsedValue = cachedMetaData.getMetaValue(node.getNodeString(), parser).orElse(null);
		return Optional.ofNullable(parsedValue);
	}

	private void ensureQueryOptions(){
		if(queryOptions == null)
			queryOptions = QueryOptions.defaultContextualOptions();
	}

	private User getUser(ServerPlayer player){
		LuckPerms luckPerms;
		try {
			luckPerms = LuckPermsProvider.get();
		} catch(Exception ignored){
			//LuckPerms shuts down when the server is stopped before players are logged out...
			return null;
		}
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

	private CachedMetaData getCachedMetaData(ServerPlayer player){
		User user = getUser(player);
		if(user == null)
			return null;
		ensureQueryOptions();
		CachedDataManager cachedDataManager = user.getCachedData();
		return cachedDataManager.getMetaData(queryOptions);
	}

}
