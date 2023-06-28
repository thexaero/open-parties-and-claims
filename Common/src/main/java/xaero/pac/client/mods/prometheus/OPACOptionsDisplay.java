/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2023, Xaero <xaero1996@gmail.com> and contributors
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

package xaero.pac.client.mods.prometheus;

import net.minecraft.network.chat.Component;
import xaero.pac.common.mods.prometheus.OPACOptions;
import xaero.pac.common.server.player.permission.api.IPermissionNodeAPI;
import xaero.pac.common.server.player.permission.api.UsedPermissionNodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public abstract class OPACOptionsDisplay<O extends OPACOptions, LE, TLE extends LE, TSLE extends LE, NBLE extends LE, TS> {

	protected final List<LE> entries;
	protected final Map<IPermissionNodeAPI, LE> mappedEntries;

	private static final Component TITLE = Component.translatable("option.openpartiesandclaims.permissions/v1");

	public OPACOptionsDisplay() {
		this.entries = new ArrayList<>();
		this.mappedEntries = new HashMap<>();
	}

	protected void onCreate(O options){
		entries.add(textListEntry(TITLE));
		UsedPermissionNodes.ALL.forEach((name, node) -> {
			LE entry;
			if(node.isInt()) {
				Integer currentValue = options.getValueCast(node);
				entry = betterNumberBoxListEntry(currentValue == null ? -1 : currentValue, false, node.getName(), node.getComment());
				if(currentValue == null)
					((IBetterNumberBoxListEntry)entry).setText("");
			} else {
				TS state = undefinedTriState();
				Boolean value = options.getValueCast(node);
				if(value != null)
					state = triStateOf(value);
				entry = triStateListEntry(node.getName(), state, e -> {});
			}
			entries.add(entry);
			mappedEntries.put(node, entry);
		});
	}

	protected abstract TS undefinedTriState();
	protected abstract TS triStateOf(boolean b);
	protected abstract TLE textListEntry(Component component);
	protected abstract NBLE betterNumberBoxListEntry(int amount, boolean decimals, Component component, Component tooltip);
	protected abstract TSLE triStateListEntry(Component component, TS state, Consumer<TSLE> onPress);

}
