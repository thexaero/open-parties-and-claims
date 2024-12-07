/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2024, Xaero <xaero1996@gmail.com> and contributors
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

package xaero.pac.common.util.list;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public class ListUniqueAdder<T> implements List<T> {

	private List<T> destinationList;
	private Set<T> checkingSet;

	public ListUniqueAdder(){
		checkingSet = new HashSet<>();
	}

	public ListUniqueAdder<T> setDestinationList(List<T> destinationList) {
		this.destinationList = destinationList;
		checkingSet.clear();
		checkingSet.addAll(destinationList);
		return this;
	}

	@Override
	public int size() {
		return destinationList.size();
	}

	@Override
	public boolean isEmpty() {
		return destinationList.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return checkingSet.contains(o);
	}

	@Nonnull
	@Override
	public Iterator<T> iterator() {
		return destinationList.iterator();
	}

	@Override
	public void forEach(Consumer<? super T> action) {
		destinationList.forEach(action);
	}

	@Nonnull
	@Override
	public Object[] toArray() {
		return destinationList.toArray();
	}

	@Nonnull
	@Override
	public <A> A[] toArray(@Nonnull A[] a) {
		return destinationList.toArray(a);
	}

	@Override
	public <A> A[] toArray(IntFunction<A[]> generator) {
		return destinationList.toArray(generator);
	}

	@Override
	public boolean add(T element) {
		if(!checkingSet.add(element))
			return false;
		if(!destinationList.add(element)) {
			checkingSet.remove(element);
			return false;
		}
		return true;
	}

	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean containsAll(@Nonnull Collection<?> c) {
		return checkingSet.containsAll(c);
	}

	@Override
	public boolean addAll(@Nonnull Collection<? extends T> c) {
		boolean result = false;
		for (T element : c)
			if(add(element))
				result = true;
		return result;
	}

	@Override
	public boolean addAll(int index, @Nonnull Collection<? extends T> c) {
		int currentIndex = index;
		for (T element : c) {
			if(!checkingSet.add(element))
				continue;
			add(currentIndex, element);
			currentIndex++;
		}
		return currentIndex != index;
	}

	@Override
	public boolean removeAll(@Nonnull Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeIf(Predicate<? super T> filter) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(@Nonnull Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void replaceAll(UnaryOperator<T> operator) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void sort(Comparator<? super T> c) {
		destinationList.sort(c);
	}

	@Override
	public void clear() {
		checkingSet.clear();
		destinationList.clear();
	}

	@Override
	public T get(int index) {
		return destinationList.get(index);
	}

	@Override
	public T set(int index, T element) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void add(int index, T element) {
		if(!checkingSet.add(element))
			return;
		destinationList.add(index, element);
	}

	@Override
	public T remove(int index) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int indexOf(Object o) {
		return destinationList.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		return destinationList.lastIndexOf(o);
	}

	@Nonnull
	@Override
	public ListIterator<T> listIterator() {
		return destinationList.listIterator();
	}

	@Nonnull
	@Override
	public ListIterator<T> listIterator(int index) {
		return destinationList.listIterator(index);
	}

	@Nonnull
	@Override
	public List<T> subList(int fromIndex, int toIndex) {
		return destinationList.subList(fromIndex, toIndex);
	}

	@Override
	public Spliterator<T> spliterator() {
		return destinationList.spliterator();
	}

	@Override
	public Stream<T> stream() {
		return destinationList.stream();
	}

	@Override
	public Stream<T> parallelStream() {
		return destinationList.parallelStream();
	}

	@Override
	public void addFirst(T element) {
		add(0, element);
	}

	@Override
	public void addLast(T element) {
		add(element);
	}

	@Override
	public T getFirst() {
		return destinationList.getFirst();
	}

	@Override
	public T getLast() {
		return destinationList.getLast();
	}

	@Override
	public T removeFirst() {
		throw new UnsupportedOperationException();
	}

	@Override
	public T removeLast() {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<T> reversed() {
		return destinationList.reversed();
	}

}
