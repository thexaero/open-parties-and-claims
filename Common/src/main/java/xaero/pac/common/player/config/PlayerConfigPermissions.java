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

package xaero.pac.common.player.config;

public class PlayerConfigPermissions implements IPlayerConfigPermissions {

	protected boolean view;
	protected boolean edit;
	protected boolean includePlayersInGroups;
	protected boolean includeGroupsInGroups;
	protected boolean createGroups;
	protected boolean claimAs;

	@Override
	public void setView(boolean view) {
		this.view = view;
	}

	@Override
	public void setEdit(boolean edit) {
		this.edit = edit;
	}

	@Override
	public void setIncludeGroupsInGroups(boolean includeGroupsInGroups) {
		this.includeGroupsInGroups = includeGroupsInGroups;
	}

	@Override
	public void setIncludePlayersInGroups(boolean includePlayersInGroups) {
		this.includePlayersInGroups = includePlayersInGroups;
	}

	@Override
	public void setCreateGroups(boolean createGroups) {
		this.createGroups = createGroups;
	}

	@Override
	public void setClaimAs(boolean claimAs) {
		this.claimAs = claimAs;
	}

	@Override
	public boolean canView() {
		return view;
	}

	@Override
	public boolean canEdit() {
		return edit;
	}

	@Override
	public boolean canIncludeGroupsInGroups() {
		return includeGroupsInGroups;
	}

	@Override
	public boolean canIncludePlayersInGroups() {
		return includePlayersInGroups;
	}

	@Override
	public boolean canCreateGroups() {
		return createGroups;
	}

	@Override
	public boolean canClaimAs() {
		return claimAs;
	}

	public void reset() {
		view = false;
		edit = false;
		includePlayersInGroups = false;
		includeGroupsInGroups = false;
		createGroups = false;
		claimAs = false;
	}

}
