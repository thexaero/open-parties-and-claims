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

package xaero.pac.client.claims.player.mode;

import xaero.pac.client.claims.IClientClaimsManager;
import xaero.pac.client.player.config.IPlayerConfigClientStorageManager;
import xaero.pac.common.claims.player.mode.ClaimingMode;
import xaero.pac.common.claims.player.mode.api.IClaimingModeAPI;

import java.util.Map;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.function.Function;

public final class ClientClaimingModeHandler {

	private final ClaimingMode mode;
	private final Function<IClientClaimsManager<?, ?, ?>, UUID> claimReflectionOwnerGetter;
	private final BiPredicate<IClientClaimsManager<?, ?, ?>, IPlayerConfigClientStorageManager<?>> visibilityGetter;

	private ClientClaimingModeHandler(
			ClaimingMode mode,
			Function<IClientClaimsManager<?, ?, ?>, UUID> claimReflectionOwnerGetter,
			BiPredicate<IClientClaimsManager<?, ?, ?>, IPlayerConfigClientStorageManager<?>> visibilityGetter
	) {
		this.mode = mode;
		this.claimReflectionOwnerGetter = claimReflectionOwnerGetter;
		this.visibilityGetter = visibilityGetter;
	}

	public ClaimingMode getMode() {
		return mode;
	}

	public Function<IClientClaimsManager<?, ?, ?>, UUID> getClaimReflectionOwnerGetter() {
		return claimReflectionOwnerGetter;
	}

	public BiPredicate<IClientClaimsManager<?, ?, ?>, IPlayerConfigClientStorageManager<?>> getVisibilityGetter() {
		return visibilityGetter;
	}

	public static final class Builder {

		private ClaimingMode mode;
		private Function<IClientClaimsManager<?, ?, ?>, UUID> claimReflectionOwnerGetter;
		private BiPredicate<IClientClaimsManager<?, ?, ?>, IPlayerConfigClientStorageManager<?>> visibilityGetter;

		private Builder(){}

		public Builder setDefault(){
			setMode(null);
			setClaimReflectionOwnerGetter(null);
			return this;
		}

		public Builder setMode(ClaimingMode mode){
			this.mode = mode;
			return this;
		}

		public Builder setMode(IClaimingModeAPI mode){
			return setMode((ClaimingMode) mode);
		}


		public Builder setClaimReflectionOwnerGetter(Function<IClientClaimsManager<?, ?, ?>, UUID> claimReflectionOwnerGetter) {
			this.claimReflectionOwnerGetter = claimReflectionOwnerGetter;
			return this;
		}

		public Builder setVisibilityGetter(BiPredicate<IClientClaimsManager<?, ?, ?>, IPlayerConfigClientStorageManager<?>> visibilityGetter) {
			this.visibilityGetter = visibilityGetter;
			return this;
		}

		public ClientClaimingModeHandler build(Map<IClaimingModeAPI, ClientClaimingModeHandler> dest){
			if(mode == null || claimReflectionOwnerGetter == null)
				throw new IllegalStateException();
			ClientClaimingModeHandler result = new ClientClaimingModeHandler(mode, claimReflectionOwnerGetter, visibilityGetter);
			if(dest != null)
				dest.put(mode, result);
			return result;
		}

		public static Builder begin(){
			return new Builder().setDefault();
		}

	}

}
