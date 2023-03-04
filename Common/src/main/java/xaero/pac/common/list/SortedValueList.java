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

package xaero.pac.common.list;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public final class SortedValueList<T extends Comparable<T>> implements Iterable<T> {

	private final List<T> storage;
	private final boolean mutable;

	private SortedValueList(List<T> storage, boolean mutable) {
		this.storage = mutable ? storage : Collections.unmodifiableList(storage);
		this.mutable = mutable;
	}
	
	private int binarySearch(T value){
		return Collections.binarySearch(storage, value);
	}

	private int fixIndex(int binarySearchResult){
		return binarySearchResult >= 0 ? binarySearchResult : (-binarySearchResult - 1);
	}

	public int size() {
		return storage.size();
	}

	public boolean isEmpty() {
		return storage.isEmpty();
	}

	public boolean contains(T value) {
		return binarySearch(value) >= 0;
	}

	public int indexOf(T value){
		return binarySearch(value);
	}

	@Nonnull
	@Override
	public Iterator<T> iterator() {
		return storage.iterator();
	}

	public Stream<T> stream(){
		return storage.stream();
	}

	public List<T> copyStorage(){
		return new ArrayList<>(storage);
	}

	public boolean add(T t) {
		if(!mutable)
			throw new IllegalStateException("not a mutable list!");
		int index = binarySearch(t);
		if(index >= 0)
			return false;
		storage.add(fixIndex(index), t);
		return true;
	}

	public boolean remove(T t) {
		if(!mutable)
			throw new IllegalStateException("not a mutable list!");
		int index = binarySearch(t);
		if(index < 0)
			return false;
		return storage.remove(index) == t;
	}

	public void clear() {
		if(!mutable)
			throw new IllegalStateException("not a mutable list!");
		storage.clear();
	}

	public T get(int index) {
		return storage.get(index);
	}

	public T remove(int index) {
		if(!mutable)
			throw new IllegalStateException("not a mutable list!");
		return storage.remove(index);
	}

	public boolean isMutable() {
		return mutable;
	}

	public static final class Builder<T extends Comparable<T>> {

		private List<T> content;
		private boolean mutable;

		private Builder(){}

		public Builder<T> setDefault(){
			setContent(null);
			setMutable(true);
			return this;
		}

		public Builder<T> setContent(List<T> content) {
			this.content = content;
			return this;
		}

		public Builder<T> setMutable(boolean mutable) {
			this.mutable = mutable;
			return this;
		}

		public SortedValueList<T> build(){
			if(content == null)
				content = new ArrayList<>();
			else
				Collections.sort(content);
			return new SortedValueList<>(content, mutable);
		}

		public static <T extends Comparable<T>> Builder<T> begin(){
			return new Builder<T>().setDefault();
		}

	}

}
