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

package xaero.pac.common.server.claims.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.packet.ClientboundModesPacket;
import xaero.pac.common.server.config.ServerConfig;
import xaero.pac.common.server.player.data.ServerPlayerData;
import xaero.pac.common.server.player.data.api.ServerPlayerDataAPI;

public class ClaimsAdminModeCommand {

	public void register(CommandDispatcher<CommandSourceStack> dispatcher, Commands.CommandSelection environment) {
		LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal(ClaimsCommandRegister.COMMAND_PREFIX).requires(context -> ServerConfig.CONFIG.claimsEnabled.get()).then(Commands.literal("admin-mode")
				.requires(context -> {
						if(context.hasPermission(2))
							return true;
						try {
							ServerPlayerDataAPI playerData = ServerPlayerDataAPI.from(context.getPlayerOrException());
							if(playerData == null)
								return false;
							return playerData.isClaimsAdminMode();//lets you turn off admin mode even if you're not op
						} catch (CommandSyntaxException e) {
							return false;
						}
					})
				.executes(context -> {
					ServerPlayer player = context.getSource().getPlayerOrException();
					ServerPlayerData mainCapability = (ServerPlayerData) ServerPlayerDataAPI.from(player);
					mainCapability.setClaimsAdminMode(!mainCapability.isClaimsAdminMode());
					mainCapability.setClaimsNonallyMode(false);
					player.sendMessage(new TranslatableComponent(mainCapability.isClaimsAdminMode() ? "gui.xaero_claims_admin_mode_enabled" : "gui.xaero_claims_admin_mode_disabled"), player.getUUID());
					OpenPartiesAndClaims.INSTANCE.getPacketHandler().sendToPlayer(player, new ClientboundModesPacket(mainCapability.isClaimsAdminMode()));
					return 1;
				}));
		dispatcher.register(command);
	}

}
