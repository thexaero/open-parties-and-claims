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

package xaero.pac.common.server.player.config.io.serialization;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.io.ParsingMode;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.electronwill.nightconfig.toml.TomlParser;
import com.electronwill.nightconfig.toml.TomlWriter;
import xaero.pac.common.misc.ConfigUtil;
import xaero.pac.common.server.player.config.PlayerConfig;

import java.util.LinkedHashMap;
import java.util.Objects;

public class PlayerConfigSerializer {
	
	private TomlParser parser;
	private TomlWriter writer;
	
	public PlayerConfigSerializer() {
		this.parser = new TomlParser();
		this.writer = new TomlWriter();
	}
	
	public String serialize(PlayerConfig<?> config) {
		return writer.writeToString(config.getStorage());
	}
	
	public void deserializeInto(PlayerConfig<?> config, String serializedData) {
		CommentedConfig parsedData = CommentedConfig.of(LinkedHashMap::new, TomlFormat.instance());
		parser.parse(serializedData, parsedData, ParsingMode.ADD);
		PlayerConfig.SPEC.correct(parsedData);
		Config loadedConfig = parsedData;
		if(config.getPlayerId() != null && !Objects.equals(config.getPlayerId(), PlayerConfig.SERVER_CLAIM_UUID) && !Objects.equals(config.getPlayerId(), PlayerConfig.EXPIRED_CLAIM_UUID)) {
			loadedConfig = ConfigUtil.deepCopy(parsedData, LinkedHashMap::new);//removes comments
		}
		config.setStorage(loadedConfig);
	}
	
}
