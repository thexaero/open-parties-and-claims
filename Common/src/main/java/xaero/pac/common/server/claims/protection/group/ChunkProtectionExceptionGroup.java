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

package xaero.pac.common.server.claims.protection.group;

import com.mojang.datafixers.util.Either;
import net.minecraft.tags.TagKey;
import xaero.pac.common.server.claims.protection.ChunkProtectionExceptionSet;
import xaero.pac.common.server.claims.protection.ChunkProtectionExceptionType;
import xaero.pac.common.server.player.config.api.IPlayerConfigOptionSpecAPI;

import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class ChunkProtectionExceptionGroup<T> {

	public static final Pattern GROUP_NAME_PATTERN = Pattern.compile("[a-zA-Z\\d_\\-]{1,32}");
	private final String name;
	private final ChunkProtectionExceptionType type;
	private final ChunkProtectionExceptionSet<T> exceptionSet;
	private IPlayerConfigOptionSpecAPI<Integer> playerConfigOption;

	private ChunkProtectionExceptionGroup(String name, ChunkProtectionExceptionType type, ChunkProtectionExceptionSet<T> exceptionSet) {
		this.name = name;
		this.type = type;
		this.exceptionSet = exceptionSet;
	}

	public String getName() {
		return name;
	}

	public ChunkProtectionExceptionType getType() {
		return type;
	}

	public boolean contains(T object){
		return exceptionSet.contains(object);
	}

	public void updateTagExceptions(){
		exceptionSet.updateTagExceptions();
	}

	public void setPlayerConfigOption(IPlayerConfigOptionSpecAPI<Integer> playerConfigOption) {
		this.playerConfigOption = playerConfigOption;
	}

	public IPlayerConfigOptionSpecAPI<Integer> getPlayerConfigOption() {
		return playerConfigOption;
	}

	public Stream<Either<T, TagKey<T>>> stream(){
		return exceptionSet.stream();
	}

	public static final class Builder<T> {

		private String name;
		private ChunkProtectionExceptionType type;
		private ChunkProtectionExceptionSet.Builder<T> exceptionSetBuilder;

		private Builder(){
			exceptionSetBuilder = ChunkProtectionExceptionSet.Builder.begin();
		}

		private Builder<T> setDefault() {
			setName(null);
			setType(null);
			exceptionSetBuilder.setDefault();
			return this;
		}

		public Builder<T> setName(String name) {
			this.name = name;
			return this;
		}

		public Builder<T> setType(ChunkProtectionExceptionType type) {
			this.type = type;
			return this;
		}

		public Builder<T> setTagStreamGetter(Function<TagKey<T>, Stream<T>> tagStreamGetter) {
			this.exceptionSetBuilder.setTagStreamGetter(tagStreamGetter);
			return this;
		}

		public Builder<T> addException(Either<T, TagKey<T>> e){
			exceptionSetBuilder.addEither(e);
			return this;
		}

		public ChunkProtectionExceptionGroup<T> build(){
			if(name == null || type == null)
				throw new IllegalStateException();
			if(!GROUP_NAME_PATTERN.matcher(name).matches())
				throw new IllegalArgumentException("Exception group name must consist of A - Z, numbers or the - and _ characters: " + name);
			return new ChunkProtectionExceptionGroup<>(name, type, exceptionSetBuilder.build());
		}

		public static <T> Builder<T> begin(){
			return new Builder<T>().setDefault();
		}

	}

}
