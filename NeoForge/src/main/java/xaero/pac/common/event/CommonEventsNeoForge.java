/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2022-2023, Xaero <xaero1996@gmail.com> and contributors
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
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.neoforge.event.entity.EntityEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityMobGriefingEvent;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.neoforge.event.entity.living.LivingAttackEvent;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.server.permission.events.PermissionGatherEvent;
import org.apache.commons.lang3.tuple.Pair;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.claims.player.IPlayerClaimPosList;
import xaero.pac.common.claims.player.IPlayerDimensionClaims;
import xaero.pac.common.event.api.OPACServerAddonRegisterEvent;
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
import xaero.pac.common.server.player.permission.impl.NeoForgePermissionsSystem;

public class CommonEventsNeoForge extends CommonEvents {

	public CommonEventsNeoForge(OpenPartiesAndClaims modMain) {
		super(modMain);
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onEntityPlaceBlock(BlockEvent.EntityPlaceEvent event) {
		if(super.onEntityPlaceBlock(event.getLevel(), event.getPos(), event.getEntity(), event.getPlacedBlock(), event.getBlockSnapshot().getReplacedBlock()))
			event.setCanceled(true);
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onEntityMultiPlaceBlock(BlockEvent.EntityMultiPlaceEvent event) {
		if(super.onEntityMultiPlaceBlock(event.getLevel(), event.getReplacedBlockSnapshots().stream().map(s -> Pair.of(s.getPos(), s.getCurrentBlock())), event.getEntity()))
			event.setCanceled(true);
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
	public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
		super.onPlayerRespawn(event.getEntity());
	}
	
	@SubscribeEvent
	public void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
		super.onPlayerChangedDimension(event.getEntity());
	}
	
	@SubscribeEvent
	public void onPlayerLogIn(PlayerEvent.PlayerLoggedInEvent event) {
		super.onPlayerLogIn(event.getEntity());
	}

	@SubscribeEvent
	public void onPlayerClone(PlayerEvent.Clone event) {
		super.onPlayerClone(event.getOriginal(), event.getEntity());
	}
	
	@SubscribeEvent
	public void onPlayerLogOut(PlayerEvent.PlayerLoggedOutEvent event) {
		super.onPlayerLogOut(event.getEntity());
	}
	
	@SubscribeEvent
	public void onServerTick(TickEvent.ServerTickEvent event) throws Throwable {
		//TODO probably need to stop using this event that doesn't provide the server instance
		if(lastServerStarted == null || !lastServerStarted.isSameThread())
			throw new RuntimeException("The last recorded server does not have the expected value!");
		super.onServerTick(lastServerStarted, event.phase == TickEvent.Phase.START);
	}
	
	@SubscribeEvent
	public void onPlayerTick(TickEvent.PlayerTickEvent event) throws Throwable {
		super.onPlayerTick(event.phase == TickEvent.Phase.START, event.side == LogicalSide.SERVER, event.player);
	}
	
	@SubscribeEvent
	public void onServerStopped(ServerStoppedEvent event) {
		super.onServerStopped(event.getServer());
	}
	
	@SubscribeEvent
	public void onRegisterCommands(RegisterCommandsEvent event) {
		super.onRegisterCommands(event.getDispatcher(), event.getCommandSelection());
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
		if(super.onLeftClickBlock(event.getLevel(), event.getPos(), event.getEntity()))
			event.setCanceled(true);
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onDestroyBlock(BlockEvent.BreakEvent event) {
		if(super.onDestroyBlock(event.getLevel(), event.getPos(), event.getPlayer()))
			event.setCanceled(true);
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		if(super.onRightClickBlock(event.getLevel(), event.getPos(), event.getEntity(), event.getHand(), event.getHitVec()))
			event.setCanceled(true);
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onItemRightClick(PlayerInteractEvent.RightClickItem event) {
		if(super.onItemRightClick(event.getLevel(), event.getPos(), event.getEntity(), event.getHand(), event.getItemStack()))
			event.setCanceled(true);
	}

	@SubscribeEvent
	public void onMobGrief(EntityMobGriefingEvent event) {
		if(event.getEntity() == null)
			return;
		MinecraftServer server = event.getEntity().getServer();
		if(server == null)
			return;
		if(ServerCore.isMobGriefingForItems(server.getTickCount()))//this means that the mob griefing rule is being checked for item pickup
			return;
		if(super.onMobGrief(event.getEntity()))
			event.setResult(Event.Result.DENY);
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onLivingHurt(LivingAttackEvent event) {
		if(super.onLivingHurt(event.getSource(), event.getEntity()))
			event.setCanceled(true);
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onEntityAttack(AttackEntityEvent event) {
		if(super.onEntityAttack(event.getEntity(), event.getTarget()))
			event.setCanceled(true);
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
		if(super.onEntityInteract(event.getEntity(), event.getTarget(), event.getHand()))
			event.setCanceled(true);
	}
	
	@SubscribeEvent
	public void onExplosionDetonate(ExplosionEvent.Detonate event) {
		super.onExplosionDetonate(event.getLevel(), event.getExplosion(), event.getAffectedEntities(), event.getAffectedBlocks());
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onChorusFruit(EntityTeleportEvent.ChorusFruit event){
		if(super.onChorusFruit(event.getEntity(), event.getTarget()))
			event.setCanceled(true);
	}

	@SubscribeEvent
	public void onEntityJoinWorld(EntityJoinLevelEvent event){
		if(super.onEntityJoinWorld(event.getEntity(), event.getLevel(), event.loadedFromDisk()))
			event.setCanceled(true);
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onEntityEnteringSection(EntityEvent.EnteringSection event){
		super.onEntityEnteringSection(event.getEntity(), event.getOldPos(), event.getNewPos(), event.didChunkChange());
	}

	@SubscribeEvent
	public void onPermissionsChanged(PermissionsChangedEvent event){
		if(event.getEntity() instanceof ServerPlayer serverPlayer)
			super.onPermissionsChanged(serverPlayer);
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onCropTrample(BlockEvent.FarmlandTrampleEvent event) {
		if(super.onCropTrample(event.getEntity(), event.getPos()))
			event.setCanceled(true);
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onBucketUse(FillBucketEvent event){
		if(super.onBucketUse(event.getEntity(), event.getLevel(), event.getTarget(), event.getEmptyBucket()))
			event.setCanceled(true);
	}

	@SubscribeEvent
	public void onTagsUpdate(TagsUpdatedEvent event) {
		super.onTagsUpdate();
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onItemPickup(EntityItemPickupEvent event){
		if(super.onItemPickup(event.getEntity(), event.getItem()))
			event.setCanceled(true);
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onMobCheckSpawn(MobSpawnEvent.FinalizeSpawn event){
		if(super.onMobSpawn(event.getEntity(), event.getX(), event.getY(), event.getZ(), event.getSpawnType())) {
			event.setSpawnCancelled(true);//won't be spawned
			event.setCanceled(true);//won't call finalizeSpawn
		}
	}

	@SubscribeEvent
	public void onAddReloadListenerEvent(AddReloadListenerEvent event){
		event.addListener(new ServerDataReloadListenerForge());
	}

	@SubscribeEvent
	public void onAddonRegister(OPACServerAddonRegisterEvent event){
		super.onAddonRegister(event.getServer(), event.getPermissionSystemManager(), event.getPartySystemManagerAPI(), event.getClaimsManagerTrackerAPI());

		event.getPermissionSystemManager().register("permission_api", new NeoForgePermissionsSystem());
	}

	@SubscribeEvent
	protected void onForgePermissionGather(PermissionGatherEvent.Nodes event) {
		NeoForgePermissionsSystem.registerNodes(event);
	}

	@Override
	protected void fireAddonRegisterEvent(IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData) {
		NeoForge.EVENT_BUS.post(new OPACServerAddonRegisterEvent(serverData.getServer(), serverData.getPlayerPermissionSystemManager(), serverData.getPlayerPartySystemManager(), serverData.getServerClaimsManager().getTracker()));
	}

}
