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

package xaero.pac.common.event;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.util.Result;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.entity.*;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.entity.player.*;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.listener.Priority;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.server.permission.events.PermissionGatherEvent;
import org.apache.commons.lang3.tuple.Triple;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.claims.player.IPlayerClaimPosList;
import xaero.pac.common.claims.player.IPlayerDimensionClaims;
import xaero.pac.common.event.api.v2.OPACServerAddonRegisterEvent;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.ally.IPartyAlly;
import xaero.pac.common.parties.party.member.IPartyMember;
import xaero.pac.common.server.IServerData;
import xaero.pac.common.server.claims.IServerClaimsManager;
import xaero.pac.common.server.claims.IServerDimensionClaimsManager;
import xaero.pac.common.server.claims.IServerRegionClaims;
import xaero.pac.common.server.claims.player.IServerPlayerClaimInfo;
import xaero.pac.common.server.core.ServerCore;
import xaero.pac.common.server.data.ServerDataReloadListenerForge;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.player.permission.impl.ForgePermissionsSystem;

public class CommonEventsForge extends CommonEvents {

	public CommonEventsForge(OpenPartiesAndClaims modMain) {
		super(modMain);
	}

	@SubscribeEvent(priority = Priority.HIGHEST)
	public boolean onEntityPlaceBlock(BlockEvent.EntityPlaceEvent event) {
		return super.onEntityPlaceBlock(event.getLevel(), event.getPos(), event.getEntity(), event.getPlacedBlock(), event.getBlockSnapshot().getReplacedBlock());
	}

	@SubscribeEvent(priority = Priority.HIGHEST)
	public boolean onEntityMultiPlaceBlock(BlockEvent.EntityMultiPlaceEvent event) {
		return super.onEntityMultiPlaceBlock(event.getLevel(), event.getReplacedBlockSnapshots().stream().map(s -> Triple.of(s.getPos(), s.getReplacedBlock(), s.getCurrentBlock())), event.getEntity());
	}

	@SubscribeEvent
	public void onServerAboutToStart(ServerAboutToStartEvent event) throws Throwable {
		super.onServerAboutToStart(event.getServer());
	}

	@SubscribeEvent
	public void onServerStarting(ServerStartingEvent event) {
		super.onServerStarting(event.getServer());
	}
	
