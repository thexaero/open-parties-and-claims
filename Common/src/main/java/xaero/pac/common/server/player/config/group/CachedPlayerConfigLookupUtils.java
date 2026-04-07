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

import java.util.HashSet;
import java.util.Set;

public class CachedPlayerConfigLookupUtils {

	protected static Set<IPlayerConfigGroup> createGroupLookupCache(IPlayerConfigParentGroup group){
		HashSet<IPlayerConfigGroup> result = new HashSet<>();
		traceGroups(result, group, group);
		if(group instanceof DefaultPlayerConfigGroupWrapper)
			return result;
		IPlayerConfigGroup defaultGroupSameId = group.lookupDefaultGroup(group.getId());
		if(defaultGroupSameId != group && defaultGroupSameId instanceof IPlayerConfigParentGroup defaultParentGroup)
			traceGroups(result, defaultParentGroup, group);
		return result;
	}

	private static void traceGroups(
			HashSet<IPlayerConfigGroup> destination,
			IPlayerConfigParentGroup group,
			IPlayerConfigParentGroup rootGroup
	){
		for (String directGroupId : group.getDirectGroupIds()) {
			IPlayerConfigGroup lookedUpGroup = rootGroup.lookupGroup(directGroupId);
			handleLookedUpGroup(destination, lookedUpGroup, rootGroup);
			IPlayerConfigGroup lookedUpDefaultGroup = rootGroup.lookupDefaultGroup(directGroupId);
			if(lookedUpDefaultGroup == lookedUpGroup)
				continue;
			//looking up both the personal groups and the ones from the default player config
			//allows players to add stuff on top of the default config's groups
			handleLookedUpGroup(destination, lookedUpDefaultGroup, rootGroup);
		}
	}

	private static void handleLookedUpGroup(
			HashSet<IPlayerConfigGroup> destination,
			IPlayerConfigGroup lookedUpGroup,
			IPlayerConfigParentGroup rootGroup
	){
		if(lookedUpGroup == null)
			return;
		if(!destination.add(lookedUpGroup))
			return;
		if(!(lookedUpGroup instanceof IPlayerConfigParentGroup lookedUpParentGroup))
			return;
		traceGroups(destination, lookedUpParentGroup, rootGroup);
	}

}
