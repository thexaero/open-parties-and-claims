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

package xaero.pac.common.server.io;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ObjectManagerIOToSaveTracker<
		T extends ObjectManagerIOObject
> implements Iterable<T> {

	private final ObjectManagerIO<?, ?, T, ?> io;
	private final Set<T> toSave;
	private final Set<T> toSaveLater;

	public ObjectManagerIOToSaveTracker(ObjectManagerIO<?, ?, T, ?> io, Set<T> toSave, Set<T> toSaveLater) {
		this.io = io;
		this.toSave = toSave;
		this.toSaveLater = toSaveLater;
	}

	public void add(T object){
		if(io.isLiveSaving()){
			toSaveLater.add(object);
			return;
		}
		toSave.add(object);
	}

	public void remove(T object){
		toSave.remove(object);
		toSaveLater.remove(object);
	}

	public void beforeLiveSave(){
		toSave.addAll(toSaveLater);
		toSaveLater.clear();
	}

	@Nonnull
	@Override
	public Iterator<T> iterator() {
		return toSave.iterator();
	}

	public static final class Builder<
			T extends ObjectManagerIOObject
	> {

		private ObjectManagerIO<?, ?, T, ?> io;

		private Builder(){}

		public Builder<T> setDefault(){
			setIo(null);
			return this;
		}

		public Builder<T> setIo(ObjectManagerIO<?, ?, T, ?> io){
			this.io = io;
			return this;
		}

		public ObjectManagerIOToSaveTracker<T> build(){
			if(io == null)
				throw new IllegalStateException();
			return new ObjectManagerIOToSaveTracker<>(io, new HashSet<>(), new HashSet<>());
		}

		public static <T extends ObjectManagerIOObject> Builder<T> begin(){
			return new Builder<T>().setDefault();
		}

	}


}
