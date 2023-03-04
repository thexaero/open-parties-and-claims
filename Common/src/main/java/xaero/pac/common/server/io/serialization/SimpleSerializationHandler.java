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

package xaero.pac.common.server.io.serialization;

import xaero.pac.common.server.io.ObjectManagerIOManager;
import xaero.pac.common.server.io.ObjectManagerIOObject;

public class SimpleSerializationHandler
<
S, 
I, 
T extends ObjectManagerIOObject,
M extends ObjectManagerIOManager<T, M>
> extends SerializationHandler<S, I, T, M> {
	
	private final SimpleSerializer<S, I, T, M> serializer;

	public SimpleSerializationHandler(SimpleSerializer<S, I, T, M> serializer) {
		super();
		this.serializer = serializer;
	}

	@Override
	public S serialize(T object) {
		return serializer.serialize(object);
	}

	@Override
	public T deserialize(I id, M manager, S serializedData) {
		return serializer.deserialize(id, manager, serializedData);
	}
	
	

}
