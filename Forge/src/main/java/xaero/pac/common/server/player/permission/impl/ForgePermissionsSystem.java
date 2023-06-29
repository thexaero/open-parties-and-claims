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

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.server.permission.PermissionAPI;
import net.minecraftforge.server.permission.events.PermissionGatherEvent;
import net.minecraftforge.server.permission.nodes.PermissionNode;
import net.minecraftforge.server.permission.nodes.PermissionType;
import net.minecraftforge.server.permission.nodes.PermissionTypes;
import org.apache.commons.lang3.tuple.Pair;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.server.player.permission.api.IPermissionNodeAPI;
import xaero.pac.common.server.player.permission.api.IPlayerPermissionSystemAPI;
import xaero.pac.common.server.player.permission.api.UsedPermissionNodes;

import javax.annotation.Nonnull;
import java.util.*;

public class ForgePermissionsSystem implements IPlayerPermissionSystemAPI {

	private static final Map<IPermissionNodeAPI<?>, PermissionNode<?>> REGISTERED_NODES = new HashMap<>();

	public ForgePermissionsSystem(){
	}

	@Nonnull
	@Override
	public OptionalInt getIntPermission(@Nonnull ServerPlayer player, @Nonnull IPermissionNodeAPI<Integer> node) {
		PermissionNode<?> forgeNode = REGISTERED_NODES.get(node);
		if(forgeNode == null)
			return OptionalInt.empty();
		Object value = PermissionAPI.getPermission(player, forgeNode);
		if(!(value instanceof Integer integer))
			return OptionalInt.empty();
		return OptionalInt.of(integer);
	}

	@Override
	public boolean getPermission(@Nonnull ServerPlayer player, @Nonnull IPermissionNodeAPI<Boolean> node) {
		PermissionNode<?> forgeNode = REGISTERED_NODES.get(node);
		if(forgeNode == null)
			return false;
		Object value = PermissionAPI.getPermission(player, forgeNode);
		return value instanceof Boolean bool && bool;
	}

	@Nonnull
	@Override
	public <T> Optional<T> getPermissionTyped(@Nonnull ServerPlayer player, @Nonnull IPermissionNodeAPI<T> node) {
		@SuppressWarnings("unchecked")
		PermissionNode<T> forgeNode = (PermissionNode<T>) REGISTERED_NODES.get(node);
		if(forgeNode == null)
			return Optional.empty();
		T value = PermissionAPI.getPermission(player, forgeNode);
		return Optional.ofNullable(value);
	}

	public static void registerNodes(PermissionGatherEvent.Nodes event){
		UsedPermissionNodes.ALL.values().stream().map(ForgePermissionsSystem::convertNode).filter(Objects::nonNull).map(ForgePermissionsSystem::onRegister).forEach(event::addNodes);
	}

	private static <T> Pair<IPermissionNodeAPI<T>, PermissionNode<T>> convertNode(IPermissionNodeAPI<T> node){
		@SuppressWarnings("unchecked")
		PermissionType<T> type = (PermissionType<T>) (
				node.getType() == Integer.class ?
						PermissionTypes.INTEGER :
				node.getType() == Boolean.class ?
						PermissionTypes.BOOLEAN :
				node.getType() == String.class ?
						PermissionTypes.STRING :
				node.getType() == Component.class ?
						PermissionTypes.COMPONENT :
						null
				);
		if(type == null) {
			OpenPartiesAndClaims.LOGGER.warn("Unimplemented node type for the Forge Permissions API: " + node.getType());
			return null;
		}
		return Pair.of(node, new PermissionNode<>(OpenPartiesAndClaims.MOD_ID, node.getDefaultNodeString(), type, (player, uuid, context) -> null));
	}

	public static <T> PermissionNode<T> onRegister(Pair<IPermissionNodeAPI<T>, PermissionNode<T>> node){
		REGISTERED_NODES.put(node.getLeft(), node.getRight());
		return node.getRight();
	}

}
