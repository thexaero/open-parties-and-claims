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

package xaero.pac.common.claims.player.request;

import xaero.pac.common.claims.ClaimsManager;
import xaero.pac.common.claims.player.mode.ClaimingMode;

public class ClaimActionRequest {
	
	private final ClaimsManager.Action action;
	private final int left;
	private final int top;
	private final int right;
	private final int bottom;
	private final ClaimingMode mode;
	private final int totalChunks;
	
	public ClaimActionRequest(ClaimsManager.Action action, int left, int top, int right, int bottom, ClaimingMode mode) {
		super();
		this.action = action;
		this.left = left;
		this.top = top;
		this.right = right;
		this.bottom = bottom;
		this.mode = mode;
		this.totalChunks = (1 + right - left) * (1 + top - bottom);
	}

	public ClaimsManager.Action getAction() {
		return action;
	}

	public int getLeft() {
		return left;
	}

	public int getTop() {
		return top;
	}

	public int getRight() {
		return right;
	}

	public int getBottom() {
		return bottom;
	}

	public ClaimingMode getMode() {
		return mode;
	}

	public int getTotalChunks() {
		return totalChunks;
	}

}
