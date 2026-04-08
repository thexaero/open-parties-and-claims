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

package xaero.pac.common.server.player.config.io.serialization.updater.value;

import com.electronwill.nightconfig.core.Config;
import xaero.pac.common.server.player.config.io.serialization.updater.PlayerConfigFilteredTransformer;

import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public class PlayerConfigValueChanger extends PlayerConfigFilteredTransformer {

	private final BiFunction<Config, Object, Object> operation;

	public PlayerConfigValueChanger(Predicate<String> optionFilter, BiFunction<Config, Object, Object> operation) {
		super(optionFilter);
		this.operation = operation;
	}

	@Override
	protected void transformEntry(Config configData, String key, Set<String> keys) {
		Object valueAtPath = configData.get(key);
		Object updatedValue = operation.apply(configData, valueAtPath);
		configData.set(key, updatedValue);
//		OpenPartiesAndClaims.LOGGER.info("converted value from {} to {}", valueAtPath, updatedValue);
	}

}
