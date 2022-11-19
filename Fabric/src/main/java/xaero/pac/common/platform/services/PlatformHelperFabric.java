/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2022, Xaero <xaero1996@gmail.com> and contributors
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

package xaero.pac.common.platform.services;

import net.fabricmc.loader.api.FabricLoader;
import xaero.pac.client.controls.KeyBindingHelperFabric;
import xaero.pac.client.controls.keybinding.IKeyBindingHelper;
import xaero.pac.common.entity.EntityAccessFabric;
import xaero.pac.common.entity.IEntityAccess;
import xaero.pac.common.registry.block.BlockRegistryFabric;
import xaero.pac.common.registry.block.IBlockRegistry;
import xaero.pac.common.registry.entity.EntityRegistryFabric;
import xaero.pac.common.registry.entity.IEntityRegistry;
import xaero.pac.common.registry.item.IItemRegistry;
import xaero.pac.common.registry.item.ItemRegistryFabric;
import xaero.pac.common.server.world.IServerChunkCacheAccess;
import xaero.pac.common.server.world.ServerChunkCacheAccessFabric;

public class PlatformHelperFabric implements IPlatformHelper {

	private final BlockRegistryFabric blockRegistryFabric = new BlockRegistryFabric();
	private final EntityRegistryFabric entityRegistryFabric = new EntityRegistryFabric();
	private final ItemRegistryFabric itemRegistryFabric = new ItemRegistryFabric();
	private final KeyBindingHelperFabric keyBindingHelperFabric = new KeyBindingHelperFabric();
	private final EntityAccessFabric entityAccessFabric = new EntityAccessFabric();
	private final ServerChunkCacheAccessFabric serverChunkCacheAccessFabric = new ServerChunkCacheAccessFabric();

	@Override
	public String getPlatformName() {
		return "Fabric";
	}

	@Override
	public boolean isModLoaded(String modId) {
		return FabricLoader.getInstance().isModLoaded(modId);
	}

	@Override
	public boolean isDevelopmentEnvironment() {
		return FabricLoader.getInstance().isDevelopmentEnvironment();
	}

	@Override
	public IBlockRegistry getBlockRegistry() {
		return blockRegistryFabric;
	}

	@Override
	public IEntityRegistry getEntityRegistry() {
		return entityRegistryFabric;
	}

	@Override
	public IItemRegistry getItemRegistry() {
		return itemRegistryFabric;
	}

	@Override
	public IKeyBindingHelper getKeyBindingHelper() {
		return keyBindingHelperFabric;
	}

	@Override
	public IServerChunkCacheAccess getServerChunkCacheAccess() {
		return serverChunkCacheAccessFabric;
	}

	@Override
	public IEntityAccess getEntityAccess() {
		return entityAccessFabric;
	}

}
