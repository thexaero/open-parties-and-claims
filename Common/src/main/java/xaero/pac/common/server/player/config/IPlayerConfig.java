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

package xaero.pac.common.server.player.config;

import xaero.pac.common.claims.player.mode.api.IClaimingModeAPI;
import xaero.pac.common.server.player.config.api.v2.IPlayerConfigAPI;
import xaero.pac.common.server.player.config.api.v2.IPlayerConfigOptionSpecAPI;
import xaero.pac.common.server.player.config.group.IServerPlayerConfigGroupManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public interface IPlayerConfig extends IPlayerConfigAPI {
	//internal API

	public <T> T applyDefaultReplacer(IPlayerConfigOptionSpecAPI<T> o, T value);

	@Nullable
	@Override
	public IPlayerConfig getSubConfig(@Nonnull String id);

	@Nonnull
	@Override
	public IPlayerConfig getEffectiveSubConfig(@Nonnull String id);

	@Override
	@Nonnull
	public IPlayerConfig getEffectiveSubConfig(int subIndex);

	@Override
	public boolean subConfigExists(@Nonnull String id);

	@Override
	public boolean subConfigExists(int subIndex);

	@Override
	@Nonnull
	public IPlayerConfig getUsedSubConfig();

	@Deprecated
	@Nonnull
	@Override
	IPlayerConfig getUsedServerSubConfig();

	@Nonnull
	public IPlayerConfig getUsedSubConfig(@Nonnull IClaimingModeAPI claimingMode);

	@Nullable
	@Override
	public IPlayerConfig createSubConfig(@Nonnull String id);

	@Nullable
	public IPlayerConfig createSubConfig(@Nonnull String id, boolean initStorage);

	@Nullable
	@Override
	public String getSubId();

	@Override
	public int getSubIndex();

	@Override
	public int getSubCount();

	@Nonnull
	@Override
	public List<String> getSubConfigIds();

	@Nonnull
	@Override
	public Stream<IPlayerConfigAPI> getSubConfigAPIStream();

	@Nullable
	@Override
	public <T> T getDefaultRawValue(@Nonnull IPlayerConfigOptionSpecAPI<T> option);

	@Override
	public boolean isOptionAllowed(@Nonnull IPlayerConfigOptionSpecAPI<?> option);

	@Override
	public boolean isBeingDeleted();

	public void setBeingDeleted();

	public IPlayerConfig removeSubConfig(String id);

	public IPlayerConfig removeSubConfig(int index);

	public Iterator<IPlayerConfig> getSubConfigIterator();

	@Override
	IServerPlayerConfigGroupManager getPlayerGroups();

	IPlayerConfigManager getManager();

	boolean isOptionDefaulted(IPlayerConfigOptionSpecAPI<?> option);

	<T> void resetAutomaticDefaultValue(@Nonnull IPlayerConfigOptionSpecAPI<T> o);

	IPlayerConfig getMain();

}
