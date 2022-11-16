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

package xaero.pac.common.server.player.localization.api;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nonnull;

/**
 * A text localizer that adapts to the player that the text is to be read by, whether the player has the mod installed or not.
 */
public interface IAdaptiveLocalizerAPI {

	/**
	 * Gets a text component for translatable text adapted to a specified player.
	 *
	 * @param player  the player to read the text component, not null
	 * @param key  the key of the translated line, not null
	 * @param args  the arguments to format the translated line with, optional
	 * @return the text component, not null
	 */
	@Nonnull
	public MutableComponent getFor(@Nonnull ServerPlayer player, @Nonnull String key, @Nonnull Object... args);

	/**
	 * Converts a text component to be readable by a specified player, if necessary.
	 * <p>
	 * Only ever converts translatable text components and always returns anything else unchanged.
	 * The arguments of a translatable text component are also converted if necessary.
	 * Does not convert the siblings of the text component.
	 *
	 * @param player  the player to read the text component, not null
	 * @param component  the text component to adapt to the player, not null
	 * @return the converted text component or the unchanged input component if no conversion was necessary, not null
	 */
	@Nonnull
	public Component getFor(@Nonnull ServerPlayer player, @Nonnull Component component);

}
