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

import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.LoadingModList;
import net.neoforged.fml.loading.moddiscovery.ModFileInfo;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

public class PlatformMixinHelperNeoForge implements IPlatformMixinHelper<ModFileInfo, ArtifactVersion> {

	@Override
	public ModFileInfo getLoadingModInfo(String modId) {
		return FMLLoader.getCurrent().getLoadingModList().getModFileById(modId);
	}

	@Override
	public ArtifactVersion getModVersion(ModFileInfo modFileInfo) {
		return modFileInfo.getMods().get(0).getVersion();
	}

	@Override
	public ArtifactVersion getVersionFromString(String versionString) {
		return new DefaultArtifactVersion(versionString);
	}

}
