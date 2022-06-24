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

public class ForgeConfigEnumValueWrapper<T extends Enum<T>> implements IForgeConfigEnumValue<T>{

	private final ForgeConfigSpec.EnumValue<T> enumValue;

	public ForgeConfigEnumValueWrapper(ForgeConfigSpec.EnumValue<T> enumValue) {
		this.enumValue = enumValue;
	}

	@Override
	public List<String> getPath() {
		return enumValue.getPath();
	}

	@Override
	public T get() {
		return enumValue.get();
	}

	@Override
	public IForgeConfigSpecBuilder next() {
		return new ForgeConfigSpecBuilderWrapper(enumValue.next());
	}

	@Override
	public void save() {
		enumValue.save();
	}

	@Override
	public void set(T value) {
		enumValue.set(value);
	}

	@Override
	public void clearCache() {
		enumValue.clearCache();
	}

}
