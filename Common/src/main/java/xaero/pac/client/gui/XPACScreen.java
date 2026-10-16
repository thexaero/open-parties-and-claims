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

package xaero.pac.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.FormattedCharSequence;
import xaero.pac.client.gui.widget.dropdown.DropDownWidget;
import xaero.pac.client.gui.widget.dropdown.IDropDownContainer;

import java.util.List;

public class XPACScreen extends Screen implements IDropDownContainer {

	private static final Component XPAC_TITLE = new TranslatableComponent("gui.xaero_pac_ui_parties_and_claims");
	protected final Screen escape;
	protected final Screen parent;
	protected DropDownWidget openDropdown;
	protected List<FormattedCharSequence> nextTooltip;

	protected XPACScreen(Screen escape, Screen parent, Component p_96550_) {
		super(p_96550_);
		this.escape = escape;
		this.parent = parent;
	}

	@Override
	protected void init() {
		super.init();
		openDropdown = null;
	}

	@Override
	public void onClose() {
		minecraft.setScreen(escape);
	}
	
	public void goBack() {
		minecraft.setScreen(parent);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if(openDropdown != null){
			if(!openDropdown.onDropDown((int) mouseX, (int) mouseY, height)) {
				openDropdown.setClosed(true);
				openDropdown = null;
			} else
				openDropdown.mouseClicked(mouseX, mouseY, button);
			return true;
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double wheel) {
		if(openDropdown != null) {
			if(openDropdown.onDropDown((int) mouseX, (int) mouseY, height))
				return openDropdown.mouseScrolled(mouseX, mouseY, wheel);
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, wheel);
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if(openDropdown != null)
			if(openDropdown.mouseReleased(mouseX, mouseY, button))
				return true;
		return super.mouseReleased(mouseX, mouseY, button);
	}

	protected int getOPACTitleY(){
		return height / 7 - 27;
	}

	@Override
	public void render(PoseStack poseStack, int mouseX, int mouseY, float partial) {
		super.render(poseStack, mouseX, mouseY, partial);
		drawCenteredString(poseStack, font, XPAC_TITLE, width / 2, getOPACTitleY(), -1);
		renderPreDropdown(poseStack, mouseX, mouseY, partial);
		if(openDropdown != null) {
			poseStack.pushPose();
			poseStack.translate(0, 0, 2);
			openDropdown.render(poseStack, mouseX, mouseY, height, false);
			poseStack.popPose();
		}
	}

	protected void renderPreDropdown(PoseStack poseStack, int mouseX, int mouseY, float partial){
		List<FormattedCharSequence> nextTooltip = this.nextTooltip;
		this.nextTooltip = null;
		if(openDropdown != null && openDropdown.onDropDown(mouseX, mouseY, height))
			return;
		for (GuiEventListener child : children()) {
			if(!(child instanceof DropDownWidget dropDownWidget))
				continue;
			if(!dropDownWidget.isClosed())
				continue;
			if(dropDownWidget.getTooltip() == null)
				continue;
			if(!dropDownWidget.isMouseOver(mouseX, mouseY))
				continue;
			renderTooltip(poseStack, dropDownWidget.getTooltip(), mouseX, mouseY + 34);
			return;
		}
		if(nextTooltip != null)
			renderTooltip(poseStack, nextTooltip, mouseX, mouseY + 34);
	}

	@Override
	public void onDropdownOpen(DropDownWidget menu) {
		if(this.openDropdown != null && this.openDropdown != menu)
			openDropdown.setClosed(true);
		this.openDropdown = menu;
	}

	@Override
	public void onDropdownClosed(DropDownWidget menu) {
		if(menu != this.openDropdown && this.openDropdown != null)
			this.openDropdown.setClosed(true);
		this.openDropdown = null;
	}

	private <T extends GuiEventListener & NarratableEntry, T2 extends GuiEventListener & Widget & NarratableEntry>
	void replaceWidget(T current, T replacement, T2 replaceRenderable) {
		if(current == null)
			return;
		int childIndex = children().indexOf(current);
		if(childIndex == -1)
			return;
		if(current == openDropdown)
			openDropdown = null;
		super.removeWidget(current);
		if(replaceRenderable != null)
			super.addRenderableWidget(replaceRenderable);
		else
			super.addWidget(replacement);
		children().remove(replacement);
		((List<GuiEventListener>)children()).add(childIndex, replacement);
		if(getFocused() == current)
			setFocused(replacement);
	}

	protected <T extends GuiEventListener & NarratableEntry> void replaceWidget(T current, T replacement) {
		replaceWidget(current, replacement, null);
	}

	protected <T extends GuiEventListener & Widget & NarratableEntry> void replaceRenderableWidget(T current, T replacement) {
		replaceWidget(current, replacement, replacement);
	}

	public Screen getEscape() {
		return escape;
	}

	public Screen getParent() {
		return parent;
	}

	protected void buttonTooltip(List<FormattedCharSequence> tooltip, Button button, int mouseX, int mouseY){
		if(button.isMouseOver(mouseX, mouseY) && (openDropdown == null || !openDropdown.onDropDown(mouseX, mouseY, height)))
			nextTooltip = tooltip;
	}

	protected void buttonTooltip(Component tooltip, Button button, int mouseX, int mouseY){
		buttonTooltip(font.split(tooltip, 200), button, mouseX, mouseY);
	}

}
