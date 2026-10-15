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

package xaero.pac.client.gui;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import org.joml.Vector2i;
import org.joml.Vector2ic;

public class OPACTooltipPositioner implements ClientTooltipPositioner {

	public static final OPACTooltipPositioner INSTANCE = new OPACTooltipPositioner();

	@Override
	public Vector2ic positionTooltip(int screenWidth, int screenHeight, int mouseX, int mouseY, int width, int height) {
		Vector2i result = new Vector2i(mouseX + 12, mouseY + WidgetListScreen.ROW_HEIGHT);
		if(result.x + width > screenWidth)
			result.x = mouseX - 12 - width;
		if(result.x < 4)
			result.x = 4;
		if(result.y + height + 3 > screenHeight)
			result.y = screenHeight - height - 3;
		return result;
	}

}
