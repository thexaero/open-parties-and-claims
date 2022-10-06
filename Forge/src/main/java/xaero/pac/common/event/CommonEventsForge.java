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

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.EntityMobGriefingEvent;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.*;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import xaero.pac.OpenPartiesAndClaims;

public class CommonEventsForge extends CommonEvents {

	public CommonEventsForge(OpenPartiesAndClaims modMain) {
		super(modMain);
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onEntityPlaceBlock(BlockEvent.EntityPlaceEvent event) {
		if(super.onEntityPlaceBlock(event.getWorld(), event.getPos(), event.getEntity()))
			event.setCanceled(true);
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onEntityMultiPlaceBlock(BlockEvent.EntityMultiPlaceEvent event) {
		if(super.onEntityMultiPlaceBlock(event.getWorld(), event.getReplacedBlockSnapshots().stream().map(BlockSnapshot::getPos), event.getEntity()))
			event.setCanceled(true);
	}

	@SubscribeEvent
	public void onServerAboutToStart(ServerAboutToStartEvent event) throws Throwable {
		super.onServerAboutToStart(event.getServer());
	}

	@SubscribeEvent
	public void onServerStarting(ServerStartingEvent event) {
		super.onServerStarting();
	}
	
	@SubscribeEvent
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		super.onPlayerRespawn(event.getPlayer());
	}
	
	@SubscribeEvent
	public void onPlayerChangedDimension(PlayerChangedDimensionEvent event) {
		super.onPlayerChangedDimension(event.getPlayer());
	}
	
	@SubscribeEvent
	public void onPlayerLogIn(PlayerLoggedInEvent event) {
		super.onPlayerLogIn(event.getPlayer());
	}

	@SubscribeEvent
	public void onPlayerClone(PlayerEvent.Clone event) {
		super.onPlayerClone(event.getOriginal(), event.getPlayer());
	}
	
	@SubscribeEvent
	public void onPlayerLogOut(PlayerLoggedOutEvent event) {
		super.onPlayerLogOut(event.getPlayer());
	}
	
	@SubscribeEvent
	public void onServerTick(ServerTickEvent event) throws Throwable {
		super.onServerTick(event.phase == Phase.START);
	}
	
	@SubscribeEvent
	public void onPlayerTick(PlayerTickEvent event) throws Throwable {
		super.onPlayerTick(event.phase == Phase.START, event.side == LogicalSide.SERVER, event.player);
	}
	
	@SubscribeEvent
	public void onServerStopped(ServerStoppedEvent event) {
		super.onServerStopped(event.getServer());
	}
	
	@SubscribeEvent
	public void onRegisterCommands(RegisterCommandsEvent event) {
		super.onRegisterCommands(event.getDispatcher(), event.getEnvironment());
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
		if(super.onLeftClickBlock(event.getSide() == LogicalSide.SERVER, event.getWorld(), event.getPos(), event.getPlayer()))
			event.setCanceled(true);
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onDestroyBlock(BlockEvent.BreakEvent event) {
		if(super.onDestroyBlock(event.getWorld(), event.getPos(), event.getPlayer()))
			event.setCanceled(true);
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		if(super.onRightClickBlock(event.getSide() == LogicalSide.SERVER, event.getWorld(), event.getPos(), event.getPlayer(), event.getHand(), event.getHitVec()))
			event.setCanceled(true);
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onItemRightClick(PlayerInteractEvent.RightClickItem event) {
		if(super.onItemRightClick(event.getSide() == LogicalSide.SERVER, event.getWorld(), event.getPos(), event.getPlayer(), event.getHand(), event.getItemStack()))
			event.setCanceled(true);
	}

	@SubscribeEvent
	public void onMobGrief(EntityMobGriefingEvent event) {
		if(super.onMobGrief(event.getEntity()))
			event.setResult(Result.DENY);
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onLivingHurt(LivingAttackEvent event) {
		if(super.onLivingHurt(event.getSource(), event.getEntity()))
			event.setCanceled(true);
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onEntityAttack(AttackEntityEvent event) {
		if(super.onEntityAttack(event.getPlayer(), event.getTarget()))
			event.setCanceled(true);
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
		if(super.onEntityInteract(event.getEntity(), event.getTarget(), event.getHand()))
			event.setCanceled(true);
	}
	
	@SubscribeEvent
	public void onExplosionDetonate(ExplosionEvent.Detonate event) {
		super.onExplosionDetonate(event.getWorld(), event.getExplosion(), event.getAffectedEntities(), event.getAffectedBlocks());
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onChorusFruit(EntityTeleportEvent.ChorusFruit event){
		if(super.onChorusFruit(event.getEntity(), event.getTarget()))
			event.setCanceled(true);
	}

	@SubscribeEvent
	public void onEntityJoinWorld(EntityJoinWorldEvent event){
		super.onEntityJoinWorld(event.getEntity(), event.getWorld());
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onEntityEnteringSection(EntityEvent.EnteringSection event){
		super.onEntityEnteringSection(event.getEntity(), event.getOldPos(), event.getNewPos(), event.didChunkChange());
	}

	@SubscribeEvent
	public void onPermissionsChanged(PermissionsChangedEvent event){
		if(event.getPlayer() instanceof ServerPlayer serverPlayer)
			super.onPermissionsChanged(serverPlayer);
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onCropTrample(BlockEvent.FarmlandTrampleEvent event) {
		if(super.onCropTrample(event.getEntity(), event.getPos()))
			event.setCanceled(true);
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onBucketUse(FillBucketEvent event){
		if(super.onBucketUse(event.getEntity(), event.getTarget(), event.getEmptyBucket()))
			event.setCanceled(true);
	}

	@SubscribeEvent
	public void onTagsUpdate(TagsUpdatedEvent event) {
		super.onTagsUpdate();
	}
}
