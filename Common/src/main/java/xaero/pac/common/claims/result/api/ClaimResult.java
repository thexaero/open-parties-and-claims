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

package xaero.pac.common.claims.result.api;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import xaero.pac.common.claims.player.api.IPlayerChunkClaimAPI;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A claim result for a single chunk
 */
public class ClaimResult<C extends IPlayerChunkClaimAPI> {
	
	private final C claimResult;
	private final Type resultType;
	private final Component customReason;

	/**
	 * A constructor for internal usage
	 *
	 * @param claimResult  the claim state where relevant
	 * @param resultType  the result type
	 */
	public ClaimResult(@Nullable C claimResult, @Nonnull Type resultType) {
		this(claimResult, resultType, null);
	}

	/**
	 * A constructor for internal usage
	 *
	 * @param claimResult  the claim state where relevant
	 * @param resultType  the result type
	 * @param customReason  the custom reason given by an addon for this result, can be null
	 */
	public ClaimResult(@Nullable C claimResult, @Nonnull Type resultType, @Nullable Component customReason) {
		super();
		this.claimResult = claimResult;
		this.resultType = resultType;
		this.customReason = customReason;
	}

	/**
	 * Gets the claim state associated with this result where relevant.
	 *
	 * @return the claim state for this result, can be null
	 */
	@Nullable
	public C getClaimResult() {
		return claimResult;
	}

	/**
	 * Gets the {@link ClaimResult.Type} of this result.
	 *
	 * @return the type of this result, not null
	 */
	@Nonnull
	public Type getResultType() {
		return resultType;
	}

	/**
	 * Gets the text Component with the custom reason given by an addon for this result.
	 *
	 * @return the custom reason Component, null if there isn't one
	 */
	@Nullable
	public Component getCustomReason() {
		return customReason;
	}

	/**
	 * Gets the display message for this result, applying the custom reason given by an addon when it exists.
	 *
	 * @return the display message for this result
	 */
	@Nonnull
	public Component getMessage(){
		if(customReason == null)
			return resultType.message;
		if(resultType.interruptsAreaAction)
			return Component.translatable("gui.xaero_claims_claim_action_interruptingly_forbidden_by_addon_reason", customReason);
		return Component.translatable("gui.xaero_claims_claim_action_forbidden_by_addon_reason", customReason);
	}

	/**
	 * All types of claim action results
	 */
	public static enum Type {

		/** A chunk was already forceloadable */
		ALREADY_FORCELOADABLE(Component.translatable("gui.xaero_claims_forceload_already"), false, false, false),

		/** A chunk was already not forceloadable */
		ALREADY_UNFORCELOADED(Component.translatable("gui.xaero_claims_unforceload_already"), false, false, false),

		/** The claims feature is disabled */
		CLAIMS_ARE_DISABLED(Component.translatable("gui.xaero_claims_are_disabled").withStyle(ChatFormatting.RED), false, true, false),

		/** The area for a claim action was too big */
		TOO_MANY_CHUNKS(Component.translatable("gui.xaero_claims_too_many_chunks").withStyle(ChatFormatting.RED), false, true, false),

		/** The dimension is unclaimable */
		UNCLAIMABLE_DIMENSION(Component.translatable("gui.xaero_claims_claim_dimension_unclaimable").withStyle(ChatFormatting.RED), false, true, false),

		/** The claiming player is not part of the player group that the Wilderness "Reclaimable By" option is set to */
		DIMENSION_NOT_RECLAIMABLE(Component.translatable("gui.xaero_claims_claim_dimension_not_reclaimable").withStyle(ChatFormatting.RED), false, true, false),

		/** The chunk isn't claimed by who is trying to (un)forceload it */
		NOT_CLAIMED_BY_USER_FORCELOAD(Component.translatable("gui.xaero_claims_forceload_not_yours").withStyle(ChatFormatting.RED), false, true, false),

		/** The chunk isn't claimed by who is trying to unclaim it */
		NOT_CLAIMED_BY_USER(Component.translatable("gui.xaero_claims_claim_unclaim_not_yours").withStyle(ChatFormatting.RED), false, true, false),

		/** The chunk is not claimed */
		NOT_CLAIMED(Component.translatable("gui.xaero_claims_claim_unclaim_not_claimed").withStyle(ChatFormatting.RED), false, true, false),

		/** The chunk is already claimed */
		ALREADY_CLAIMED(Component.translatable("gui.xaero_claims_claim_already_claimed").withStyle(ChatFormatting.RED), false, true, false),

		/** The maximum number of forceloadable claims was reached */
		FORCELOAD_LIMIT_REACHED(Component.translatable("gui.xaero_claims_forceload_limit_reached").withStyle(ChatFormatting.RED), false, true, true),

