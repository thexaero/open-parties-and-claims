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
import earth.terrarium.prometheus.client.screens.roles.options.entries.TextBoxListEntry;
import earth.terrarium.prometheus.client.screens.roles.options.entries.TextListEntry;
import earth.terrarium.prometheus.client.screens.roles.options.entries.TriStateListEntry;
import earth.terrarium.prometheus.common.handlers.role.Role;
import net.minecraft.network.chat.Component;
import xaero.pac.common.mods.prometheus.OPACOptions;
import xaero.pac.common.server.player.permission.api.IPermissionNodeAPI;
import xaero.pac.common.server.player.permission.api.UsedPermissionNodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OPACOptionsDisplay implements OptionDisplay {

	protected final List<ListEntry> entries;
	protected final Map<IPermissionNodeAPI<?>, ListEntry> mappedEntries;

	private static final Component TITLE = Component.translatable("option.openpartiesandclaims.permissions/v1");

	public OPACOptionsDisplay(List<ListEntry> entries, Map<IPermissionNodeAPI<?>, ListEntry> mappedEntries) {
		this.entries = entries;
		this.mappedEntries = mappedEntries;
	}

	public static OPACOptionsDisplay create(Role role, SelectionList<ListEntry> ignored) {
		List<ListEntry> entries = new ArrayList<>();
		Map<IPermissionNodeAPI<?>, ListEntry> mappedEntries = new HashMap<>();
		OPACOptions options = role.getNonNullOption(OPACOptions.SERIALIZER);
		entries.add(new TextListEntry(TITLE));
		UsedPermissionNodes.ALL.forEach((name, node) -> {
			ListEntry entry;
			if(Number.class.isAssignableFrom(node.getType())) {
				Number currentValue = options.getValueCast(node);
				boolean withPartial = currentValue instanceof Float || currentValue instanceof Double;
				entry = new BetterNumberBoxListEntry(currentValue == null ? -1 : currentValue.intValue(), withPartial, node.getName(), node.getComment());
				if(currentValue == null)
					((BetterNumberBoxListEntry)entry).setText("");
				else if(withPartial)
					((BetterNumberBoxListEntry)entry).setText(currentValue.toString());
			} else if(node.getType() == String.class){
				String currentValue = options.getValueCast(node);
				entry = new TextBoxListEntry(currentValue, 200, node.getName(), node.getComment(), s -> true);
			} else if(node.getType() == Component.class){
				Component currentValue = options.getValueCast(node);
				String text = Component.Serializer.toJson(currentValue);
				entry = new TextBoxListEntry(text, 5000, node.getName(), node.getComment(), s -> true);
			} else {
				TriState state = TriState.UNDEFINED;
				Boolean value = options.getValueCast(node);
				if(value != null)
					state = TriState.of(value);
				entry = new TriStateListEntry(node.getName(), state, e -> {});
			}
			entries.add(entry);
			mappedEntries.put(node, entry);
		});
		return new OPACOptionsDisplay(entries, mappedEntries);
	}

	@Override
	public List<ListEntry> getDisplayEntries() {
		return entries;
	}

	@Override
	public boolean save(Role role) {
		OPACOptions options = new OPACOptions();
		mappedEntries.forEach((node, entry) -> {
			if(entry instanceof NumberBoxListEntry numberBoxListEntry) {
				if(node.getType() == Float.class || node.getType() == Double.class) {
					numberBoxListEntry.getDoubleValue().ifPresent(d -> {
						Number castValue;
						if(node.getType() == Float.class)
							castValue = (float) d;
						else
							castValue = d;
						options.setValueCast(node, castValue);
					});
					return;
				}
				numberBoxListEntry.getIntValue().ifPresent(i -> {
					Number castValue;
					if(node.getType() == Byte.class)
						castValue = (byte)i;
					else if(node.getType() == Short.class)
						castValue = (short)i;
					else if(node.getType() == Long.class)
						castValue = (long)i;
					else
						castValue = i;
					options.setValueCast(node, castValue);
				});
				return;
			}
			if((node.getType() == String.class || node.getType() == Component.class) && entry instanceof TextBoxListEntry textBoxListEntry){
				Object value = textBoxListEntry.getText();
				if(node.getType() == Component.class)
					value = Component.Serializer.fromJson(textBoxListEntry.getText());
				options.setValueCast(node, value);
				return;
			}
			if(node.getType() == Boolean.class && entry instanceof TriStateListEntry triStateListEntry && triStateListEntry.state() != TriState.UNDEFINED) {
				options.setValueCast(node, triStateListEntry.state() == TriState.TRUE);
			}
		});
		role.setData(options);
		return true;
	}

}
