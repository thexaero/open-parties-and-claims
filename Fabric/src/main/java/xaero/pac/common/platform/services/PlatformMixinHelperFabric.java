/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2025, Xaero <xaero1996@gmail.com> and contributors
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
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;

public class PlatformMixinHelperFabric implements IPlatformMixinHelper<ModContainer, Version> {

	@Override
	public ModContainer getLoadingModInfo(String modId) {
		return FabricLoader.getInstance().getModContainer(modId).orElse(null);
	}

	@Override
	public Version getModVersion(ModContainer modContainer) {
		return modContainer.getMetadata().getVersion();
	}

	@Override
	public Version getVersionFromString(String versionString){
		try {
			return Version.parse(versionString);
		} catch (VersionParsingException e) {
			throw new RuntimeException(e);
		}
	}

}
