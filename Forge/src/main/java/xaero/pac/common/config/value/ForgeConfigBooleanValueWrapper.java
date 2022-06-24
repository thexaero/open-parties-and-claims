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

package xaero.pac.common.config.value;

import net.minecraftforge.common.ForgeConfigSpec;
import xaero.pac.common.config.ForgeConfigSpecBuilderWrapper;
import xaero.pac.common.config.IForgeConfigSpecBuilder;

import java.util.List;

public class ForgeConfigBooleanValueWrapper implements IForgeConfigBooleanValue{

	private final ForgeConfigSpec.BooleanValue booleanValue;

	public ForgeConfigBooleanValueWrapper(ForgeConfigSpec.BooleanValue booleanValue) {
		this.booleanValue = booleanValue;
	}

	@Override
	public List<String> getPath() {
		return booleanValue.getPath();
	}

	@Override
	public Boolean get() {
		return booleanValue.get();
	}

	@Override
	public IForgeConfigSpecBuilder next() {
		return new ForgeConfigSpecBuilderWrapper(booleanValue.next());
	}

	@Override
	public void save() {
		booleanValue.save();
	}

	@Override
	public void set(Boolean value) {
		booleanValue.set(value);
	}

	@Override
	public void clearCache() {
		booleanValue.clearCache();
	}
}
