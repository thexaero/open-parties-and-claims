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

package xaero.pac.common.server.claims.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.claims.player.IPlayerClaimPosList;
import xaero.pac.common.claims.player.IPlayerDimensionClaims;
import xaero.pac.common.packet.claims.ClientboundClaimModesPacket;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.ally.IPartyAlly;
import xaero.pac.common.parties.party.member.IPartyMember;
import xaero.pac.common.server.IServerData;
import xaero.pac.common.server.ServerData;
import xaero.pac.common.server.claims.IServerClaimsManager;
import xaero.pac.common.server.claims.IServerDimensionClaimsManager;
import xaero.pac.common.server.claims.IServerRegionClaims;
import xaero.pac.common.server.claims.player.IServerPlayerClaimInfo;
import xaero.pac.common.server.command.CommandRequirementHelper;
import xaero.pac.common.server.config.ServerConfig;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.player.data.ServerPlayerData;
import xaero.pac.common.server.player.data.api.ServerPlayerDataAPI;
import xaero.pac.common.server.player.localization.AdaptiveLocalizer;
import xaero.pac.common.server.world.ServerLevelHelper;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

public class ClaimsImpersonateCommand {

	public void register(CommandDispatcher<CommandSourceStack> dispatcher, Commands.CommandSelection environment) {
		LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal(ClaimsCommandRegister.COMMAND_PREFIX)
				.requires(context -> ServerConfig.CONFIG.claimsEnabled.get())
				.then(Commands.literal("impersonate")
				.requires(CommandRequirementHelper.onServerThread(context -> {
						if(context.hasPermission(2) )
							return true;
						try {
							ServerPlayer player = context.getPlayerOrException();
							MinecraftServer server = ServerLevelHelper.getServer(player);
							IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
									serverData = ServerData.from(server);
							if(serverData.getServerClaimsManager().getPermissionHandler().playerHasImpersonationPermission(player))
								return true;
							return ServerPlayerDataAPI.from(player).getClaimsImpersonationInfo().getPlayerId() != null;//lets you disable the impersonation
						} catch (CommandSyntaxException e) {
							return false;
						}
				}))
				.executes(context -> execute(context, true))
				.then(Commands.argument("player", GameProfileArgument.gameProfile())
				.executes(context -> execute(context, false))));
		dispatcher.register(command);
	}

	private static int execute(CommandContext<CommandSourceStack> context, boolean disable) throws CommandSyntaxException {
		ServerPlayer player = context.getSource().getPlayerOrException();
		MinecraftServer server = ServerLevelHelper.getServer(player);
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
				serverData = ServerData.from(server);
		AdaptiveLocalizer adaptiveLocalizer = serverData.getAdaptiveLocalizer();
		NameAndId toImpersonate = null;
		ServerPlayerData playerData = (ServerPlayerData) ServerPlayerDataAPI.from(player);
		if(!disable) {
			Collection<NameAndId> profileCollection = GameProfileArgument.getGameProfiles(context, "player");
			if (profileCollection.isEmpty()) {
				context.getSource().sendFailure(adaptiveLocalizer.getFor(player, "gui.xaero_claims_impersonate_unknown_player"));
				return 0;
			}
			if (profileCollection.size() > 1) {
				context.getSource().sendFailure(adaptiveLocalizer.getFor(player, "gui.xaero_claims_impersonate_too_many_players"));
				return 0;
			}
			toImpersonate = profileCollection.iterator().next();
			if(player.getUUID().equals(toImpersonate.id()) || Objects.equals(toImpersonate.id(), playerData.getClaimsImpersonationInfo().getPlayerId())) {
				toImpersonate = null;
				disable = true;
			}
		}
		UUID idToImpersonate = toImpersonate == null ? null : toImpersonate.id();
		if(idToImpersonate == null)
			disable = true;
		playerData.getClaimsImpersonationInfo().reset();
		playerData.setClaimingMode(null);
		playerData.getClaimsImpersonationInfo().setPlayerId(idToImpersonate);
		Component impersonatedName = disable ? null : Component.literal(toImpersonate.name()).withStyle(ChatFormatting.GREEN);
		player.sendSystemMessage(
				adaptiveLocalizer.getFor(player,
						disable ? Component.translatable("gui.xaero_claims_impersonate_disabled") :
								Component.translatable("gui.xaero_claims_impersonate_enabled", impersonatedName)
				)
		);
		OpenPartiesAndClaims.INSTANCE.getPacketHandler().sendToPlayer(player, ClientboundClaimModesPacket.get(playerData));
		return 1;
	}

}
