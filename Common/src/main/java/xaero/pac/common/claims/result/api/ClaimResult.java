/*
 *     Open Parties and Claims - adds chunk claims and player parties to Minecraft
 *     Copyright (C) 2022, Xaero <xaero1996@gmail.com> and contributors
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of version 3 of the GNU Lesser General Public License
 *     (LGPL-3.0-only) as published by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received copies of the GNU Lesser General Public License
 *     and the GNU General Public License along with this program.
 *     If not, see <https://www.gnu.org/licenses/>.
 */

package xaero.pac.common.claims.result.api;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import xaero.pac.common.claims.player.api.IPlayerChunkClaimAPI;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A claim result for a single chunk
 */
public class ClaimResult<C extends IPlayerChunkClaimAPI> {
	
	private final C claimResult;
	private final Type resultType;

	/**
	 * A constructor for internal usage
	 *
	 * @param claimResult  the claim state where relevant
	 * @param resultType  the result type
	 */
	public ClaimResult(@Nullable C claimResult, @Nonnull Type resultType) {
		super();
		this.claimResult = claimResult;
		this.resultType = resultType;
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
	 * All types of claim action results
	 */
	public static enum Type {

		/** A chunk was already forceloadable */
		ALREADY_FORCELOADABLE(new TranslatableComponent("gui.xaero_claims_forceload_already"), false, false),

		/** A chunk was already not forceloadable */
		ALREADY_UNFORCELOADED(new TranslatableComponent("gui.xaero_claims_unforceload_already"), false, false),

		/** The claims feature is disabled */
		CLAIMS_ARE_DISABLED(new TranslatableComponent("gui.xaero_claims_are_disabled").withStyle(ChatFormatting.RED), false, true),

		/** The area for a claim action was too big */
		TOO_MANY_CHUNKS(new TranslatableComponent("gui.xaero_claims_too_many_chunks").withStyle(ChatFormatting.RED), false, true),

		/** The dimension is unclaimable */
		UNCLAIMABLE_DIMENSION(new TranslatableComponent("gui.xaero_claims_claim_dimension_unclaimable").withStyle(ChatFormatting.RED), false, true),

		/** The chunk isn't claimed by who is trying to (un)forceload it */
		NOT_CLAIMED_BY_USER_FORCELOAD(new TranslatableComponent("gui.xaero_claims_forceload_not_yours").withStyle(ChatFormatting.RED), false, true),

		/** The chunk isn't claimed by who is trying to unclaim it */
		NOT_CLAIMED_BY_USER(new TranslatableComponent("gui.xaero_claims_claim_unclaim_not_yours").withStyle(ChatFormatting.RED), false, true),

		/** The chunk is already claimed */
		ALREADY_CLAIMED(new TranslatableComponent("gui.xaero_claims_claim_already_claimed").withStyle(ChatFormatting.RED), false, true),

		/** The maximum number of forceloadable claims was reached */
		FORCELOAD_LIMIT_REACHED(new TranslatableComponent("gui.xaero_claims_forceload_limit_reached").withStyle(ChatFormatting.RED), false, true),

		/** The maximum number of claims was reached */
		CLAIM_LIMIT_REACHED(new TranslatableComponent("gui.xaero_claims_claim_limit_reached").withStyle(ChatFormatting.RED), false, true),

		/** The chunk was beyond the maximum distance */
		TOO_FAR(new TranslatableComponent("gui.xaero_claims_claim_not_within_distance").withStyle(ChatFormatting.RED), false, true),

		/** Successfully unforceloaded a chunk */
		SUCCESSFUL_UNFORCELOAD(new TranslatableComponent("gui.xaero_claims_unforceloaded"), true, false),

		/** Successfully unclaimed a chunk */
		SUCCESSFUL_UNCLAIM(new TranslatableComponent("gui.xaero_claims_unclaimed"), true, false),

		/** Successfully forceloaded a chunk */
		SUCCESSFUL_FORCELOAD(new TranslatableComponent("gui.xaero_claims_forceloaded"), true, false),

		/** Successfully claimed a chunk */
		SUCCESSFUL_CLAIM(new TranslatableComponent("gui.xaero_claims_claimed"), true, false);

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
		
		private Type(Component message, boolean success, boolean fail) {
			this.message = message;
			this.success = success;
			this.fail = fail;
		}
		
	}

}
