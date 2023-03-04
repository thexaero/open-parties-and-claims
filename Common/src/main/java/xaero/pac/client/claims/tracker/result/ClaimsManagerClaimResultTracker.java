/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2022-2023, Xaero <xaero1996@gmail.com> and contributors
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

package xaero.pac.client.claims.tracker.result;

import xaero.pac.client.claims.tracker.result.api.IClaimsManagerClaimResultListenerAPI;
import xaero.pac.common.claims.result.api.AreaClaimResult;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

public class ClaimsManagerClaimResultTracker implements IClaimsManagerClaimResultTracker {
	
	private Set<IClaimsManagerClaimResultListenerAPI> listeners;

	private ClaimsManagerClaimResultTracker(Set<IClaimsManagerClaimResultListenerAPI> listeners) {
		super();
		this.listeners = listeners;
	}

	@Override
	public void register(@Nonnull IClaimsManagerClaimResultListenerAPI listener) {
		listeners.add(listener);
	}
	
	public void onClaimResult(AreaClaimResult result){
		for(IClaimsManagerClaimResultListenerAPI listener : listeners)
			listener.onClaimResult(result);
	}
	
	public static final class Builder {

		private Builder() {
		}

		private Builder setDefault() {
			return this;
		}

		public ClaimsManagerClaimResultTracker build() {
			return new ClaimsManagerClaimResultTracker(new HashSet<>());
		}

		public static Builder begin() {
			return new Builder().setDefault();
		}

	}

}
