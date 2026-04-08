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

package xaero.pac.common.server.player.config.io.serialization.updater.rename;

import com.electronwill.nightconfig.core.Config;
import xaero.pac.common.server.player.config.io.serialization.updater.IPlayerConfigConfigurableTransformer;
import xaero.pac.common.server.player.config.io.serialization.updater.IPlayerConfigOpConfigurableTransformer;
import xaero.pac.common.server.player.config.io.serialization.updater.PlayerConfigFilteredTransformer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

public class PlayerConfigOptionRenamer extends PlayerConfigFilteredTransformer implements IPlayerConfigConfigurableTransformer, IPlayerConfigOpConfigurableTransformer {

	private final Function<String, String> operation;
	private final Map<String, String> renamedKeys;

	public PlayerConfigOptionRenamer(Predicate<String> optionFilter, Function<String, String> operation) {
		super(optionFilter);
		this.operation = operation;
		this.renamedKeys = new HashMap<>();
	}

	@Override
	public void transform(Config configData, Set<String> keys) {
		renamedKeys.clear();
		super.transform(configData, keys);
		renamedKeys.forEach((oldKey, newKey) -> {
			keys.remove(oldKey);
			keys.add(newKey);
		});
		renamedKeys.clear();
	}

	@Override
	protected void transformEntry(Config configData, String key, Set<String> keys) {
		Object valueAtPath = configData.get(key);
		String updatedPath = operation.apply(key);
		configData.remove(key);
		configData.set(updatedPath, valueAtPath);
		renamedKeys.put(key, updatedPath);
	}

	private boolean transformList(List<String> configurableOptions) {
		String root = "playerConfig.";
		boolean madeChanges = false;
		for (int i = 0; i < configurableOptions.size(); i++) {
			String optionPath = configurableOptions.get(i);
			String prefixComment = null;
			if(optionPath.startsWith("/*")) {
				int commentEndIndex = optionPath.indexOf("*/");
				if(commentEndIndex == -1)
					continue;//not going to pass any filter anyway
				int pathStart = commentEndIndex + 2;
				prefixComment = optionPath.substring(0, pathStart);
				optionPath = optionPath.substring(pathStart);
			}
			boolean startsWithRoot = optionPath.startsWith(root);
			if(!startsWithRoot)
				optionPath = root + optionPath;
			if(!getOptionFilter().test(optionPath))
				continue;
			String updatedPath = operation.apply(optionPath);
			if(!startsWithRoot)
				updatedPath = updatedPath.substring(root.length());
			if(prefixComment != null)
				updatedPath = prefixComment + updatedPath;
//			OpenPartiesAndClaims.LOGGER.info("renamed {} to {}", optionPath, updatedPath);
			configurableOptions.set(i, updatedPath);
			madeChanges = true;
		}
		return madeChanges;
	}

	@Override
	public boolean transformConfigurableList(List<String> playerConfigurableOptions) {
		return transformList(playerConfigurableOptions);
	}

	@Override
	public boolean transformOpConfigurableList(List<String> opConfigurableOptions) {
		return transformList(opConfigurableOptions);
	}

}
