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

package xaero.pac.common.platform;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;
import xaero.pac.client.controls.keybinding.IKeyBindingHelper;
import xaero.pac.client.controls.keybinding.KeyBindingHelperForge;
import xaero.pac.common.entity.EntityAccessForge;
import xaero.pac.common.platform.services.IPlatformHelper;
import xaero.pac.common.registry.block.BlockRegistryForge;
import xaero.pac.common.registry.block.IBlockRegistry;
import xaero.pac.common.server.world.IServerChunkCacheAccess;
import xaero.pac.common.server.world.ServerChunkCacheAccessForge;

public class ForgePlatformHelper implements IPlatformHelper {

    private final BlockRegistryForge blockRegistryForge = new BlockRegistryForge();
    private final KeyBindingHelperForge keyBindingRegistryForge = new KeyBindingHelperForge();
    private final ServerChunkCacheAccessForge serverChunkCacheAccessForge = new ServerChunkCacheAccessForge();
    private final EntityAccessForge entityAccessForge = new EntityAccessForge();

    @Override
    public String getPlatformName() {

        return "Forge";
    }

    @Override
    public boolean isModLoaded(String modId) {

        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {

        return !FMLLoader.isProduction();
    }

    @Override
    public IBlockRegistry getBlockRegistry() {
        return blockRegistryForge;
    }

    @Override
    public IKeyBindingHelper getKeyBindingRegistry() {
        return keyBindingRegistryForge;
    }

    @Override
    public IServerChunkCacheAccess getServerChunkCacheAccess() {
        return serverChunkCacheAccessForge;
    }

    @Override
    public EntityAccessForge getEntityAccess() {
        return entityAccessForge;
    }

}
