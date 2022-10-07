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

package xaero.pac.common.packet.config;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.client.gui.OtherPlayerConfigWaitScreen;
import xaero.pac.client.player.config.IPlayerConfigClientStorage;
import xaero.pac.client.player.config.IPlayerConfigClientStorageManager;
import xaero.pac.client.player.config.IPlayerConfigStringableOptionClientStorage;
import xaero.pac.client.player.config.PlayerConfigOptionClientStorage;
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.claims.player.IPlayerClaimPosList;
import xaero.pac.common.claims.player.IPlayerDimensionClaims;
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
import xaero.pac.common.server.player.config.IPlayerConfig;
import xaero.pac.common.server.player.config.IPlayerConfigManager;
import xaero.pac.common.server.player.config.PlayerConfigOptionSpec;
import xaero.pac.common.server.player.config.api.IPlayerConfigAPI.SetResult;
import xaero.pac.common.server.player.config.api.IPlayerConfigOptionSpecAPI;
import xaero.pac.common.server.player.config.api.PlayerConfigOptions;
import xaero.pac.common.server.player.config.api.PlayerConfigType;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

public class PlayerConfigOptionValuePacket extends PlayerConfigPacket {
	
	private final PlayerConfigType type;
	private final String subId;
	private final UUID owner;
	private final List<PlayerConfigOptionClientStorage<?>> entries;

	public PlayerConfigOptionValuePacket(PlayerConfigType type, String subId, UUID owner, List<PlayerConfigOptionClientStorage<?>> entries) {
		super();
		this.type = type;
		this.subId = subId;
		this.owner = owner;
		this.entries = entries;
	}
	
	public PlayerConfigType getType() {
		return type;
	}
	
	public UUID getOwner() {
		return owner;
	}
	
	public Stream<PlayerConfigOptionClientStorage<?>> entryStream(){
		return entries.stream();
	}
	
	public int getSize() {
		return entries.size();
	}

	public String getSubId() {
		return subId;
	}

	public static class Codec implements BiConsumer<PlayerConfigOptionValuePacket, FriendlyByteBuf>, Function<FriendlyByteBuf, PlayerConfigOptionValuePacket> {
		
		@Override
		public PlayerConfigOptionValuePacket apply(FriendlyByteBuf input) {
			try {
				CompoundTag nbt = input.readNbt(new NbtAccounter(524288));
				if(nbt == null)
					return null;
				String typeString = nbt.getString("t");
				if(typeString.length() > 100) {
					OpenPartiesAndClaims.LOGGER.info("Player config type string is too long!");
					return null;
				}
				PlayerConfigType type = null;
				try {
					type = PlayerConfigType.valueOf(typeString);
				} catch(IllegalArgumentException iae) {
				}
				if(type == null) {
					OpenPartiesAndClaims.LOGGER.info("Received unknown player config type!");
					return null;
				}
				String subID = nbt.contains("si") ? nbt.getString("si") : null;
				if(subID != null && subID.length() > 100) {
					OpenPartiesAndClaims.LOGGER.info("Player config sub ID string is too long!");
					return null;
				}
				UUID owner = type != PlayerConfigType.PLAYER || nbt.getBoolean("co") ? null : nbt.getUUID("o");
				ListTag entryListTag = nbt.getList("e", Tag.TAG_COMPOUND);
				if(entryListTag.size() < 0 || entryListTag.size() > 512) {//there are other max size checks when reading the nbt tag, but an extra one here won't hurt
					OpenPartiesAndClaims.LOGGER.info("Received an illegal player config option entry number: " + entryListTag.size());
					return null;
				}
				List<PlayerConfigOptionClientStorage<?>> entries = new ArrayList<>(entryListTag.size());
				String warningToOutput = null;
				for(Tag e : entryListTag) {
					CompoundTag entryTag = (CompoundTag) e;
					String optionId = entryTag.getString("i");
					if(optionId.length() > 1000) {
						OpenPartiesAndClaims.LOGGER.info("Received player config option id string is not allowed!");
						return null;
					}
					PlayerConfigOptionSpec<?> option = (PlayerConfigOptionSpec<?>) PlayerConfigOptions.OPTIONS.get(optionId);
					Object value;
					if(option == null) {
						if(warningToOutput == null)
							warningToOutput = "Received unknown player config option id: " + optionId;
						continue;
					} else {
						if(!entryTag.contains("v"))
							value = null;
						else if(option.getType() == Boolean.class)
							value = entryTag.getBoolean("v");
						else if(option.getType() == Integer.class)
							value = entryTag.getInt("v");
						else if(option.getType() == Double.class)
							value = entryTag.getDouble("v");
						else if(option.getType() == Float.class)
							value = entryTag.getFloat("v");
						else if(option.getType() == String.class) {
							value = entryTag.getString("v");
							if(((String)value).length() > 1000) {
								OpenPartiesAndClaims.LOGGER.info("Received a string option value that is too long: " + ((String)value).length());
								return null;
							}
						} else {
							if(warningToOutput == null)
								warningToOutput = "Received unknown player config option type: " + option.getType();
							continue;
						}
					}
					boolean mutable = entryTag.getBoolean("m");
					boolean defaulted = entryTag.getBoolean("d");
					PlayerConfigOptionClientStorage<?> entry = PlayerConfigOptionClientStorage.createCast(option, value);
					entry.setMutable(mutable);
					entry.setDefaulted(defaulted);
					entries.add(entry);
					
				}
				if(warningToOutput != null)
					OpenPartiesAndClaims.LOGGER.info(warningToOutput);
				return new PlayerConfigOptionValuePacket(type, subID, owner, entries);
			} catch(Throwable t) {
				return null;
			}
		}

