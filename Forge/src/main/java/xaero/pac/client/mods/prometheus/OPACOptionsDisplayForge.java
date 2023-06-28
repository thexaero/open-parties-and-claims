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

import com.teamresourceful.resourcefullib.client.components.selection.ListEntry;
import com.teamresourceful.resourcefullib.client.components.selection.SelectionList;
import com.teamresourceful.resourcefullib.common.utils.TriState;
import earth.terrarium.prometheus.api.roles.client.OptionDisplay;
import earth.terrarium.prometheus.client.screens.roles.options.entries.NumberBoxListEntry;
import earth.terrarium.prometheus.client.screens.roles.options.entries.TextListEntry;
import earth.terrarium.prometheus.client.screens.roles.options.entries.TriStateListEntry;
import earth.terrarium.prometheus.common.handlers.role.Role;
import net.minecraft.network.chat.Component;
import xaero.pac.common.mods.prometheus.OPACOptionsForge;

import java.util.List;
import java.util.function.Consumer;

public class OPACOptionsDisplayForge extends OPACOptionsDisplay<OPACOptionsForge, ListEntry, TextListEntry, TriStateListEntry, BetterNumberBoxListEntryForge, TriState> implements OptionDisplay {

	public static OPACOptionsDisplayForge create(Role role, SelectionList<ListEntry> ignored) {
		OPACOptionsDisplayForge instance = new OPACOptionsDisplayForge();
		OPACOptionsForge options = role.getNonNullOption(OPACOptionsForge.SERIALIZER);
		instance.onCreate(options);
		return instance;
	}

	@Override
	public List<ListEntry> getDisplayEntries() {
		return entries;
	}

	@Override
	public boolean save(Role role) {
		OPACOptionsForge options = new OPACOptionsForge();
		mappedEntries.forEach((node, entry) -> {
			if(entry instanceof NumberBoxListEntry numberBoxListEntry && numberBoxListEntry.getIntValue().isPresent()) {
				options.setValue(node, numberBoxListEntry.getIntValue().getAsInt());
				return;
			}
			if(entry instanceof TriStateListEntry triStateListEntry && triStateListEntry.state() != TriState.UNDEFINED) {
				options.setValue(node, triStateListEntry.state() == TriState.TRUE);
			}
		});
		role.setData(options);
		return true;
	}

	@Override
	protected TriState undefinedTriState() {
		return TriState.UNDEFINED;
	}

	@Override
	protected TriState triStateOf(boolean b) {
		return TriState.of(b);
	}

	@Override
	protected TextListEntry textListEntry(Component component) {
		return new TextListEntry(component);
	}

	@Override
	protected BetterNumberBoxListEntryForge betterNumberBoxListEntry(int amount, boolean decimals, Component component, Component tooltip) {
		return new BetterNumberBoxListEntryForge(amount, decimals, component, tooltip);
	}

	@Override
	protected TriStateListEntry triStateListEntry(Component component, TriState state, Consumer<TriStateListEntry> onPress) {
		return new TriStateListEntry(component, state, onPress);
	}

}
