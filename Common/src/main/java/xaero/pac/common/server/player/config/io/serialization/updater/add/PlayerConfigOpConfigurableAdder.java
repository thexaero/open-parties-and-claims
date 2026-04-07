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

package xaero.pac.common.server.player.config.io.serialization.updater.add;

import com.electronwill.nightconfig.core.Config;
import com.google.common.collect.ImmutableList;
import xaero.pac.common.server.player.config.io.serialization.updater.IPlayerConfigOpConfigurableTransformer;
import xaero.pac.common.server.player.config.io.serialization.updater.IPlayerConfigTransformer;

import java.util.List;
import java.util.Set;

//Exists so that additions to opConfigurablePlayerConfigOption can be done in the right order relative to
//other player config changes, such as renames.
public class PlayerConfigOpConfigurableAdder implements IPlayerConfigTransformer, IPlayerConfigOpConfigurableTransformer {

	private final ImmutableList<String> toAdd;

	public PlayerConfigOpConfigurableAdder(ImmutableList<String> toAdd) {
		this.toAdd = toAdd;
	}

	@Override
	public boolean needsKeys() {
		return false;
	}

	@Override
	public void transform(Config configData, Set<String> keys) {
		//does nothing because only the server config option opConfigurablePlayerConfigOption needs changes
	}

	@Override
	public boolean transformOpConfigurableList(List<String> opConfigurableOptions) {
		opConfigurableOptions.addAll(toAdd);
		return true;
	}

}
