/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2022, Xaero <xaero1996@gmail.com> and contributors
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

package xaero.pac.common.server.claims.forceload.player;

import xaero.pac.common.server.claims.forceload.ClaimTicket;

import java.util.HashMap;
import java.util.Map;

public final class PlayerForceloadTicketManager {

	private final Map<ClaimTicket, ClaimTicket> tickets;
	private boolean failedToEnableSome;//because of the forceload limit

	private PlayerForceloadTicketManager(Map<ClaimTicket, ClaimTicket> tickets) {
		this.tickets = tickets;
	}

	public void add(ClaimTicket ticket){
		tickets.put(ticket, ticket);
	}

	public ClaimTicket remove(ClaimTicket ticket){
		return tickets.remove(ticket);
	}

	public Iterable<ClaimTicket> values(){
		return tickets.values();
	}

	public int getCount(){
		return tickets.size();
	}

	public boolean failedToEnableSome() {
		return failedToEnableSome;
	}

	public void setFailedToEnableSome(boolean failedToEnableSome) {
		this.failedToEnableSome = failedToEnableSome;
	}

	public static final class Builder {

		private Builder(){}

		public Builder setDefault(){
			return this;
		}

		public PlayerForceloadTicketManager build(){
			return new PlayerForceloadTicketManager(new HashMap<>());
		}

		public static Builder begin(){
			return new Builder().setDefault();
		}

	}

}