		@Override
		public void accept(PlayerConfigOptionValuePacket t, FriendlyByteBuf u) {
			CompoundTag nbt = new CompoundTag();
			nbt.putString("t", t.getType().name());
			if(t.subId != null)
				nbt.putString("si", t.subId);
			if(t.getType() == PlayerConfigType.PLAYER) {
				nbt.putBoolean("co", t.owner == null);
				if(t.owner != null)
					nbt.putUUID("o", t.owner);
			}
			
			ListTag entryListTag = new ListTag();
			
			for(PlayerConfigOptionClientStorage<?> entry : t.entries) {
				CompoundTag entryTag = new CompoundTag();
				PlayerConfigOptionSpec<?> entryOption = entry.getOption();
				Object entryValue = entry.getValue();
				entryTag.putString("i", entryOption.getId());
				if(entryValue != null) {
					if (entryOption.getType() == Boolean.class)
						entryTag.putBoolean("v", (boolean) entryValue);
					else if (entryOption.getType() == Integer.class)
						entryTag.putInt("v", (int) entryValue);
					else if (entryOption.getType() == Double.class)
						entryTag.putDouble("v", (double) entryValue);
					else if (entryOption.getType() == Float.class)
						entryTag.putFloat("v", (float) entryValue);
					else if (entryOption.getType() == String.class)
						entryTag.putString("v", (String) entryValue);
					else
						OpenPartiesAndClaims.LOGGER.info("Sending an unknown player config option type: " + entryOption.getType());
				}
				entryTag.putBoolean("m", entry.isMutable());
				entryTag.putBoolean("d", entry.isDefaulted());
				
				entryListTag.add(entryTag);
			}
			
			nbt.put("e", entryListTag);
			u.writeNbt(nbt);
		}
		
	}

	public static class ClientHandler implements Consumer<PlayerConfigOptionValuePacket> {

