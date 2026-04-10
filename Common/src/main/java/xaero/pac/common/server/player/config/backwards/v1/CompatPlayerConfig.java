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

package xaero.pac.common.server.player.config.backwards.v1;

import xaero.pac.common.server.player.config.api.v1.IPlayerConfigAPI;
import xaero.pac.common.server.player.config.api.v1.IPlayerConfigOptionSpecAPI;
import xaero.pac.common.server.player.config.api.PlayerConfigType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Deprecated
public class CompatPlayerConfig implements IPlayerConfigAPI {

	public final xaero.pac.common.server.player.config.api.v2.IPlayerConfigAPI realConfig;

	public CompatPlayerConfig(xaero.pac.common.server.player.config.api.v2.IPlayerConfigAPI realConfig) {
		this.realConfig = realConfig;
	}

	@Override
	public UUID getPlayerId() {
		return realConfig.getPlayerId();
	}

	@Override
	public <T extends Comparable<T>> SetResult tryToSet(@Nonnull IPlayerConfigOptionSpecAPI<T> option, @Nullable T value) {
		CompatPlayerConfigOptionSpec<T, ?> compatOption = (CompatPlayerConfigOptionSpec<T, ?>) option;
		return tryToSet(compatOption, value);
	}

	private <T extends Comparable<T>, R> SetResult tryToSet(CompatPlayerConfigOptionSpec<T, R> compatOption, @Nullable T value) {
		xaero.pac.common.server.player.config.api.v2.IPlayerConfigOptionSpecAPI<R> realOption = compatOption.realOption;
		R current = realConfig.getEffective(realOption);
		R converted = value == null ? null : compatOption.toRealConverter.apply(value, current);
		return SetResult.fromReal(realConfig.tryToSet(realOption, converted));
	}

	@Override
	public <T extends Comparable<T>> T getEffective(@Nonnull IPlayerConfigOptionSpecAPI<T> option) {
		CompatPlayerConfigOptionSpec<T, ?> compatOption = (CompatPlayerConfigOptionSpec<T, ?>) option;
		return getEffective(compatOption);
	}

	private <T extends Comparable<T>, R> T getEffective(CompatPlayerConfigOptionSpec<T, R> compatOption) {
		xaero.pac.common.server.player.config.api.v2.IPlayerConfigOptionSpecAPI<R> realOption = compatOption.realOption;
		R current = realConfig.getEffective(realOption);
		return compatOption.fromRealConverter.apply(current);
	}

	@Override
	public <T extends Comparable<T>> T getFromEffectiveConfig(@Nonnull IPlayerConfigOptionSpecAPI<T> option) {
		CompatPlayerConfigOptionSpec<T, ?> compatOption = (CompatPlayerConfigOptionSpec<T, ?>) option;
		return getFromEffectiveConfig(compatOption);
	}

	public <T extends Comparable<T>, R> T getFromEffectiveConfig(CompatPlayerConfigOptionSpec<T, R> compatOption) {
		xaero.pac.common.server.player.config.api.v2.IPlayerConfigOptionSpecAPI<R> realOption = compatOption.realOption;
		R current = realConfig.getFromEffectiveConfig(realOption);
		return compatOption.fromRealConverter.apply(current);
	}

	@Override
	public <T extends Comparable<T>> T getRaw(@Nonnull IPlayerConfigOptionSpecAPI<T> o) {
		CompatPlayerConfigOptionSpec<T, ?> compatOption = (CompatPlayerConfigOptionSpec<T, ?>) o;
		return getRaw(compatOption);
	}

	private <T extends Comparable<T>, R> T getRaw(CompatPlayerConfigOptionSpec<T, R> compatOption) {
		xaero.pac.common.server.player.config.api.v2.IPlayerConfigOptionSpecAPI<R> realOption = compatOption.realOption;
		R current = realConfig.getRaw(realOption);
		return current == null ? null : compatOption.fromRealConverter.apply(current);
	}

	@Override
	public <T extends Comparable<T>> SetResult tryToReset(@Nonnull IPlayerConfigOptionSpecAPI<T> option) {
		CompatPlayerConfigOptionSpec<T, ?> compatOption = (CompatPlayerConfigOptionSpec<T, ?>) option;
		return tryToReset(compatOption);
	}

