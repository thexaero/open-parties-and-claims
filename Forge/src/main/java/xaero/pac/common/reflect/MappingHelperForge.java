/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2023, Xaero <xaero1996@gmail.com> and contributors
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

package xaero.pac.common.reflect;

import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class MappingHelperForge implements IMappingHelper {
	@Override
	public String fixFabricFieldMapping(Class<?> clazz, String name, String descriptor) throws NoSuchFieldException {
		throw new NoSuchFieldException();
	}

	@Override
	public String fixFabricMethodMapping(Class<?> clazz, String name, String descriptor) throws NoSuchMethodException {
		throw new NoSuchMethodException();
	}

	@Override
	public Field findForgeField(Class<?> clazz, String deobfName, String obfName) {
		try {
			return ObfuscationReflectionHelper.findField(clazz, obfName);
		} catch(Exception e){
			return null;
		}
	}

	@Override
	public Method findForgeMethod(Class<?> clazz, String deobfName, String obfName, Class<?>... parameterTypes) {
		try {
			return ObfuscationReflectionHelper.findMethod(clazz, obfName, parameterTypes);
		} catch(Exception e){
			return null;
		}
	}

}
