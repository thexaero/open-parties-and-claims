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

package xaero.pac.common.server.core;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.claims.player.IPlayerClaimPosList;
import xaero.pac.common.claims.player.IPlayerDimensionClaims;
import xaero.pac.common.packet.ClientboundPacDimensionHandshakePacket;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.member.IPartyMember;
import xaero.pac.common.server.IServerData;
import xaero.pac.common.server.ServerData;
import xaero.pac.common.server.claims.IServerClaimsManager;
import xaero.pac.common.server.claims.IServerDimensionClaimsManager;
import xaero.pac.common.server.claims.IServerRegionClaims;
import xaero.pac.common.server.claims.player.IServerPlayerClaimInfo;
import xaero.pac.common.server.config.ServerConfig;
import xaero.pac.common.server.parties.party.IServerParty;

import javax.annotation.Nullable;

public class ServerCore {
	
	public static void onServerTickStart(MinecraftServer server) {
		OpenPartiesAndClaims.INSTANCE.startupCrashHandler.check();
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo>> serverData = ServerData.from(server);
		if(serverData != null)
			try {
				serverData.getServerTickHandler().onTick(serverData);
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
	}

	public static void onServerWorldInfo(Player player){
		OpenPartiesAndClaims.INSTANCE.getPacketHandler().sendToPlayer((ServerPlayer) player, new ClientboundPacDimensionHandshakePacket(ServerConfig.CONFIG.claimsEnabled.get(), ServerConfig.CONFIG.partiesEnabled.get()));
	}

	public static boolean canAddLivingEntityEffect(LivingEntity target, MobEffectInstance effect, @Nullable Entity source){
		if(source == null || !(source.level instanceof ServerLevel))
			return true;
		Level world = source.level;
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo>> serverData = ServerData.from(world.getServer());
		if(serverData == null)
			return true;
		boolean shouldProtect = serverData.getChunkProtection().onEntityInteract(serverData, source, target, InteractionHand.MAIN_HAND, false, true);
		return !shouldProtect;
	}

	public static boolean canSpreadFire(LevelReader levelReader, BlockPos pos){
		if(!(levelReader instanceof ServerLevel level))
			return true;
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo>> serverData = ServerData.from(level.getServer());
		if(serverData == null)
			return true;
		boolean shouldProtect = serverData.getChunkProtection().onFireSpread(serverData, level, pos);
		return !shouldProtect;
	}

	public static boolean mayUseItemAt(Player player, BlockPos pos, Direction direction, ItemStack itemStack){
		if(player.getServer() == null)
			return true;
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo>> serverData = ServerData.from(player.getServer());
		if(serverData == null)
			return true;
		boolean shouldProtect = serverData.getChunkProtection().onUseItemAt(serverData, player, pos, direction, itemStack);
		return !shouldProtect;
	}

	public static boolean replaceFluidCanPassThrough(boolean currentValue, BlockGetter blockGetter, BlockPos from, BlockPos to){
		if(!currentValue)
			return false;
		if(!(blockGetter instanceof ServerLevel level))
			return true;
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo>> serverData = ServerData.from(level.getServer());
		if(serverData == null)
			return true;
		boolean shouldProtect = serverData.getChunkProtection().onFluidSpread(serverData, level, from, to);
		return !shouldProtect;
	}

	public static DispenseItemBehavior replaceDispenseBehavior(DispenseItemBehavior defaultValue, ServerLevel level, BlockPos blockPos) {
		if(defaultValue == DispenseItemBehavior.NOOP)
			return defaultValue;
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo>> serverData = ServerData.from(level.getServer());
		if(serverData == null)
			return defaultValue;
		boolean shouldProtect = serverData.getChunkProtection().onDispenseFrom(serverData, level, blockPos);
		return shouldProtect ? DispenseItemBehavior.NOOP : defaultValue;
	}

}