	@SubscribeEvent
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		super.onPlayerRespawn(event.getEntity());
	}
	
	@SubscribeEvent
	public void onPlayerChangedDimension(PlayerChangedDimensionEvent event) {
		super.onPlayerChangedDimension(event.getEntity());
	}
	
	@SubscribeEvent
	public void onPlayerLogIn(PlayerLoggedInEvent event) {
		super.onPlayerLogIn(event.getEntity());
	}

	@SubscribeEvent
	public void onPlayerClone(PlayerEvent.Clone event) {
		super.onPlayerClone(event.getOriginal(), event.getEntity());
	}
	
	@SubscribeEvent
	public void onPlayerLogOut(PlayerLoggedOutEvent event) {
		super.onPlayerLogOut(event.getEntity());
	}

	private void checkLastServerStarted(){
		if(lastServerStarted == null || !lastServerStarted.isSameThread())
			throw new RuntimeException("The last recorded server does not have the expected value!");
	}

	@SubscribeEvent
	public void onServerTick(ServerTickEvent.Pre event) throws Throwable {
		//TODO probably need to stop using this event that doesn't provide the server instance
		checkLastServerStarted();
		super.onServerTick(lastServerStarted, true);
	}

	@SubscribeEvent
	public void onServerTick(ServerTickEvent.Post event) throws Throwable {
		//TODO probably need to stop using this event that doesn't provide the server instance
		checkLastServerStarted();
		super.onServerTick(lastServerStarted, false);
	}
	
	@SubscribeEvent
	public void onPlayerTick(PlayerTickEvent.Pre event) throws Throwable {
		super.onPlayerTick(true, event.side == LogicalSide.SERVER, event.player);
	}

	@SubscribeEvent
	public void onPlayerTick(PlayerTickEvent.Post event) throws Throwable {
		super.onPlayerTick(false, event.side == LogicalSide.SERVER, event.player);
	}
	
	@SubscribeEvent
	public void onServerStopped(ServerStoppedEvent event) {
		super.onServerStopped(event.getServer());
	}
	
	@SubscribeEvent
	public void onRegisterCommands(RegisterCommandsEvent event) {
		super.onRegisterCommands(event.getDispatcher(), event.getCommandSelection());
	}

	@SubscribeEvent(priority = Priority.HIGHEST)
	public boolean onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
		return super.onLeftClickBlock(event.getLevel(), event.getPos(), event.getEntity());
	}

	@SubscribeEvent(priority = Priority.HIGHEST)
	public boolean onDestroyBlock(BlockEvent.BreakEvent event) {
		return super.onDestroyBlock(event.getLevel(), event.getPos(), event.getPlayer());
	}

	@SubscribeEvent(priority = Priority.HIGHEST)
	public boolean onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		return super.onRightClickBlock(event.getLevel(), event.getPos(), event.getEntity(), event.getHand(), event.getHitVec());
	}

	@SubscribeEvent(priority = Priority.HIGHEST)
	public boolean onItemRightClick(PlayerInteractEvent.RightClickItem event) {
		return super.onItemRightClick(event.getLevel(), event.getPos(), event.getEntity(), event.getHand(), event.getItemStack());
	}

	@SubscribeEvent(priority = Priority.HIGHEST)
	public boolean onItemUseTick(LivingEntityUseItemEvent.Tick event) {
		return super.onItemUseTick(event.getEntity(), event.getItem());
	}

	@SubscribeEvent(priority = Priority.HIGHEST)
	public boolean onItemUseTick(LivingEntityUseItemEvent.Stop event) {
		return super.onItemUseStop(event.getEntity(), event.getItem());
	}

	@SubscribeEvent
	public void onMobGrief(EntityMobGriefingEvent event) {
		if(event.getEntity() == null)
			return;
		MinecraftServer server = event.getEntity().getServer();
		if(server == null)
			return;
		if(!server.isSameThread())
			return;
		boolean items = ServerCore.isMobGriefingForItems(server.getTickCount());
		//^ this means that the mob griefing rule is being checked for item pickup
		if(
				!OpenPartiesAndClaims.INSTANCE.getModSupport().OPTIFINE &&
				//^ with optifine, MixinForgeMob injection into aiStep breaks,
				//which breaks item pickup protection, so we have to use the mob griefing check
				items
		)
			return;
		if(super.onMobGrief(event.getEntity(), items))
			event.setResult(Result.DENY);
	}

	@SubscribeEvent(priority = Priority.HIGHEST)
	public boolean onLivingHurt(LivingAttackEvent event) {
		return super.onLivingHurt(event.getSource(), event.getEntity());
	}

	@SubscribeEvent(priority = Priority.HIGHEST)
	public boolean onEntityAttack(AttackEntityEvent event) {
		return super.onEntityAttack(event.getEntity(), event.getTarget());
	}

	@SubscribeEvent(priority = Priority.HIGHEST)
	public boolean onEntityInteract(PlayerInteractEvent.EntityInteract event) {
		return super.onEntityInteract(event.getEntity(), event.getTarget(), event.getHand());
	}

	@SubscribeEvent(priority = Priority.HIGHEST)
	public boolean onInteractEntitySpecific(PlayerInteractEvent.EntityInteractSpecific event) {
		return super.onInteractEntitySpecific(event.getEntity(), event.getTarget(), event.getHand());
	}
	
	@SubscribeEvent
	public void onExplosionDetonate(ExplosionEvent.Detonate event) {
		super.onExplosionDetonate(event.getLevel(), event.getExplosion(), event.getAffectedEntities(), event.getAffectedBlocks());
	}

	@SubscribeEvent(priority = Priority.HIGHEST)
	public boolean onChorusFruit(EntityTeleportEvent.ChorusFruit event){
		return super.onChorusFruit(event.getEntity(), event.getTarget());
	}

	@SubscribeEvent
	public boolean onEntityJoinWorld(EntityJoinLevelEvent event){
		return super.onEntityJoinWorld(event.getEntity(), event.getLevel(), event.loadedFromDisk());
	}

	@SubscribeEvent(priority = Priority.HIGHEST)
	public void onEntityEnteringSection(EntityEvent.EnteringSection event){
		super.onEntityEnteringSection(event.getEntity(), event.getOldPos(), event.getNewPos(), event.didChunkChange());
	}

	@SubscribeEvent
	public void onPermissionsChanged(PermissionsChangedEvent event){
		if(event.getEntity() instanceof ServerPlayer serverPlayer)
			super.onPermissionsChanged(serverPlayer);
	}

	@SubscribeEvent(priority = Priority.HIGHEST)
	public boolean onCropTrample(BlockEvent.FarmlandTrampleEvent event) {
		return super.onCropTrample(event.getEntity(), event.getPos());
	}

	@SubscribeEvent(priority = Priority.HIGHEST)
	public boolean onBucketUse(FillBucketEvent event){
		return super.onBucketUse(event.getEntity(), event.getLevel(), event.getTarget(), event.getEmptyBucket());
	}

	@SubscribeEvent
	public void onTagsUpdate(TagsUpdatedEvent event) {
		super.onTagsUpdate();
	}

	@SubscribeEvent(priority = Priority.HIGHEST)
	public boolean onItemPickup(EntityItemPickupEvent event){
		return super.onItemPickup(event.getEntity(), event.getItem());
	}

	@SubscribeEvent(priority = Priority.HIGHEST)
	public boolean onMobCheckSpawn(MobSpawnEvent.FinalizeSpawn event){
		if(event.getEntity().isAddedToWorld())
			return false;
		if(super.onMobSpawn(event.getEntity(), event.getX(), event.getY(), event.getZ(), event.getSpawnReason())) {
			event.setSpawnCancelled(true);//won't be spawned
			return true;//won't call finalizeSpawn
		}
		return false;
	}

	@SubscribeEvent(priority = Priority.HIGHEST)
	public void onProjectileImpact(ProjectileImpactEvent event){
		if(super.onProjectileImpact(event.getRayTraceResult(), event.getProjectile()))
			event.setImpactResult(ProjectileImpactEvent.ImpactResult.STOP_AT_CURRENT_NO_DAMAGE);
	}

	@SubscribeEvent
	public void onAddReloadListenerEvent(AddReloadListenerEvent event){
		event.addListener(new ServerDataReloadListenerForge());
	}

	@SubscribeEvent
	public void onAddonRegister(OPACServerAddonRegisterEvent event){
		super.onAddonRegister(event.getServer(), event.getPermissionSystemManager(), event.getPartySystemManagerAPI(), event.getClaimsManagerTrackerAPI());

		event.getPermissionSystemManager().register("permission_api", new ForgePermissionsSystem());
	}

	@SubscribeEvent
	protected void onForgePermissionGather(PermissionGatherEvent.Nodes event) {
		ForgePermissionsSystem.registerNodes(event);
	}

	@Override
	public void fireAddonRegisterEvent(IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData) {
		OPACServerAddonRegisterEvent.BUS.post(new OPACServerAddonRegisterEvent(serverData.getServer(), serverData.getPlayerPermissionSystemManager(), serverData.getPlayerPartySystemManager(), serverData.getServerClaimsManager().getTracker()));

		//TODO remove this when the deprecated event is removed
		xaero.pac.common.event.api.OPACServerAddonRegisterEvent.BUS.post(new xaero.pac.common.event.api.OPACServerAddonRegisterEvent(serverData.getServer(), serverData.getPlayerPermissionSystemManager(), serverData.getPlayerPartySystemManager(), serverData.getServerClaimsManager().getTracker()));
	}

}
