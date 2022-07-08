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

package xaero.pac.common.event;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.claims.player.IPlayerClaimPosList;
import xaero.pac.common.claims.player.IPlayerDimensionClaims;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.member.IPartyMember;
import xaero.pac.common.server.IOpenPACMinecraftServer;
import xaero.pac.common.server.IServerData;
import xaero.pac.common.server.ServerData;
import xaero.pac.common.server.ServerDataInitializer;
import xaero.pac.common.server.claims.IServerClaimsManager;
import xaero.pac.common.server.claims.IServerDimensionClaimsManager;
import xaero.pac.common.server.claims.IServerRegionClaims;
import xaero.pac.common.server.claims.command.ClaimsCommandRegister;
import xaero.pac.common.server.claims.player.IServerPlayerClaimInfo;
import xaero.pac.common.server.command.CommonCommandRegister;
import xaero.pac.common.server.core.ServerCore;
import xaero.pac.common.server.parties.command.PartyCommandRegister;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.player.data.IOpenPACServerPlayer;
import xaero.pac.common.server.player.data.api.ServerPlayerDataAPI;

import java.util.List;

public class CommonEvents {

	protected final OpenPartiesAndClaims modMain;
	protected MinecraftServer lastServerStarted;
	
	public CommonEvents(OpenPartiesAndClaims modMain) {
		this.modMain = modMain;
	}

	public void onServerAboutToStart(MinecraftServer server) throws Throwable {
		lastServerStarted = server;
		OpenPartiesAndClaims.LOGGER.info("Initializing Open Parties and Claims for the server...");
		((IOpenPACMinecraftServer) lastServerStarted).setXaero_OPAC_ServerData(new ServerDataInitializer().init(modMain, lastServerStarted));
		modMain.getPacketHandler().onServerAboutToStart();
	}

	public void onServerStarting() {
		modMain.startupCrashHandler.check();
		ServerData.from(lastServerStarted).getServerLoadCallback().onLoad();
	}

	public void onPlayerRespawn(Player player) {
		if(player instanceof ServerPlayer) {
			IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo>> serverData = ServerData.from(player.getServer());
			if(serverData != null) {
				serverData.getPlayerWorldJoinHandler().onWorldJoin(serverData, (ServerLevel) player.getLevel(), (ServerPlayer) player);
			}
		}
	}

	public void onPlayerChangedDimension(Player player) {
		if(player instanceof ServerPlayer) {
			IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo>> serverData = ServerData.from(player.getServer());
			if(serverData != null) {
				serverData.getPlayerWorldJoinHandler().onWorldJoin(serverData, (ServerLevel) player.getLevel(), (ServerPlayer) player);
			}
		}
	}

	public void onPlayerLogIn(Player player) {
		if(player.getLevel() instanceof ServerLevel) {
			IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo>> serverData = ServerData.from(player.getServer());
			if(serverData != null) {
				serverData.getPlayerLoginHandler().handlePreWorldJoin((ServerPlayer) player, serverData);
				serverData.getPlayerWorldJoinHandler().onWorldJoin(serverData, (ServerLevel) player.getLevel(), (ServerPlayer) player);
				serverData.getPlayerLoginHandler().handlePostWorldJoin((ServerPlayer) player, serverData);
			}
		}
	}

	public void onPlayerClone(Player original, Player player) {
		if(original.getLevel() instanceof ServerLevel) {
			//copy player data on respawn/clone
			((IOpenPACServerPlayer)player).setXaero_OPAC_PlayerData(ServerPlayerDataAPI.from((ServerPlayer)original));
		}
	}

	public void onPlayerLogOut(Player player) {
		if(player.getLevel() instanceof ServerLevel) {
			IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo>> serverData = ServerData.from(player.getServer());
			if(serverData != null) {
				serverData.getPlayerLogoutHandler().handle((ServerPlayer) player, serverData);
			}
		}
	}

	public void onServerTick(boolean isTickStart) throws Throwable {
		if(isTickStart) {
			ServerCore.onServerTickStart(lastServerStarted);//also used by the core mod!
		}
	}

	public void onPlayerTick(boolean isTickStart, boolean isServerSide, Player player) throws Throwable {
		if(isServerSide && isTickStart) {
			IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo>> serverData = ServerData.from(player.getServer());
			if(serverData != null)
				serverData.getPlayerTickHandler().onTick((ServerPlayer) player, serverData);
		}
	}

