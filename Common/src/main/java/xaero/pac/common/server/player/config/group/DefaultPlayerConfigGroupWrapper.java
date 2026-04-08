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

package xaero.pac.common.server.player.config.group;

import net.minecraft.server.level.ServerPlayer;
import xaero.pac.common.server.player.config.IPlayerConfig;
import xaero.pac.common.server.player.config.group.custom.ICustomPlayerConfigGroup;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;
import java.util.UUID;

/**
 * Used for storing group lookup cache for a default config player group but in the context of a specific player config.
 * <p>
 * Necessary for cases where an exception option is not defaulted, the selected group is a default config player group
 * which directly or indirectly includes other groups that the specific player config extends with its own groups with
 * the same id(s), but doesn't extend the root selected group in the same way, so the default config's group would be
 * returned by the group manager, leaving out any extensions of the included groups during checks, if not for this
 * wrapper.
 */
public class DefaultPlayerConfigGroupWrapper extends CachedPlayerConfigParentGroup {

	private final IPlayerConfig config;
	private final ICustomPlayerConfigGroup defaultConfigGroup;

	public DefaultPlayerConfigGroupWrapper(IPlayerConfig config, ICustomPlayerConfigGroup defaultConfigGroup) {
		this.config = config;
		this.defaultConfigGroup = defaultConfigGroup;
	}

	@Nonnull
	@Override
	public Set<String> getDirectGroupIds() {
		return defaultConfigGroup.getDirectGroupIds();
	}

	@Override
	public IPlayerConfigGroup lookupGroup(String id) {
		return config.getPlayerGroups().getUnwrapped(id);
	}

	@Override
	public IPlayerConfigGroup lookupDefaultGroup(String id) {
		return defaultConfigGroup.lookupGroup(id);
	}

	@Nonnull
	@Override
	public String getId() {
		return defaultConfigGroup.getId();
	}

	@Override
	public boolean isDirectlyInGroup(IPlayerConfig contextConfig, @Nullable ServerPlayer player, @Nullable UUID playerId) {
		return defaultConfigGroup.isDirectlyInGroup(contextConfig, player, playerId);
	}

}
