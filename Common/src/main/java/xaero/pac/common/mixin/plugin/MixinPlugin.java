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

package xaero.pac.common.mixin.plugin;

import com.google.common.collect.ImmutableMap;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.platform.Services;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class MixinPlugin implements IMixinConfigPlugin {

	private final Map<String, String> MIXIN_MOD_ID_MAP = ImmutableMap.of(
	);

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		String modId = MIXIN_MOD_ID_MAP.get(mixinClassName);
		if(modId == null){
			int mixinPackageIndex = mixinClassName.indexOf(".mixin.");
			String relativeMixinPath = mixinClassName.substring(mixinPackageIndex + 7);
			int relativeDotIndex = relativeMixinPath.indexOf('.');
			if(relativeDotIndex == -1)
				return true;
			modId = relativeMixinPath.substring(0, relativeDotIndex);
		}
		if(modId == null)
			return true;
		return Services.PLATFORM.shouldApplyMixinsTargetingMod(modId);
	}

	@Override
	public void onLoad(String mixinPackage) {

	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

	}

	@Override
	public String getRefMapperConfig() {
		return null;
	}

	@Override
	public List<String> getMixins() {
		return null;
	}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

	}

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

	}

}
