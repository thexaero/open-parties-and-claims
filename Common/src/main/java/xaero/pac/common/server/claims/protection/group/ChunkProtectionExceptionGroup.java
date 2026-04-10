/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2022-2026, Xaero <xaero1996@gmail.com> and contributors
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
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.TagKey;
import xaero.pac.common.server.claims.protection.ChunkProtectionExceptionSet;
import xaero.pac.common.server.claims.protection.ChunkProtectionExceptionType;
import xaero.pac.common.server.claims.protection.ExceptionElementType;
import xaero.pac.common.server.player.config.PlayerConfigOptionCategory;
import xaero.pac.common.server.player.config.api.v2.IPlayerConfigOptionSpecAPI;

import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class ChunkProtectionExceptionGroup<T> {

	public static final Pattern GROUP_NAME_PATTERN = Pattern.compile("[a-zA-Z\\d_\\-]{1,32}");
	private final String name;
	private final ChunkProtectionExceptionType type;
	private final ChunkProtectionExceptionSet<T> exceptionSet;
	private IPlayerConfigOptionSpecAPI<String> playerConfigOption;
	private final String contentString;
	private final PlayerConfigOptionCategory optionCategory;
	private final boolean ofSubjects;//(usually true) the group contains objects that are being interacted with as opposed to actors that interact
	private final Class<?> subjectType;//doesn't always match element type

	private ChunkProtectionExceptionGroup(String name, ChunkProtectionExceptionType type, ChunkProtectionExceptionSet<T> exceptionSet, String contentString, PlayerConfigOptionCategory optionCategory, boolean ofSubjects, Class<?> subjectType) {
		this.name = name;
		this.type = type;
		this.exceptionSet = exceptionSet;
		this.contentString = contentString;
		this.optionCategory = optionCategory;
		this.ofSubjects = ofSubjects;
		this.subjectType = subjectType;
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

	public void updateTagExceptions(MinecraftServer server){
		exceptionSet.updateTagExceptions(server);
	}

	public void setPlayerConfigOption(IPlayerConfigOptionSpecAPI<String> playerConfigOption) {
		this.playerConfigOption = playerConfigOption;
	}

	public IPlayerConfigOptionSpecAPI<String> getPlayerConfigOption() {
		return playerConfigOption;
	}

	public Stream<Either<T, TagKey<T>>> stream(){
		return exceptionSet.stream();
	}

	public String getContentString() {
		return contentString;
	}

	public PlayerConfigOptionCategory getOptionCategory() {
		return optionCategory;
	}

	public boolean isOfSubjects() {
		return ofSubjects;
	}

	public ExceptionElementType<T> getElementType(){
		return exceptionSet.getElementType();
	}

	public Class<?> getSubjectType() {
		return subjectType;
	}

	public static final class Builder<T> {

		private String name;
		private ChunkProtectionExceptionType type;
		private ChunkProtectionExceptionSet.Builder<T> exceptionSetBuilder;
		private String contentString;
		private PlayerConfigOptionCategory optionCategory;
		private boolean ofSubjects;
		private ExceptionElementType<T> elementType;
		private Class<?> subjectType;

		private Builder(ExceptionElementType<T> elementType){
			this.elementType = elementType;
			exceptionSetBuilder = ChunkProtectionExceptionSet.Builder.begin(elementType);
		}

		private Builder<T> setDefault() {
			setOfSubjects(true);
			setSubjectType(elementType.getType());
			setName(null);
			setType(null);
			exceptionSetBuilder.setDefault();
			return this;
		}

		public Builder<T> setSubjectType(Class<?> subjectType) {
			this.subjectType = subjectType;
			return this;
		}

		public Builder<T> setOfSubjects(boolean ofSubjects) {
			this.ofSubjects = ofSubjects;
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

		public Builder<T> setContentString(String contentString) {
			this.contentString = contentString;
			return this;
		}

		public Builder<T> addException(Either<T, TagKey<T>> e){
			exceptionSetBuilder.addEither(e);
			return this;
		}

		public Builder<T> setOptionCategory(PlayerConfigOptionCategory optionCategory) {
			this.optionCategory = optionCategory;
			return this;
		}

		public ChunkProtectionExceptionGroup<T> build(){
			if(name == null || type == null || contentString == null || optionCategory == null)
				throw new IllegalStateException();
			if(!GROUP_NAME_PATTERN.matcher(name).matches())
				throw new IllegalArgumentException("Exception group name must consist of A - Z, numbers or the - and _ characters: " + name);
			return new ChunkProtectionExceptionGroup<>(name, type, exceptionSetBuilder.build(), contentString, optionCategory, ofSubjects, subjectType);
		}

		public static <T> Builder<T> begin(ExceptionElementType<T> elementType){
			return new Builder<T>(elementType).setDefault();
		}

	}

}
