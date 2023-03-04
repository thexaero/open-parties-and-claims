/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2022-2023, Xaero <xaero1996@gmail.com> and contributors
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

package xaero.pac.client.gui.component;

import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.function.Function;

public class CachedComponentSupplier {

	private final Function<Object[], Component> factory;
	private Object[] registeredArgs;
	private Component cachedComponent;

	public CachedComponentSupplier(Function<Object[], Component> factory){
		this.factory = factory;
	}

	public Component get(Object... args){
		if(cachedComponent == null || !Arrays.equals(registeredArgs, args)) {
			cachedComponent = factory.apply(args);
			registeredArgs = args;
		}
		return cachedComponent;
	}

}