		@Override
		public void accept(PlayerConfigOptionValuePacket t) {
			IPlayerConfigClientStorageManager<IPlayerConfigClientStorage<IPlayerConfigStringableOptionClientStorage<?>>>
					playerConfigStorageManager = OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getPlayerConfigStorageManager();

			IPlayerConfigClientStorage<IPlayerConfigStringableOptionClientStorage<?>> storage = null;
			boolean isForOtherPlayer = false;
			if(t.getType() == PlayerConfigType.PLAYER) {
				isForOtherPlayer = t.getOwner() != null;
				if(isForOtherPlayer) {
					if(Minecraft.getInstance().screen != null && Minecraft.getInstance().screen instanceof OtherPlayerConfigWaitScreen) {
						if(t.subId == null) {
							IPlayerConfigClientStorage<IPlayerConfigStringableOptionClientStorage<?>> prevOtherStorage = playerConfigStorageManager.getOtherPlayerConfig();
							storage = playerConfigStorageManager.beginConfigStorageBuild(LinkedHashMap::new).setType(PlayerConfigType.PLAYER).setOwner(t.owner).build();
							if(prevOtherStorage != null && t.getOwner().equals(prevOtherStorage.getOwner()))
								storage.setSelectedSubConfig(prevOtherStorage.getSelectedSubConfig());
							playerConfigStorageManager.setOtherPlayerConfig(storage);
						} else
							storage = playerConfigStorageManager.getOtherPlayerConfig();
					}
				} else
					storage = playerConfigStorageManager.getMyPlayerConfig();
			} else
				storage =
						t.getType() == PlayerConfigType.SERVER ? playerConfigStorageManager.getServerClaimsConfig() :
								t.getType() == PlayerConfigType.EXPIRED ? playerConfigStorageManager.getExpiredClaimsConfig() :
										t.getType() == PlayerConfigType.WILDERNESS ? playerConfigStorageManager.getWildernessConfig() :
												playerConfigStorageManager.getDefaultPlayerConfig();
			if(storage != null) {
				if(t.subId != null)
					storage = storage.getOrCreateSubConfig(t.subId);
				final IPlayerConfigClientStorage<IPlayerConfigStringableOptionClientStorage<?>> forwardedStorage = storage;
				t.entryStream().forEach(entry -> {
					IPlayerConfigStringableOptionClientStorage<?> optionStorage = forwardedStorage.getOptionStorage(entry.getOption());
					optionStorage.setCastValue(entry.getValue());
					optionStorage.setMutable(entry.isMutable());
					optionStorage.setDefaulted(entry.isDefaulted());
				});
			}
		}

	}

	public static class ServerHandler implements BiConsumer<PlayerConfigOptionValuePacket, ServerPlayer> {

		@SuppressWarnings("unchecked")
		private <T extends Comparable<T>> SetResult setConfigUnchecked(IPlayerConfig config, IPlayerConfigOptionSpecAPI<T> option, Object value) {
			return config.tryToSet(option, (T) value);
		}

		@Override
		public void accept(PlayerConfigOptionValuePacket t, ServerPlayer serverPlayer) {
			if(t.entries.size() > 1) {
				OpenPartiesAndClaims.LOGGER.info("A player is attempting to modify multiple options in a single packet! Name: " + serverPlayer.getGameProfile().getName());
				return;
			}
			boolean isOP = serverPlayer.hasPermissions(2);
			PlayerConfigOptionClientStorage<?> optionEntry = t.entries.get(0);
			UUID ownerId = t.getType() != PlayerConfigType.PLAYER ? null : t.owner == null ? serverPlayer.getUUID() : t.owner;
			if(!isOP) {
				if(t.getType() != PlayerConfigType.PLAYER) {
					OpenPartiesAndClaims.LOGGER.info("Non-op player is attempting to modify a config without required permissions! Name: " + serverPlayer.getGameProfile().getName());
					return;
				}
				if(ServerConfig.CONFIG.opConfigurablePlayerConfigOptions.get().contains(optionEntry.getOption().getId())) {
					OpenPartiesAndClaims.LOGGER.info("Non-op player is attempting to modify a op-only option! Name: " + serverPlayer.getGameProfile().getName());
					return;
				}
				if(!Objects.equals(ownerId, serverPlayer.getUUID())) {
					OpenPartiesAndClaims.LOGGER.info("Non-op player is attempting to modify another player's config! Name: " + serverPlayer.getGameProfile().getName());
					return;
				}
			}
			IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(serverPlayer.getServer());
			IPlayerConfigManager playerConfigs = serverData.getPlayerConfigs();
			IPlayerConfig config =
					t.getType() == PlayerConfigType.PLAYER ?
							playerConfigs.getLoadedConfig(ownerId) :
							t.getType() == PlayerConfigType.SERVER ?
									playerConfigs.getServerClaimConfig() :
									t.getType() == PlayerConfigType.EXPIRED ?
											playerConfigs.getExpiredClaimConfig() :
											t.getType() == PlayerConfigType.WILDERNESS ?
													playerConfigs.getWildernessConfig() :
													playerConfigs.getDefaultConfig();
			if(t.subId != null)
				config = config.getSubConfig(t.subId);
			if(config != null) {
				SetResult result = setConfigUnchecked(config, optionEntry.getOption(), optionEntry.getValue());
				if (result != SetResult.SUCCESS && (config.getType() != PlayerConfigType.PLAYER || serverPlayer.getUUID().equals(config.getPlayerId())))
					playerConfigs.getSynchronizer().syncOptionToClient(serverPlayer, config, optionEntry.getOption());//restore the correct value
			}
		}
	}
	
}
