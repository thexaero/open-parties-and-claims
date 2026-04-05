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

package xaero.pac.common.player.config.group.custom;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import xaero.pac.common.player.config.PlayerConfigConstants;
import xaero.pac.common.util.linked.ILinkedChainNode;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.UUID;

public class CustomPlayerGroupMember implements ICustomPlayerGroupMember, ILinkedChainNode<CustomPlayerGroupMember> {

	private UUID id;
	private String displayName;
	private CustomPlayerGroupMember next;
	private CustomPlayerGroupMember previous;
	private boolean destroyed;
	private CustomPlayerConfigGroupData groupData;

	public CustomPlayerGroupMember(@Nullable UUID id, @Nullable String displayName) {
		this.id = id;
		this.displayName = displayName;
	}

	public void setGroupData(CustomPlayerConfigGroupData groupData) {
		if(this.groupData != null)
			throw new IllegalStateException("Can't reuse CustomPlayerGroupMember objects!");
		this.groupData = groupData;
	}

	@Override
	@Nullable
	public UUID getId() {
		return id;
	}

	@Override
	@Nullable
	public String getDisplayName() {
		return displayName;
	}

	protected void setId(UUID id) {
		if(this.id != null)
			throw new IllegalStateException();
		this.id = id;
	}

	protected void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) return false;
		CustomPlayerGroupMember that = (CustomPlayerGroupMember) o;
		return Objects.equals(id, that.id) && Objects.equals(displayName, that.displayName);
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder("IncludedPlayer(");
		if(displayName != null)
			result.append(displayName);
		result.append("|");
		if(id != null)
			result.append(id);
		result.append(")");
		return result.toString();
	}

	@Nonnull
	@Override
	public Component getDisplayLabel() {
		MutableComponent result = Component.literal("");
		result.append(displayName == null ? PlayerConfigConstants.UNKNOWN_PLAYER : Component.literal(displayName));
		if(id != null)
			result.withStyle(s ->
					s.withHoverEvent(
							new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(id.toString()))
					)
			);
		return result;
	}

	@Override
	public void setNext(CustomPlayerGroupMember element) {
		this.next = element;
	}

	@Override
	public void setPrevious(CustomPlayerGroupMember element) {
		this.previous = element;
	}

	@Override
	public CustomPlayerGroupMember getNext() {
		return next;
	}

	@Override
	public CustomPlayerGroupMember getPrevious() {
		return previous;
	}

	@Override
	public boolean isDestroyed() {
		return destroyed;
	}

	@Override
	public void onDestroyed() {
		destroyed = true;
	}
}
