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

package xaero.pac.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

public abstract class WidgetListScreen extends XPACScreen {
	
	private final static int ROW_HEIGHT = 24;
	private final static int PER_PAGE = 12;
	
	private int page;
	private final int pageCount;
	protected final List<WidgetListElement<?>> elements;
	private final List<EditBox> tickableBoxes;
	
	private Button nextButton;
	private Button prevButton;

	protected WidgetListScreen(List<WidgetListElement<?>> elements, List<EditBox> tickableBoxes, Screen escape, Screen parent, Component p_96550_) {
		super(escape, parent, p_96550_);
		this.elements = elements;
		this.tickableBoxes = tickableBoxes;
		this.pageCount = (elements.size() + PER_PAGE - 1) / PER_PAGE;
	}
	
	@Override
	protected void init() {
		super.init();
		int xAnchor = width / 2;
		int yAnchor = height / 7;
		int startIndex = getStartIndex();
		int endIndex = getEndIndex();
		tickableBoxes.clear();
		for(int index = startIndex; index < endIndex; index++) {
			int indexOff = index - startIndex;
			int x = xAnchor - 205 + 210 * (indexOff & 1);
			int y = yAnchor + (indexOff >> 1) * ROW_HEIGHT;
			elements.get(index).screenInit(x, y, this, tickableBoxes);
		}
		addRenderableWidget(new Button(xAnchor - 100, this.height / 6 + 168, 200, 20, new TranslatableComponent("gui.xaero_pac_back"), this::onBackButton));

		prevButton = new Button(this.width / 2 - 205, yAnchor + 144, 75, 20, new TranslatableComponent("gui.xaero_pac_previous", new Object[0]), this::onPrevButton);
		nextButton = new Button(this.width / 2 + 131, yAnchor + 144, 75, 20, new TranslatableComponent("gui.xaero_pac_next", new Object[0]), this::onNextButton);
		if(pageCount > 1) {
			this.addRenderableWidget(prevButton);
			this.addRenderableWidget(nextButton);
			prevButton.active = page > 0;
			nextButton.active = page < pageCount - 1;
		}
	    minecraft.keyboardHandler.setSendRepeatsToGui(true);
	}
	
	private int getStartIndex() {
		return page * PER_PAGE;
	}
	
	private int getEndIndex() {
		return Math.min((page + 1) * PER_PAGE, elements.size());
	}
	
	@Override
	protected <T extends GuiEventListener & Widget & NarratableEntry> T addRenderableWidget(T widget) {
		return super.addRenderableWidget(widget);
	}
	
	@Override
	protected <T extends Widget> T addRenderableOnly(T widget) {
		return super.addRenderableOnly(widget);
	}
	
	@Override
	protected <T extends GuiEventListener & NarratableEntry> T addWidget(T p_96625_) {
		return super.addWidget(p_96625_);
	}
	
	@Override
	public void render(PoseStack poseStack, int mouseX, int mouseY, float partial) {
		renderBackground(poseStack);
		drawCenteredString(poseStack, font, title, width / 2, 16, -1);
		super.render(poseStack, mouseX, mouseY, partial);
		int startIndex = getStartIndex();
		int endIndex = getEndIndex();
		WidgetListElement<?> tooltipElement = null;
		for(int index = startIndex; index < endIndex; index++) {
			WidgetListElement<?> e = elements.get(index);
			e.render(poseStack);
			if(e.isOver(mouseX, mouseY))
				tooltipElement = e;
		}
		if(tooltipElement != null) {
			List<FormattedCharSequence> tooltip = tooltipElement.getTooltip();
			if(tooltip != null)
				renderTooltip(poseStack, tooltip, mouseX, mouseY + ROW_HEIGHT + 10);
		}
	}
	
	@Override
	public void tick() {
		super.tick();
		for (EditBox editBox : tickableBoxes) {
			editBox.tick();
		}
	}
	
	private void onPrevButton(Button b) {
		page--;
		init(minecraft, width, height);
	}
	
	private void onNextButton(Button b) {
		page++;
		init(minecraft, width, height);
	}
	
	protected void onBackButton(Button b) {
		goBack();
	}
	@Override
	public void setFocused(GuiEventListener l) {//setFocused
		GuiEventListener currentFocused = getFocused();
		if(currentFocused != null && currentFocused != l && currentFocused instanceof EditBox)
			((EditBox) currentFocused).setFocus(false);
		super.setFocused(l);
	}

}
