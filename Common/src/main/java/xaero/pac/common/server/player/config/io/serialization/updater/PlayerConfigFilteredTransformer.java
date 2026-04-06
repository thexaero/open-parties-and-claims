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

import java.util.Set;
import java.util.function.Predicate;

public abstract class PlayerConfigFilteredTransformer implements IPlayerConfigTransformer {

	private final Predicate<String> optionFilter;

	public PlayerConfigFilteredTransformer(Predicate<String> optionFilter) {
		this.optionFilter = optionFilter;
	}

	public Predicate<String> getOptionFilter() {
		return optionFilter;
	}

	@Override
	public boolean needsKeys() {
		return true;
	}

	@Override
	public void transform(Config configData, Set<String> keys) {
		for (String key : keys)
			if(optionFilter.test(key))
				transformEntry(configData, key, keys);
	}

	protected abstract void transformEntry(Config configData, String key, Set<String> keys);

}
