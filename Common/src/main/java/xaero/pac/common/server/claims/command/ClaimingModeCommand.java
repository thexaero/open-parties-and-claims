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

package xaero.pac.common.server.claims.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.claims.player.IPlayerClaimPosList;
import xaero.pac.common.claims.player.IPlayerDimensionClaims;
import xaero.pac.common.claims.player.mode.ClaimingMode;
import xaero.pac.common.claims.result.api.ClaimResult;
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
import xaero.pac.common.server.config.ServerConfig;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.player.data.ServerPlayerData;
import xaero.pac.common.server.player.data.api.ServerPlayerDataAPI;
import xaero.pac.common.server.player.localization.AdaptiveLocalizer;
import xaero.pac.common.server.world.ServerLevelHelper;

import java.util.UUID;
import java.util.function.Predicate;

public class ClaimingModeCommand {

	public static final String DEFAULT_MODE_PREFIX = "default";
	private final Component DEFAULT_MODE_ENABLED = Component.translatable("gui.xaero_claims_default_mode_enabled");

	private final ClaimingMode mode;

	public ClaimingModeCommand(ClaimingMode mode) {
		this.mode = mode;
	}

	public void register(CommandDispatcher<CommandSourceStack> dispatcher, Commands.CommandSelection environment) {
		String modeId = mode == null ? DEFAULT_MODE_PREFIX : mode.getId();
		Predicate<CommandSourceStack> requirement = mode == null ? s -> true : mode.getCommandVisibilityRequirement();
		LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal(ClaimsCommandRegister.COMMAND_PREFIX).requires(context -> ServerConfig.CONFIG.claimsEnabled.get())
				.then(Commands.literal(modeId + "-claim-mode")
				.requires(requirement)
				.executes(context -> {
					ServerPlayer player = context.getSource().getPlayerOrException();
					MinecraftServer server = ServerLevelHelper.getServer(player);
					IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
							serverData = ServerData.from(server);
					AdaptiveLocalizer adaptiveLocalizer = serverData.getAdaptiveLocalizer();
					ServerPlayerData playerData = (ServerPlayerData) ServerPlayerDataAPI.from(player);
					if(mode != null && mode.getPermissionChecker() != null){
						UUID permissionCheckPlayerId = player.getUUID();
						if(mode.canBeImpersonated()) {
							serverData.getServerClaimsManager().getPermissionHandler().ensureImpersonationPermission(player, playerData);
							if (playerData.getClaimsImpersonationInfo().getPlayerId() != null)
								permissionCheckPlayerId = playerData.getClaimsImpersonationInfo().getPlayerId();
						}
						ClaimResult.Type failureType = mode.getPermissionChecker().apply(permissionCheckPlayerId, serverData.getServerClaimsManager());
						if (failureType != null) {
							serverData.getServerClaimsManager().getPermissionHandler().resetClaimingMode(player);
							context.getSource().sendFailure(adaptiveLocalizer.getFor(player, failureType.message));
							return 0;
						}
					}
					boolean enabling = playerData.getRawClaimingMode() != mode;
					playerData.setClaimingMode(enabling ? mode : null);
					player.sendSystemMessage(
							adaptiveLocalizer.getFor(
									player,
									mode == null ? DEFAULT_MODE_ENABLED : enabling ? mode.getEnableMessage() : mode.getDisableMessage()
							)
					);
					OpenPartiesAndClaims.INSTANCE.getPacketHandler().sendToPlayer(player, ClientboundClaimModesPacket.get(playerData));
					return 1;
				}));
		dispatcher.register(command);
	}

}
