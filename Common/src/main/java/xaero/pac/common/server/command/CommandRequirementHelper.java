/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2024-2026, Xaero <xaero1996@gmail.com> and contributors
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

package xaero.pac.common.server.command;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import xaero.pac.OpenPartiesAndClaims;

import java.util.function.Predicate;

public class CommandRequirementHelper {

	public static Predicate<CommandSourceStack> onServerThread(Predicate<CommandSourceStack> requirement){
		return c -> {
			MinecraftServer server = c.getServer();
			if(server == null)
				return false;
			if(server.isSameThread())
				return requirement.test(c);
			if(!server.scheduleExecutables())//for example, after the server is stopped
				return false;
			return server.submit(() -> requirement.test(c)).join();
		};
	}

}
