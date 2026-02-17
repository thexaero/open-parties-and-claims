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
import xaero.pac.common.platform.MixinServices;
import xaero.pac.common.platform.services.IPlatformMixinHelper;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class MixinPlugin implements IMixinConfigPlugin {

	private static final Map<String, String> MIXIN_MOD_REQ_MAP = ImmutableMap.of(
	);

	//almost no mod needs to use the following field for class-check-based mod detection, probably just Optifine
	public static final Map<String, String> MOD_REQ_CLASS_CHECKS = ImmutableMap.of(
			"optifine", "optifine.Patcher"
	);

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		String modReq = MIXIN_MOD_REQ_MAP.get(mixinClassName);
		if(modReq == null){
			int mixinPackageIndex = mixinClassName.indexOf(".mixin.");
			String relativeMixinPath = mixinClassName.substring(mixinPackageIndex + 7);
			int relativeDotIndex = relativeMixinPath.lastIndexOf('.');
			if(relativeDotIndex == -1)
				return true;
			modReq = relativeMixinPath.substring(0, relativeDotIndex);
		}
		String[] modReqArgs = modReq.split("\\.");
		String modId = modReqArgs[0];
		boolean isBreakRequirement = modReqArgs.length > 1 && modReqArgs[1].equals("breaks");
		String classCheck = MOD_REQ_CLASS_CHECKS.get(modId);
		if(classCheck != null){
			try {
				Class.forName(classCheck);
				return !isBreakRequirement;
			} catch(ClassNotFoundException cnfe){
				return isBreakRequirement;
			}
		}
		String minVersion = null;
		String maxVersion = null;
		int minVersionArgIndex = 1;
		if(isBreakRequirement)
			minVersionArgIndex++;
		if(modReqArgs.length > minVersionArgIndex)
			minVersion = modReqArgs[minVersionArgIndex].substring(1)
					.replaceAll("_", ".")
					.replaceAll("H", "-")
					.replaceAll("P", "+");
		if(modReqArgs.length > minVersionArgIndex + 1)
			maxVersion = modReqArgs[minVersionArgIndex + 1].substring(1)
					.replaceAll("_", ".")
					.replaceAll("H", "-")
					.replaceAll("P", "+");
		return isBreakRequirement != isModRequirementMet(MixinServices.PLATFORM, modId, minVersion, maxVersion);
	}

	public <M, V extends Comparable<V>> boolean isModRequirementMet(
			IPlatformMixinHelper<M, V> platform,
			String modId,
			String minVersionString,
			String maxVersionString
	) {
		M modInfo = platform.getLoadingModInfo(modId);
		if(modInfo == null)
			return false;
		if(minVersionString == null && maxVersionString == null)
			return true;
		V installedVersion = platform.getModVersion(modInfo);
		if(minVersionString != null) {
			V minVersion = platform.getVersionFromString(minVersionString);
			if(installedVersion.compareTo(minVersion) < 0)
				return false;
		}
		if(maxVersionString == null)
			return true;
		V maxVersion = platform.getVersionFromString(maxVersionString);
		return installedVersion.compareTo(maxVersion) <= 0;
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
