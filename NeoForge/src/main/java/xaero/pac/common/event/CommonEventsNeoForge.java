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

import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.TriState;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.entity.XpOrbTargetingEvent;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.entity.*;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.server.permission.events.PermissionGatherEvent;
import org.apache.commons.lang3.tuple.Triple;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.claims.player.IPlayerClaimPosList;
import xaero.pac.common.claims.player.IPlayerDimensionClaims;
import xaero.pac.common.event.api.OPACServerAddonRegisterEventContext;
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
import xaero.pac.common.server.data.ServerDataReloadListenerNeoForge;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.player.permission.impl.NeoForgePermissionsSystem;
import xaero.pac.common.server.world.ServerLevelHelper;

public class CommonEventsNeoForge extends CommonEvents {

	public CommonEventsNeoForge(OpenPartiesAndClaims modMain) {
		super(modMain);
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onEntityPlaceBlock(BlockEvent.EntityPlaceEvent event) {
		if(super.onEntityPlaceBlock(event.getLevel(), event.getPos(), event.getEntity(), event.getPlacedBlock(), event.getBlockSnapshot().getState()))
			event.setCanceled(true);
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onEntityMultiPlaceBlock(BlockEvent.EntityMultiPlaceEvent event) {
		if(super.onEntityMultiPlaceBlock(event.getLevel(), event.getReplacedBlockSnapshots().stream().map(s -> Triple.of(s.getPos(), s.getState(), s.getCurrentState())), event.getEntity()))
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

	private void onServerTick(ServerTickEvent event) throws Throwable {
		//TODO probably need to stop using this event that doesn't provide the server instance
		if(lastServerStarted == null || !lastServerStarted.isSameThread())
			throw new RuntimeException("The last recorded server does not have the expected value!");
		super.onServerTick(lastServerStarted, event instanceof ServerTickEvent.Pre);
	}

	@SubscribeEvent
	public void onServerTickPre(ServerTickEvent.Pre event) throws Throwable {
		onServerTick(event);
	}

	@SubscribeEvent
	public void onServerTickPost(ServerTickEvent.Post event) throws Throwable {
		onServerTick(event);
	}

	private void onPlayerTick(PlayerTickEvent event) throws Throwable {
		super.onPlayerTick(event instanceof PlayerTickEvent.Pre, ServerLevelHelper.getServer(event.getEntity()) != null, event.getEntity());
	}

	@SubscribeEvent
	public void onPlayerTickPre(PlayerTickEvent.Pre event) throws Throwable {
		onPlayerTick(event);
	}

	@SubscribeEvent
	public void onPlayerTickPost(PlayerTickEvent.Post event) throws Throwable {
		onPlayerTick(event);
	}
	
	@SubscribeEvent
	public void onServerStopping(ServerStoppingEvent event) {
		super.onServerStopping(event.getServer());
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
	public void onDestroyBlock(BreakBlockEvent event) {
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

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onItemUseTick(LivingEntityUseItemEvent.Tick event) {
		if(super.onItemUseTick(event.getEntity(), event.getItem()))
			event.setCanceled(true);
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onItemUseTick(LivingEntityUseItemEvent.Stop event) {
		if(super.onItemUseStop(event.getEntity(), event.getItem()))
			event.setCanceled(true);
	}

	@SubscribeEvent
	public void onMobGrief(EntityMobGriefingEvent event) {
		if(event.getEntity() == null)
			return;
		MinecraftServer server = ServerLevelHelper.getServer(event.getEntity());
		if(server == null)
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
			event.setCanGrief(false);
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onLivingHurt(LivingIncomingDamageEvent event) {
		if(super.onLivingHurt(event.getSource(), event.getEntity()))
			event.setCanceled(true);
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onEntityAttack(AttackEntityEvent event) {
		if(super.onEntityAttack(event.getEntity(), event.getTarget()))
			event.setCanceled(true);
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onInteractEntity(PlayerInteractEvent.EntityInteract event) {
		if(super.onInteractEntitySpecific(event.getEntity(), event.getTarget(), event.getHand()))
			event.setCanceled(true);
	}
	
	@SubscribeEvent
	public void onExplosionDetonate(ExplosionEvent.Detonate event) {
		super.onExplosionDetonate(event.getLevel(), event.getExplosion(), event.getAffectedEntities(), event.getAffectedBlocks());
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onChorusFruit(EntityTeleportEvent.ItemConsumption event){
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

	@SubscribeEvent
	public void onTagsUpdate(TagsUpdatedEvent event) {
		super.onTagsUpdate();
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onItemPickup(ItemEntityPickupEvent.Pre event){
		if(super.onItemPickup(event.getPlayer(), event.getItemEntity()))
			event.setCanPickup(TriState.FALSE);
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onMobCheckSpawn(FinalizeSpawnEvent event){
		if(event.getEntity().isAddedToLevel())
			return;
		if(super.onMobSpawn(event.getEntity(), event.getX(), event.getY(), event.getZ(), event.getSpawnType())) {
			event.setSpawnCancelled(true);//won't be spawned
			event.setCanceled(true);//won't call finalizeSpawn
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onProjectileImpact(ProjectileImpactEvent event){
		if(super.onProjectileImpact(event.getRayTraceResult(), event.getProjectile()))
			event.setCanceled(true);
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)//to try to execute last
	public void onXpOrbTargetting(XpOrbTargetingEvent event){
		event.setFollowingPlayer(ServerCore.onExperiencePickup(event.getFollowingPlayer(), event.getXpOrb()));
	}

	@SubscribeEvent
	public void onAddReloadListenerEvent(AddServerReloadListenersEvent event){
		event.addListener(Identifier.fromNamespaceAndPath(OpenPartiesAndClaims.MOD_ID, "main"),
				new ServerDataReloadListenerNeoForge());
	}

	@SubscribeEvent
	public void onAddonRegister(OPACServerAddonRegisterEvent event){
		super.onAddonRegister(event.getContext());

		event.getContext().getPermissionSystemManagerAPI().register("permission_api", new NeoForgePermissionsSystem());
	}

	@SubscribeEvent
	protected void onForgePermissionGather(PermissionGatherEvent.Nodes event) {
		NeoForgePermissionsSystem.registerNodes(event);
	}

	@Override
	public void fireAddonRegisterEvent(OPACServerAddonRegisterEventContext context, IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData) {
		NeoForge.EVENT_BUS.post(new OPACServerAddonRegisterEvent(context));

		//TODO remove this when the deprecated event is removed
		NeoForge.EVENT_BUS.post(new xaero.pac.common.event.api.OPACServerAddonRegisterEvent(serverData.getServer(), serverData.getPlayerPermissionSystemManager(), serverData.getPlayerPartySystemManager(), serverData.getServerClaimsManager().getTracker()));
	}

}
