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

package xaero.pac.common.server.claims.action.listener.override.api;

import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A claim action permission override
 */
public class ClaimActionPermissionOverride {

	private final ClaimActionPermissionOverrideType type;
	private final Component reason;

	/**
	 * Constructs a claim action permission override instance with a specified type and reason.
	 * <p>
	 * At the moment, the reason for the override is only ever displayed when forbidding a claiming action. But feel
	 * free to provide a reason for a non-forbidding override. It might be used in the future.
	 * <p>
	 * All override types can be seen at {@link ClaimActionPermissionOverrideType}.
	 *
	 * @param type  the type of the override, not null
	 * @param reason  the reason for the override, can be null
	 */
	public ClaimActionPermissionOverride(@Nonnull ClaimActionPermissionOverrideType type, @Nullable Component reason) {
		this.type = type;
		this.reason = reason;
	}

	/**
	 * Gets the override type.
	 *
	 * @return the override type, not null
	 */
	@Nonnull
	public ClaimActionPermissionOverrideType getType() {
		return type;
	}

	/**
	 * Gets the reason for the override
	 *
	 * @return the override reason, can be null
	 */
	@Nullable
	public Component getReason() {
		return reason;
	}

}
