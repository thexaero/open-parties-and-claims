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

package xaero.pac.common.server.claims.player.io;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.storage.LevelResource;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.claims.player.PlayerDimensionClaims;
import xaero.pac.common.server.claims.ServerClaimsManager;
import xaero.pac.common.server.claims.player.ServerPlayerClaimInfo;
import xaero.pac.common.server.claims.player.ServerPlayerClaimInfoManager;
import xaero.pac.common.server.config.ServerConfig;
import xaero.pac.common.server.io.FileIOHelper;
import xaero.pac.common.server.io.IOThreadWorker;
import xaero.pac.common.server.io.ObjectManagerIO;
import xaero.pac.common.server.io.ObjectManagerIOObject;
import xaero.pac.common.server.io.serialization.SerializationHandler;
import xaero.pac.common.server.io.serialization.SerializedDataFileIO;
import xaero.pac.common.server.player.config.PlayerConfig;

import java.nio.file.Path;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiConsumer;

public final class PlayerClaimInfoManagerIO<S>
 extends ObjectManagerIO<S, UUID, ServerPlayerClaimInfo, ServerPlayerClaimInfoManager, PlayerClaimInfoManagerIO<S>> {

	private final ServerClaimsManager serverClaimsManager;
	private final Path claimsFolderPath;
	
	private PlayerClaimInfoManagerIO(String extension, SerializationHandler<S, UUID, ServerPlayerClaimInfo, ServerPlayerClaimInfoManager> serializationHandler, SerializedDataFileIO<S, UUID> serializedDataFileIO, IOThreadWorker ioThreadWorker, MinecraftServer server, ServerClaimsManager serverClaimsManager, ServerPlayerClaimInfoManager claimsManager, FileIOHelper fileIOHelper, Path claimsFolderPath) {
		super(serializationHandler, serializedDataFileIO, ioThreadWorker, server, extension, claimsManager, fileIOHelper);
		this.serverClaimsManager = serverClaimsManager;
		this.claimsFolderPath = claimsFolderPath;
	}

	@Override
	protected Path getObjectFolderPath() {
		return claimsFolderPath;
	}
	
	@Override
	public void load() {
		if(!ServerConfig.CONFIG.claimsEnabled.get())
			return;
		long before = System.currentTimeMillis(); 
		OpenPartiesAndClaims.LOGGER.info("Loading claims...");
		super.load();
		OpenPartiesAndClaims.LOGGER.info("Loaded claims in " + (System.currentTimeMillis() - before) + "ms");
		manager.onLoad();
		serverClaimsManager.onLoad();
	}

	@Override
	public boolean save() {
		if(!ServerConfig.CONFIG.claimsEnabled.get())
			return true;
		OpenPartiesAndClaims.LOGGER.info("Saving claims!");
		return super.save();
	}

	@Override
	public void delete(ObjectManagerIOObject object) {
		if(!ServerConfig.CONFIG.claimsEnabled.get())
			return;
		super.delete(object);
	}

	@Override
	public void onServerTick() {
		super.onServerTick();
	}

	@Override
	protected UUID getObjectId(String fileNameNoExtension, Path file) {
		return UUID.fromString(fileNameNoExtension);
	}

	@Override
	protected void onObjectLoad(ServerPlayerClaimInfo loadedObject) {
		//the result is not stored anywhere directly but the info in it is forwarded to the claims manager, which updates the player claims manager
		if(!Objects.equals(loadedObject.getPlayerId(), PlayerConfig.EXPIRED_CLAIM_UUID) && !Objects.equals(loadedObject.getPlayerId(), PlayerConfig.SERVER_CLAIM_UUID))
			serverClaimsManager.getPlayerInfo(loadedObject.getPlayerId()).setPlayerUsername(loadedObject.getPlayerUsername());
		loadedObject.getStream().forEach(
				e -> {
					ResourceLocation dim = e.getKey();
					PlayerDimensionClaims dimensionClaims = e.getValue();
					BiConsumer<Boolean, ChunkPos> claimConsumer = (fl, pos) -> {
						serverClaimsManager.claim(dim, loadedObject.getPlayerId(), pos.x, 
								pos.z, fl);
					};
					dimensionClaims.getStream().forEach(posList -> {
						boolean forceloadable = posList.getClaimState().isForceloadable();
						posList.getStream().forEach(pos -> claimConsumer.accept(forceloadable, pos));
					});
				}
				);
	}
	
	public static final class Builder<S> extends ObjectManagerIO.Builder<S, UUID, ServerPlayerClaimInfo, ServerPlayerClaimInfoManager, PlayerClaimInfoManagerIO<S>>{

		private ServerClaimsManager serverClaimsManager;
		
		private Builder() {
		}
		
		@Override
		public Builder<S> setDefault() {
			super.setDefault();
			setServerClaimsManager(null);
			return this;
		}
		
		public Builder<S> setServerClaimsManager(ServerClaimsManager serverClaimsManager) {
			this.serverClaimsManager = serverClaimsManager;
			return this;
		}
		
		@Override
		public PlayerClaimInfoManagerIO<S> build() {
			if(serverClaimsManager == null)
				throw new IllegalStateException();
			setManager(serverClaimsManager.getPlayerClaimInfoManager());
			return super.build();
		}

		public PlayerClaimInfoManagerIO<S> buildInternally() {
			Path claimsFolderPath = server.getWorldPath(LevelResource.ROOT).resolve("data").resolve(OpenPartiesAndClaims.MOD_ID).resolve("player-claims");
			return new PlayerClaimInfoManagerIO<>(fileExtension, serializationHandler, serializedDataFileIO, ioThreadWorker, server, serverClaimsManager, manager, fileIOHelper, claimsFolderPath);
		}

		public static <S>Builder<S> begin() {
			return new Builder<S>().setDefault();
		}

	}

}
