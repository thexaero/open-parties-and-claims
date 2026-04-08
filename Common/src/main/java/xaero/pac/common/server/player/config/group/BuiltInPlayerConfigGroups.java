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

import xaero.pac.common.player.config.PlayerConfigConstants;
import xaero.pac.common.server.player.config.group.custom.ICustomPlayerConfigGroup;
import xaero.pac.common.server.player.config.group.party.AllyPlayerConfigGroup;
import xaero.pac.common.server.player.config.group.party.PartyPlayerConfigGroup;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class BuiltInPlayerConfigGroups {

	private final static Map<String, IPlayerConfigGroup> ALL = new HashMap<>();
	public final static ConstantPlayerConfigGroup NOBODY =
			register(new ConstantPlayerConfigGroup(PlayerConfigConstants.NO_EXCEPTION_ID, false));
	public final static PartyPlayerConfigGroup PARTY =
			register(new PartyPlayerConfigGroup(PlayerConfigConstants.PARTY_EXCEPTION_ID));
	public final static IPlayerConfigGroup ALLIES =
			register(new AllyPlayerConfigGroup(PlayerConfigConstants.ALLIES_EXCEPTION_ID));
	public final static ConstantPlayerConfigGroup EVERYONE =
			register(new ConstantPlayerConfigGroup(PlayerConfigConstants.EVERYONE_EXCEPTION_ID, true));

	private static <T extends IPlayerConfigGroup> T register(T group){
		if(group instanceof ICustomPlayerConfigGroup)
			throw new IllegalArgumentException();
		ALL.put(group.getId(), group);
		return group;
	}

	public static IPlayerConfigGroup get(String id){
		return ALL.get(id);
	}

	public static void forEach(Consumer<IPlayerConfigGroup> consumer){
		ALL.values().forEach(consumer);
	}

}
