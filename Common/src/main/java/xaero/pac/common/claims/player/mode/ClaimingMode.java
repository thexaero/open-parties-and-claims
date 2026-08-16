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

package xaero.pac.common.claims.player.mode;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import xaero.pac.common.claims.player.mode.api.IClaimingModeAPI;
import xaero.pac.common.claims.result.api.ClaimResult;
import xaero.pac.common.server.claims.IServerClaimsManager;
import xaero.pac.common.server.player.config.IPlayerConfig;
import xaero.pac.common.server.player.config.api.PlayerConfigType;
import xaero.pac.common.server.player.config.api.v2.IPlayerConfigOptionSpecAPI;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public final class ClaimingMode implements IClaimingModeAPI {

	private final String id;
	private final BiFunction<UUID, IServerClaimsManager<?, ?, ?>, ClaimResult.Type> permissionChecker;
	private final BiFunction<UUID, IServerClaimsManager<?, ?, ?>, UUID> forcedUUIDGetter;
	private final UUID clientCountsSourceId;
	private final Function<IPlayerConfig, IPlayerConfig> claimConfigGetter;
	private final Component enableMessage;
	private final Component disableMessage;
	private final Predicate<CommandSourceStack> commandVisibilityRequirement;
	private final IPlayerConfigOptionSpecAPI<String> subClaimOption;
	private final PlayerConfigType configType;
	private final BiFunction<ServerPlayer, IServerClaimsManager<?, ?, ?>, ClaimingModeLimits> limitsBuilder;
	private final Component activeLabel;
	private final boolean canBeImpersonated;

	private ClaimingMode(
			String id,
			BiFunction<UUID, IServerClaimsManager<?, ?, ?>, ClaimResult.Type> permissionChecker,
			BiFunction<UUID, IServerClaimsManager<?, ?, ?>, UUID> forcedUUIDGetter,
			UUID clientCountsSourceId,
			Function<IPlayerConfig, IPlayerConfig> claimConfigGetter,
			Component enableMessage,
			Component disableMessage,
			Predicate<CommandSourceStack> commandVisibilityRequirement,
			IPlayerConfigOptionSpecAPI<String> subClaimOption,
			PlayerConfigType configType,
			BiFunction<ServerPlayer, IServerClaimsManager<?, ?, ?>, ClaimingModeLimits> limitsBuilder,
			Component activeLabel,
			boolean canBeImpersonated
	) {
		this.id = id;
		this.permissionChecker = permissionChecker;
		this.forcedUUIDGetter = forcedUUIDGetter;
		this.clientCountsSourceId = clientCountsSourceId;
		this.claimConfigGetter = claimConfigGetter;
		this.enableMessage = enableMessage;
		this.disableMessage = disableMessage;
		this.commandVisibilityRequirement = commandVisibilityRequirement;
		this.subClaimOption = subClaimOption;
		this.configType = configType;
		this.limitsBuilder = limitsBuilder;
		this.activeLabel = activeLabel;
		this.canBeImpersonated = canBeImpersonated;
	}

	@Override
	@Nonnull
	public String getId() {
		return id;
	}

	public BiFunction<UUID, IServerClaimsManager<?, ?, ?>, UUID> getForcedUUIDGetter() {
		return forcedUUIDGetter;
	}

	public BiFunction<UUID, IServerClaimsManager<?, ?, ?>, ClaimResult.Type> getPermissionChecker() {
		return permissionChecker;
	}

	public Function<IPlayerConfig, IPlayerConfig> getClaimConfigGetter() {
		return claimConfigGetter;
	}

	public Component getEnableMessage() {
		return enableMessage;
	}

	public Component getDisableMessage() {
		return disableMessage;
	}

	public Predicate<CommandSourceStack> getCommandVisibilityRequirement() {
		return commandVisibilityRequirement;
	}

	public IPlayerConfigOptionSpecAPI<String> getSubClaimOption() {
		return subClaimOption;
	}

	public PlayerConfigType getConfigType() {
		return configType;
	}

	public UUID getClientCountsSourceId() {
		return clientCountsSourceId;
	}

	public BiFunction<ServerPlayer, IServerClaimsManager<?, ?, ?>, ClaimingModeLimits> getLimitsBuilder() {
		return limitsBuilder;
	}

	public Component getActiveLabel() {
		return activeLabel;
	}

	@Override
	public boolean isGlobal(){
		return configType.isGlobal();
	}

	@Override
	public boolean canBeImpersonated() {
		return canBeImpersonated;
	}

	public static final class Builder {

		private String id;
		private BiFunction<UUID, IServerClaimsManager<?, ?, ?>, ClaimResult.Type> permissionChecker;
		private BiFunction<UUID, IServerClaimsManager<?, ?, ?>, UUID> forcedUUIDGetter;
		private UUID clientCountsSourceId;
		private Function<IPlayerConfig, IPlayerConfig> claimConfigGetter;
		private Component enableMessage;
		private Component disableMessage;
		private Predicate<CommandSourceStack> commandVisibilityRequirement;
		private IPlayerConfigOptionSpecAPI<String> subClaimOption;
		private PlayerConfigType configType;
		private BiFunction<ServerPlayer, IServerClaimsManager<?, ?, ?>, ClaimingModeLimits> limitsBuilder;
		private Component activeLabel;
		private boolean canBeImpersonated;

		private Builder(){}

		public Builder setDefault(){
			setId(null);
			setPermissionChecker(null);
			setForcedUUIDGetter(null);
			setClaimConfigGetter(null);
			setEnableMessage(null);
			setDisableMessage(null);
			setCommandVisibilityRequirement(null);
			setConfigType(null);
			setClientCountsSourceId(null);
			setLimitsBuilder(null);
			setActiveLabel(null);
			setCanBeImpersonated(false);
			return this;
		}

		public Builder setId(String id){
			this.id = id;
			return this;
		}

		public Builder setPermissionChecker(
				BiFunction<UUID, IServerClaimsManager<?, ?, ?>, ClaimResult.Type> permissionChecker
		) {
			this.permissionChecker = permissionChecker;
			return this;
		}

		public Builder setForcedUUIDGetter(
				BiFunction<UUID, IServerClaimsManager<?, ?, ?>, UUID> forcedUUIDGetter
		) {
			this.forcedUUIDGetter = forcedUUIDGetter;
			return this;
		}

		public Builder setClientCountsSourceId(UUID clientCountsSourceId) {
			this.clientCountsSourceId = clientCountsSourceId;
			return this;
		}

		public Builder setClaimConfigGetter(Function<IPlayerConfig, IPlayerConfig> claimConfigGetter) {
			this.claimConfigGetter = claimConfigGetter;
			return this;
		}

		public Builder setEnableMessage(Component enableMessage) {
			this.enableMessage = enableMessage;
			return this;
		}

		public Builder setDisableMessage(Component disableMessage) {
			this.disableMessage = disableMessage;
			return this;
		}

		public Builder setCommandVisibilityRequirement(
				Predicate<CommandSourceStack> commandVisibilityRequirement
		) {
			this.commandVisibilityRequirement = commandVisibilityRequirement;
			return this;
		}

		public Builder setSubClaimOption(IPlayerConfigOptionSpecAPI<String> subClaimOption) {
			this.subClaimOption = subClaimOption;
			return this;
		}

		public Builder setConfigType(PlayerConfigType configType) {
			this.configType = configType;
			return this;
		}

		public Builder setLimitsBuilder(BiFunction<ServerPlayer, IServerClaimsManager<?, ?, ?>, ClaimingModeLimits> limitsBuilder) {
			this.limitsBuilder = limitsBuilder;
			return this;
		}

		public Builder setActiveLabel(Component activeLabel) {
			this.activeLabel = activeLabel;
			return this;
		}

		public Builder setCanBeImpersonated(boolean canBeImpersonated) {
			this.canBeImpersonated = canBeImpersonated;
			return this;
		}

		public ClaimingMode build(Map<String, IClaimingModeAPI> dest){
			if(id == null || claimConfigGetter == null || enableMessage == null ||
					disableMessage == null || commandVisibilityRequirement == null ||
					subClaimOption == null || configType == null || limitsBuilder == null ||
					activeLabel == null
			)
				throw new IllegalStateException();
			if(configType.isGlobal() && canBeImpersonated)
				throw new IllegalStateException("There is no point in impersonating a global claiming mode!");
			ClaimingMode result = new ClaimingMode(
					id, permissionChecker, forcedUUIDGetter, clientCountsSourceId,
					claimConfigGetter, enableMessage,
					disableMessage, commandVisibilityRequirement, subClaimOption,
					configType, limitsBuilder, activeLabel, canBeImpersonated
			);
			if(dest != null)
				dest.put(id, result);
			return result;
		}

		public static Builder begin(){
			return new Builder().setDefault();
		}

	}

}
