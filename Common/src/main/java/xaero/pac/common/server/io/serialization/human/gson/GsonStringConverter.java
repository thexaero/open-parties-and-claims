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

package xaero.pac.common.server.io.serialization.human.gson;

import xaero.pac.common.server.io.serialization.human.HumanReadableStringConverter;

public class GsonStringConverter<S extends GsonSnapshot, I> extends HumanReadableStringConverter<S, I> {

	private GsonSnapshotSerializer<S> gson;

	public GsonStringConverter(GsonSnapshotSerializer<S> gson) {
		super();
		this.gson = gson;
	}

	@Override
	public S convert(I id, String humanReadable) {
		return (S) gson.deserialize(humanReadable);
	}

	@Override
	public String convert(S data) {
		return gson.serialize(data);
	}

}