		/** The maximum number of claims was reached */
		CLAIM_LIMIT_REACHED(Component.translatable("gui.xaero_claims_claim_limit_reached").withStyle(ChatFormatting.RED), false, true, true),

		/** The player's claim count is even higher than their limit */
		OVER_CLAIM_LIMIT(Component.translatable("gui.xaero_claims_over_claim_limit").withStyle(ChatFormatting.RED), false, true, true),

		/** The chunk was beyond the maximum distance */
		TOO_FAR(Component.translatable("gui.xaero_claims_claim_not_within_distance").withStyle(ChatFormatting.RED), false, true, false),

		/** The chunk was beyond the maximum distance */
		ANOTHER_DIMENSION(Component.translatable("gui.xaero_claims_claim_not_within_dimension").withStyle(ChatFormatting.RED), false, true, false),

		/** There is a claim replacement currently in progress in the background */
		REPLACEMENT_IN_PROGRESS(Component.translatable("gui.xaero_claims_replacement_in_progress").withStyle(ChatFormatting.RED), false, true, true),

		/** There is a claim transfer currently in progress in the background */
		TRANSFER_IN_PROGRESS(Component.translatable("gui.xaero_claims_transfer_in_progress").withStyle(ChatFormatting.RED), false, true, true),

		/** There is an area claim action task in progress in the background */
		AREA_ACTION_IN_PROGRESS(Component.translatable("gui.xaero_claims_area_action_task_in_progress").withStyle(ChatFormatting.RED), false, true, true),

		/**
		 * The user doesn't have permission to make server claims
		 * <p>
		 * This result type is only used for server claim requests made by online players.
		 * Permissions for server claims are not checked by the try methods in the server claims manager.
		 */
		NO_SERVER_PERMISSION(Component.translatable("gui.xaero_claims_claim_no_server_permission").withStyle(ChatFormatting.RED), false, true, false),

		/**
		 * The user doesn't have permission to make party claims
		 * <p>
		 * This result type is only used for party claim requests made by online players.
		 * Permissions for party claims are not checked by the try methods in the server claims manager.
		 */
		NO_PARTY_PERMISSION(Component.translatable("gui.xaero_claims_claim_no_party_permission").withStyle(ChatFormatting.RED), false, true, false),

		/**
		 * The user doesn't have permission to make party claims
		 * <p>
		 * This result type is only used for party claim requests made by online players.
		 * Permissions for party claims are not checked by the try methods in the server claims manager.
		 */
		NOT_IN_PARTY(Component.translatable("gui.xaero_claims_claim_not_in_party").withStyle(ChatFormatting.RED), false, true, false),

		/** The claiming action was interrupted from the outside */
		INTERRUPTED(Component.translatable("gui.xaero_claims_area_claim_action_interrupted").withStyle(ChatFormatting.RED), false, true, true),

		/** The claiming action was forbidden by an addon */
		ADDON_FORBIDS(Component.translatable("gui.xaero_claims_claim_action_forbidden_by_addon").withStyle(ChatFormatting.RED), false, true, false),

		/** The claiming action was interruptingly forbidden by an addon */
		ADDON_INTERRUPTS(Component.translatable("gui.xaero_claims_claim_action_interruptingly_forbidden_by_addon").withStyle(ChatFormatting.RED), false, true, true),

		/** The claiming action was interruptingly forbidden by an addon */
		CANT_TOUCH_OTHER(Component.translatable("gui.xaero_claims_claim_cant_touch_other_claims").withStyle(ChatFormatting.RED), false, true, false),

		/** Successfully unforceloaded a chunk */
		SUCCESSFUL_UNFORCELOAD(Component.translatable("gui.xaero_claims_unforceloaded"), true, false, false),

		/** Successfully unclaimed a chunk */
		SUCCESSFUL_UNCLAIM(Component.translatable("gui.xaero_claims_unclaimed"), true, false, false),

		/** Successfully forceloaded a chunk */
		SUCCESSFUL_FORCELOAD(Component.translatable("gui.xaero_claims_forceloaded"), true, false, false),

		/** Successfully claimed a chunk */
		SUCCESSFUL_CLAIM(Component.translatable("gui.xaero_claims_claimed"), true, false, false);

		/**
		 * A message describing the result
		 */
		@Nonnull
		public final Component message;

		/**
		 * Whether the result can be considered a success
		 */
		public final boolean success;

		/**
		 * Whether the result can be considered a failure
		 */
		public final boolean fail;

		/**
		 * Whether this result should stop an area claim action from continuing
		 */
		public final boolean interruptsAreaAction;
		
		private Type(Component message, boolean success, boolean fail, boolean interruptsAreaAction) {
			this.message = message;
			this.success = success;
			this.fail = fail;
			this.interruptsAreaAction = interruptsAreaAction;
		}
		
	}

}
