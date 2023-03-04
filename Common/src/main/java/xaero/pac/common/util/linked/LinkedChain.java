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

package xaero.pac.common.util.linked;

import com.google.common.collect.Streams;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.stream.Stream;

/**
 * Iterating over this chain over multiple ticks, from multiple places, won't throw concurrent modification
 * errors unlike LinkedHashMaps.
 * For element removal to work while preserving memory, a value instance can only be in 1 chain ever.
 *
 * @param <V>  the element type
 */
public class LinkedChain<V extends ILinkedChainNode<V>> implements Iterable<V> {

	private boolean destroyed;
	private V head;

	public void add(V element) {
		if(destroyed)
			throw new RuntimeException(new IllegalAccessException("Trying to use a destroyed chain!"));
		if(element.isDestroyed())
			throw new IllegalArgumentException("Trying to reintroduce a removed chain element!");
		if(head != null) {
			element.setNext(head);
			head.setPrevious(element);
		}
		head = element;
	}

	public void remove(V element) {
		if(destroyed)
			throw new RuntimeException(new IllegalAccessException("Trying to use a cleared chain!"));
		if(element.isDestroyed())
			return;
		V prev = element.getPrevious();
		V next = element.getNext();
		if(prev != null)
			prev.setNext(next);
		if(next != null)
			next.setPrevious(prev);
		if(element == head)
			head = next;
		element.onDestroyed();
	}

	public void destroy(){
		head = null;
		destroyed = true;
	}

	@Nonnull
	@Override
	public Iterator<V> iterator() {
		return new Iterator<>() {
			private V next = head;

			private V reachValidNext(){
				if(destroyed) {
					next = null;
					return null;
				}
				while(next != null && next.isDestroyed())
					next = next.getNext();
				return next;
			}

			@Override
			public boolean hasNext() {
				return reachValidNext() != null;
			}

			@Nullable
			@Override
			public V next() {
				V result = reachValidNext();
				if(result != null)
					next = result.getNext();
				return result;
			}
		};
	}

	@Nonnull
	public Stream<V> stream(){
		return Streams.stream(this);
	}

}
