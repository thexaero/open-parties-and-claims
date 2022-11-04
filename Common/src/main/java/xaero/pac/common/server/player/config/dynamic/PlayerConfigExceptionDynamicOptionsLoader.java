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

package xaero.pac.common.server.player.config.dynamic;

import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.player.config.dynamic.PlayerConfigDynamicOptions;
import xaero.pac.common.server.claims.protection.ChunkProtectionExceptionType;
import xaero.pac.common.server.claims.protection.group.ChunkProtectionExceptionGroup;
import xaero.pac.common.server.player.config.PlayerConfig;
import xaero.pac.common.server.player.config.PlayerConfigStaticListIterationOptionSpec;

import java.util.function.Function;

public class PlayerConfigExceptionDynamicOptionsLoader {

	public static final String OPTION_ROOT = PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.exceptionGroups.";
	public static final String TRANSLATION_ROOT = "gui.xaero_pac_player_config_" + PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.exceptionGroups.";
	public static final String COMMENT_TRANSLATION_ROOT = "gui.xaero_pac_player_config_tooltip_" + PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.exceptionGroups.";
	public static final String INTERACT = "interact";
	public static final String HAND_INTERACT = "handInteract";
	public static final String BREAK = "break";
	public static final String BARRIER = "barrier";

	<T> void handleGroup(ChunkProtectionExceptionGroup<T> group, PlayerConfigDynamicOptions.Builder builder, String category, String categoryPlural, Function<T, String> objectNameGetter){
		String optionId;
		String comment;
		String translation;
		String commentTranslation;
		String interactionOptionsTooltip = "\n\n1) Party - only members of the same party as you.\n2) Allies - only members of parties allied by the one you're in.\n3) Everyone - any player.";
		if(group.getType() == ChunkProtectionExceptionType.INTERACTION) {
			optionId = OPTION_ROOT + category + "." + INTERACT;
			comment = "When enabled, claimed chunk protection makes an exception for interaction with the following " + categoryPlural + ": %1$s." + interactionOptionsTooltip;
			translation = TRANSLATION_ROOT + category + "." + INTERACT;
			commentTranslation = COMMENT_TRANSLATION_ROOT + category + "." + INTERACT;
		} else if(group.getType() == ChunkProtectionExceptionType.EMPTY_HAND_INTERACTION) {
			optionId = OPTION_ROOT + category + "." + HAND_INTERACT;
			comment = "When enabled, claimed chunk protection makes an exception for interaction with an empty hand with the following " + categoryPlural + ": %1$s." + interactionOptionsTooltip;
			translation = TRANSLATION_ROOT + category + "." + HAND_INTERACT;
			commentTranslation = COMMENT_TRANSLATION_ROOT + category + "." + HAND_INTERACT;
		} else if(group.getType() == ChunkProtectionExceptionType.BREAK){
			optionId = OPTION_ROOT + category + "." + BREAK;
			comment = "When enabled, claimed chunk protection makes an exception for destruction of the following " + categoryPlural + ": %1$s." + interactionOptionsTooltip;
			translation = TRANSLATION_ROOT + category + "." + BREAK;
			commentTranslation = COMMENT_TRANSLATION_ROOT + category + "." + BREAK;
		} else if(group.getType() == ChunkProtectionExceptionType.BARRIER){
			optionId = OPTION_ROOT + category + "." + BARRIER;
			comment = "When enabled, claimed chunk protection prevents the following additional " + categoryPlural + " from entering the claim (except wilderness): %1$s.\n\n" + PlayerConfig.PROTECTION_LEVELS_TOOLTIP;
			translation = TRANSLATION_ROOT + category + "." + BARRIER;
			commentTranslation = COMMENT_TRANSLATION_ROOT + category + "." + BARRIER;
		} else {
			OpenPartiesAndClaims.LOGGER.error("Invalid group type " + group.getType() + " for " + category + " exception group " + group.getName());
			return;
		}
		optionId += "." + group.getName();
		comment = String.format(comment, group.getContentString());
		PlayerConfigStaticListIterationOptionSpec<Integer> option = PlayerConfigStaticListIterationOptionSpec.Builder.begin(Integer.class)
				.setId(optionId)
				.setList(PlayerConfig.PROTECTION_LEVELS)
				.setTranslation(translation, group.getName())
				.setCommentTranslation(commentTranslation, group.getContentString())
				.setDefaultValue(0)
				.setComment(comment)
				.build(null);
		group.setPlayerConfigOption(option);
		builder.addOption(option);
	}

}
