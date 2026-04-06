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

package xaero.pac.common.server.player.config.io.serialization.updater;

import com.electronwill.nightconfig.core.Config;
import com.google.common.collect.Lists;
import xaero.pac.common.server.player.config.PlayerConfig;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PlayerConfigUpdater {

	private final List<IPlayerConfigTransformer> transformers;//each added transformer increments the current version
	private final List<String> versionPath;

	public PlayerConfigUpdater() {
		this.transformers = new ArrayList<>();
		PlayerConfigTransformers.init(transformers);
		versionPath = Lists.newArrayList(PlayerConfig.PLAYER_CONFIG_ROOT, "version");
	}

	public void update(Config configData){
		int dataVersion = configData.contains(versionPath) ? configData.getInt(versionPath) : 0;
		if(dataVersion >= transformers.size())
			return;
		Set<String> keys = null;
		for (int i = dataVersion; i < transformers.size(); i++) {
			IPlayerConfigTransformer transformer = transformers.get(i);
			if(transformer.needsKeys() && keys == null)
				keys = collectKeys(configData);
			transformer.transform(configData, keys);
		}
	}

	private Set<String> collectKeys(Config configData){
		HashSet<String> keys = new HashSet<>();
		collectKeys(configData, keys, "");
		return keys;
	}

	private void collectKeys(Config configData, Set<String> keys, String parentKey){
		for (Config.Entry entry : configData.entrySet()) {
			String key = parentKey + entry.getKey();
			Object value = entry.getValue();
			if(value instanceof Config sub) {
				collectKeys(sub, keys, key + ".");
				continue;
			}
			keys.add(key);
		}
	}

	public int getVersion(){
		return transformers.size();
	}

	public List<String> getVersionPath() {
		return versionPath;
	}
}
