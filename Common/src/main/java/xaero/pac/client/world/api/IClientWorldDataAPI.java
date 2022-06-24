/*
 *     Open Parties and Claims - adds chunk claims and player parties to Minecraft
 *     Copyright (C) 2022, Xaero <xaero1996@gmail.com> and contributors
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of version 3 of the GNU Lesser General Public License
 *     (LGPL-3.0-only) as published by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received copies of the GNU Lesser General Public License
 *     and the GNU General Public License along with this program.
 *     If not, see <https://www.gnu.org/licenses/>.
 */

package xaero.pac.client.world.api;

/**
 * API for data attached to a client world
 */
public interface IClientWorldDataAPI {

	/**
	 * Checks whether the handshake has been received from the server after entering this world/dimension indicating that
	 * this mod is installed on the server side.
	 *
	 * @return true if the mod is installed on the server side and the handshake has been received, otherwise false
	 */
	public boolean serverHasMod();

	/**
	 * If {@link #serverHasMod()} is true, then this checks if the server has the claims feature enabled.
	 *
	 * @return true if the server has the claims feature enabled, otherwise false
	 */
	public boolean serverHasClaimsEnabled();

	/**
	 * If {@link #serverHasMod()} is true, then this checks if the server has the parties feature enabled.
	 *
	 * @return true if the server has the parties feature enabled, otherwise false
	 */
	public boolean serverHasPartiesEnabled();
	
}
