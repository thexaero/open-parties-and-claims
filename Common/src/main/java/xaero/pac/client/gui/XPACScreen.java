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

package xaero.pac.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class XPACScreen extends Screen {

	private static final Component XPAC_TITLE = Component.translatable("gui.xaero_pac_ui_parties_and_claims");
	protected final Screen escape;
	protected final Screen parent;

	protected XPACScreen(Screen escape, Screen parent, Component p_96550_) {
		super(p_96550_);
		this.escape = escape;
		this.parent = parent;
	}
	
	@Override
	public void onClose() {
		minecraft.setScreen(escape);
	}
	
	public void goBack() {
		minecraft.setScreen(parent);
	}
	
	@Override
	public void render(PoseStack poseStack, int mouseX, int mouseY, float partial) {
		drawCenteredString(poseStack, font, XPAC_TITLE, width / 2, 5, -1);
		super.render(poseStack, mouseX, mouseY, partial);
	}

}