	private <T extends Comparable<T>, R> SetResult tryToReset(CompatPlayerConfigOptionSpec<T, R> compatOption) {
		xaero.pac.common.server.player.config.api.v2.IPlayerConfigOptionSpecAPI<R> realOption = compatOption.realOption;
		return SetResult.fromReal(realConfig.tryToReset(realOption));
	}

	@Override
	public PlayerConfigType getType() {
		return realConfig.getType();
	}

	@Override
	public IPlayerConfigAPI getSubConfig(@Nonnull String id) {
		xaero.pac.common.server.player.config.api.v2.IPlayerConfigAPI realSub = realConfig.getSubConfig(id);
		return realSub == null ? null : new CompatPlayerConfig(realSub);
	}

	@Override
	public IPlayerConfigAPI getEffectiveSubConfig(@Nonnull String id) {
		xaero.pac.common.server.player.config.api.v2.IPlayerConfigAPI realSub = realConfig.getEffectiveSubConfig(id);
		return new CompatPlayerConfig(realSub);
	}

	@Override
	public IPlayerConfigAPI getEffectiveSubConfig(int subIndex) {
		xaero.pac.common.server.player.config.api.v2.IPlayerConfigAPI realSub = realConfig.getEffectiveSubConfig(subIndex);
		return new CompatPlayerConfig(realSub);
	}

	@Override
	public boolean subConfigExists(@Nonnull String id) {
		return realConfig.subConfigExists(id);
	}

	@Override
	public boolean subConfigExists(int subIndex) {
		return realConfig.subConfigExists(subIndex);
	}

	@Override
	public IPlayerConfigAPI getUsedSubConfig() {
		xaero.pac.common.server.player.config.api.v2.IPlayerConfigAPI realSub = realConfig.getUsedSubConfig();
		return new CompatPlayerConfig(realSub);
	}

	@Override
	public IPlayerConfigAPI getUsedServerSubConfig() {
		xaero.pac.common.server.player.config.api.v2.IPlayerConfigAPI realSub = realConfig.getUsedServerSubConfig();
		return new CompatPlayerConfig(realSub);
	}

	@Override
	public IPlayerConfigAPI createSubConfig(@Nonnull String id) {
		xaero.pac.common.server.player.config.api.v2.IPlayerConfigAPI realSub = realConfig.createSubConfig(id);
		return realSub == null ? null : new CompatPlayerConfig(realSub);
	}

	@Override
	public String getSubId() {
		return realConfig.getSubId();
	}

	@Override
	public int getSubIndex() {
		return realConfig.getSubIndex();
	}

	@Override
	public int getSubCount() {
		return realConfig.getSubCount();
	}

	@Override
	public List<String> getSubConfigIds() {
		return realConfig.getSubConfigIds();
	}

	@Override
	public Stream<IPlayerConfigAPI> getSubConfigAPIStream() {
		return realConfig.getSubConfigAPIStream().map(c -> new CompatPlayerConfig(c));
	}

	@Override
	public <T extends Comparable<T>> T getDefaultRawValue(@Nonnull IPlayerConfigOptionSpecAPI<T> option) {
		CompatPlayerConfigOptionSpec<T, ?> compatOption = (CompatPlayerConfigOptionSpec<T, ?>) option;
		return getDefaultRawValue(compatOption);
	}

	private <T extends Comparable<T>, R> T getDefaultRawValue(CompatPlayerConfigOptionSpec<T, R> compatOption) {
		R realValue = realConfig.getDefaultRawValue(compatOption.realOption);
		return realValue == null ? null : compatOption.fromRealConverter.apply(realValue);
	}

	@Override
	public boolean isOptionAllowed(@Nonnull IPlayerConfigOptionSpecAPI<?> option) {
		CompatPlayerConfigOptionSpec<?, ?> compatOption = (CompatPlayerConfigOptionSpec<?, ?>) option;
		return realConfig.isOptionAllowed(compatOption.realOption);
	}

	@Override
	public boolean isBeingDeleted() {
		return realConfig.isBeingDeleted();
	}

	@Override
	public int getSubConfigLimit() {
		return realConfig.getSubConfigLimit();
	}
}
