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

package xaero.pac.client.player.config;

import xaero.pac.client.player.config.api.IPlayerConfigClientStorageAPI;
import xaero.pac.common.server.player.config.api.PlayerConfigType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public interface IPlayerConfigClientStorage<OS extends IPlayerConfigStringableOptionClientStorage<?>> extends IPlayerConfigClientStorageAPI<OS> {

	//internal api

	public void reset();

	public boolean isSyncInProgress();

	public void setSyncInProgress(boolean syncInProgress);

	@Nonnull
	@Override
	public List<String> getSubConfigIds();

	@Nullable
	@Override
	IPlayerConfigClientStorage<OS> getSubConfig(@Nonnull String id);

	@Nonnull
	@Override
	IPlayerConfigClientStorage<OS> getEffectiveSubConfig(@Nonnull String id);

	@Nonnull
	@Override
	public Stream<IPlayerConfigClientStorageAPI<OS>> getSubConfigAPIStream();

	@Override
	boolean isBeingDeleted();

	public Stream<IPlayerConfigClientStorage<OS>> getSubConfigStream();

	public void setSelectedSubConfig(String selectedSubConfig);

	public String getSelectedSubConfig();

	public IPlayerConfigClientStorage<OS> getOrCreateSubConfig(String subId);

	public void removeSubConfig(String subId);

	public void setGeneralState(boolean beingDeleted, int subConfigLimit);

	public static interface IBuilder<CS extends IPlayerConfigClientStorage<?>> {
		
		public IBuilder<CS> setDefault();
		
		public IBuilder<CS> setType(PlayerConfigType type);
		
		public IBuilder<CS> setOwner(UUID owner);
		
		public CS build();
		
	}
	
}