	public void onServerStopped(MinecraftServer server) {
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo>> serverData = ServerData.from(server);
		if(serverData != null)
			serverData.onStop();
	}

	public void onRegisterCommands(CommandDispatcher<CommandSourceStack> dispatcher, Commands.CommandSelection environment) {
		new PartyCommandRegister().register(dispatcher, environment);
		new ClaimsCommandRegister().register(dispatcher, environment);
		new CommonCommandRegister().register(dispatcher, environment);
	}

	public boolean onLeftClickBlock(boolean isServerSide, Level world, BlockPos pos, Player player) {
		if(isServerSide) {
			IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo>> serverData = ServerData.from(world.getServer());
			return serverData.getChunkProtection().onLeftClickBlockServer(serverData, pos, player);
		}
		return false;
	}

	public boolean onDestroyBlock(LevelAccessor world, BlockPos pos, Player player) {
		if(world instanceof ServerLevel) {
			IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo>> serverData = ServerData.from(world.getServer());
			return serverData.getChunkProtection().onDestroyBlock(serverData, pos, player);
		}
		return false;
	}

	public boolean onRightClickBlock(boolean isServerSide, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitVec) {
		if(isServerSide) {
			IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo>> serverData = ServerData.from(world.getServer());
			return serverData.getChunkProtection().onRightClickBlock(serverData, player, hand, pos, hitVec);
		}
		return false;
	}

	public boolean onItemRightClick(boolean isServerSide, Level world, BlockPos pos, Player player, InteractionHand hand, ItemStack itemStack) {
		if(isServerSide) {
			IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo>> serverData = ServerData.from(world.getServer());
			return serverData.getChunkProtection().onItemRightClick(serverData, hand, itemStack, pos, player);
		}
		return false;
	}

	public boolean onMobGrief(Entity entity) {
		if(entity != null /*anonymous fireballs on Forge*/ && entity.getLevel() instanceof ServerLevel) {
			IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo>> serverData = ServerData.from(entity.getServer());
			return serverData.getChunkProtection().onMobGrief(serverData, entity);
		}
		return false;
	}

	public boolean onLivingHurt(DamageSource source, Entity target) {
		if(target.getLevel() instanceof ServerLevel) {
			if(!(source instanceof EntityDamageSource) &&
					!source.isDamageHelmet()/*almost certainly something falling from the top*/ && source != DamageSource.DRAGON_BREATH &&
					!source.isProjectile() && !source.isExplosion())
				return false;
			IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo>> serverData = ServerData.from(target.getServer());
			Entity effectiveSource = source.getEntity() != null ? source.getEntity() : source.getDirectEntity();
			return serverData.getChunkProtection().onEntityInteract(serverData, effectiveSource, target, InteractionHand.MAIN_HAND, source.getEntity() == source.getDirectEntity(), true);
		}
		return false;
	}

	protected boolean onEntityAttack(Player player, Entity target) {
		if(target.getLevel() instanceof ServerLevel) {
			IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo>> serverData = ServerData.from(target.getServer());
			return serverData.getChunkProtection().onEntityInteract(serverData, player, target, InteractionHand.MAIN_HAND, true, true);
		}
		return false;
	}

	public boolean onEntityInteract(Entity source, Entity target, InteractionHand hand) {
		if(target.getLevel() instanceof ServerLevel) {
			IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo>> serverData = ServerData.from(target.getServer());
			return serverData.getChunkProtection().onEntityInteract(serverData, source, target, hand, true, false);
		}
		return false;
	}

	public void onExplosionDetonate(Level world, Explosion explosion, List<Entity> affectedEntities, List<BlockPos> affectedBlocks) {
		if(world instanceof ServerLevel){
			IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo>> serverData = ServerData.from(world.getServer());
			serverData.getChunkProtection().onExplosionDetonate(serverData, (ServerLevel) world, explosion, affectedEntities, affectedBlocks);
		}
	}

	public boolean onChorusFruit(Entity entity, Vec3 target){
		if(entity.getLevel() instanceof ServerLevel){
			IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo>> serverData = ServerData.from(entity.getServer());
			return serverData.getChunkProtection().onChorusFruitTeleport(serverData, target, entity);
		}
		return false;
	}

	public void onEntityJoinWorld(Entity entity, Level world) {
		if(world instanceof ServerLevel){
			if(entity instanceof LightningBolt bolt) {
				IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo>> serverData = ServerData.from(entity.getServer());
				serverData.getChunkProtection().onLightningBolt(serverData, bolt);
			}
		}
	}
}
