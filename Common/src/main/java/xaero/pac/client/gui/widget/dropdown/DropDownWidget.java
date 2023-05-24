/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2022-2023, Xaero <xaero1996@gmail.com> and contributors
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

package xaero.pac.client.gui.widget.dropdown;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nonnull;

public final class DropDownWidget extends AbstractWidget
{

	public static final int DEFAULT_BACKGROUND = 0xC8000000;
	public static final int SELECTED_DEFAULT_BACKGROUND = 0xC8FFD700;
	public static final int SELECTED_DEFAULT_HOVERED_BACKGROUND = 0xFFFFD700;
	public static final int TRIM = 0xFFA0A0A0;
	public static final int TRIM_OPEN = 0xFFFFFFFF;
	public static final int TRIM_INSIDE = 0xFF323232;
	public static final int LINE_HEIGHT = 11;
	private int xOffset = 0;
	private int yOffset = 0;
	private String[] realOptions = {};
	private String[] options = {};
	private int selected = 0;
	private boolean closed = true;
	private int scroll;
	private long scrollTime;
	private int autoScrolling;
	private final boolean openingUp;
	private final IDropDownWidgetCallback callback;
	private final IDropDownContainer container;
	private final boolean hasEmptyOption;
	private final int selectedBackground;
	private final int selectedHoveredBackground;
	private boolean shortenFromTheRight;

	private DropDownWidget(String[] options, int x, int y, int w, Integer selected, boolean openingUp, IDropDownWidgetCallback callback, IDropDownContainer container, boolean hasEmptyOption, Component narrationTitle)
	{
		super(x, y + (openingUp ? LINE_HEIGHT : 0), w, LINE_HEIGHT, narrationTitle);
		this.realOptions = options;
		this.callback = callback;
		this.container = container;
		int emptyOptionCount = hasEmptyOption ? 1 : 0;
		this.options = new String[realOptions.length + emptyOptionCount];
		System.arraycopy(realOptions, 0, this.options, emptyOptionCount, realOptions.length);
		selectId(selected, false);
		this.openingUp = openingUp;
		this.hasEmptyOption = hasEmptyOption;
		selectedBackground = SELECTED_DEFAULT_BACKGROUND;
		selectedHoveredBackground = SELECTED_DEFAULT_HOVERED_BACKGROUND;
		active = true;
	}

	public int size(){
		return realOptions.length;
	}

	public int getXWithOffset(){
		return getX() + xOffset;
	}

	public int getYWithOffset(){
		return getY() + yOffset;
	}

	private void drawSlot(GuiGraphics guiGraphics, String text, int slotIndex, int pos, int mouseX, int mouseY, boolean scrolling, int optionLimit, int xWithOffset, int yWithOffset){
		int slotBackground;
		int emptyOptionCount = hasEmptyOption ? 1 : 0;
		if(closed && isHoveredOrFocused() || !closed && onDropDownSlot(mouseX, mouseY, slotIndex, scrolling, optionLimit))
			slotBackground = slotIndex - emptyOptionCount == selected ? selectedHoveredBackground : TRIM_INSIDE;
		else
			slotBackground = slotIndex - emptyOptionCount == selected ? selectedBackground : DEFAULT_BACKGROUND;
		if(openingUp)
			pos = -pos - 1;
		guiGraphics.fill(xWithOffset, yWithOffset + LINE_HEIGHT * pos, xWithOffset + width, yWithOffset + LINE_HEIGHT + LINE_HEIGHT *pos, slotBackground);

		guiGraphics.hLine(xWithOffset + 1, xWithOffset + width - 1, yWithOffset + LINE_HEIGHT *pos, TRIM_INSIDE);
		int textWidth = Minecraft.getInstance().font.width(text);
		boolean shortened = false;
		while(textWidth > width - 2) {
			text = shortenFromTheRight ? text.substring(0, text.length() - 1) : text.substring(1);
			textWidth = Minecraft.getInstance().font.width("..." + text);
			shortened = true;
		}
		if(shortened)
			if(shortenFromTheRight)
				text = text + "...";
			else
				text = "..." + text;
		int textColor = /*slotIndex - 1 == selected ? 0x555555 : */0xFFFFFF;
		guiGraphics.drawCenteredString(Minecraft.getInstance().font, text, xWithOffset + width/2, yWithOffset + 2 + LINE_HEIGHT * pos, textColor);
	}

	private void drawMenu(GuiGraphics guiGraphics, int amount, int mouseX, int mouseY, int scaledHeight, int optionLimit){
		boolean scrolling = scrolling(optionLimit);
		int totalH = LINE_HEIGHT * (amount + (scrolling ? 2 : 0));
		int height = scaledHeight;
		if(!openingUp && getY() + totalH + 1 > height) {
			yOffset = height - getY() - totalH - 1;
		} else if(openingUp && getY() - totalH < 0){
			yOffset = totalH - getY();
		} else
			yOffset = 0;
		int xWithOffset = getXWithOffset();
		int yWithOffset = getYWithOffset();
		int first = closed ? 0 : scroll;
		if(scrolling) {
			drawSlot(guiGraphics, (scroll == 0 ? "§8" : "§7") + I18n.get(openingUp ? "gui.xaero_dropdown_scroll_down" : "gui.xaero_dropdown_scroll_up"), -1, 0, mouseX, mouseY, scrolling, optionLimit, xWithOffset, yWithOffset);
			drawSlot(guiGraphics, (scroll + optionLimit >= options.length ? "§8" : "§7") + I18n.get(openingUp ? "gui.xaero_dropdown_scroll_up" : "gui.xaero_dropdown_scroll_down"), -2, amount + 1, mouseX, mouseY, scrolling, optionLimit, xWithOffset, yWithOffset);
		}
		for(int i = first; i < first + amount; i++) {
			String slotText;
			if(hasEmptyOption && i == 0)
				slotText = !closed ? "-" : I18n.get(realOptions[selected]).replace("§§", ":");
			else
				slotText = I18n.get(options[i]).replace("§§", ":");
			drawSlot(guiGraphics, slotText, i, i - first + (scrolling ? 1 : 0), mouseX, mouseY, scrolling, optionLimit, xWithOffset, yWithOffset);
		}
		int trimPosY = yWithOffset - (openingUp ? totalH : 0);
		int trim = closed ? DropDownWidget.TRIM : TRIM_OPEN;
		guiGraphics.vLine(xWithOffset, trimPosY, trimPosY + totalH, trim);
		guiGraphics.vLine(xWithOffset + width, trimPosY, trimPosY + totalH, trim);
		guiGraphics.hLine(xWithOffset, xWithOffset + width, trimPosY, trim);
		guiGraphics.hLine(xWithOffset, xWithOffset + width, trimPosY + totalH, trim);
	}
	
	private boolean scrolling(int optionLimit) {
		return options.length > optionLimit && !closed;
	}

	public void mouseClicked(int mouseX, int mouseY, int mouseButton, int scaledHeight)
	{
		if(!closed){
			int optionLimit = optionLimit(scaledHeight);
			int clickedId = getHoveredId(mouseX, mouseY, scrolling(optionLimit), optionLimit);
			if(clickedId >= 0)
				selectId(clickedId - (hasEmptyOption ? 1 : 0), true);
			else {
				autoScrolling = clickedId == -1 ? 1 : -1;
				scrollTime = System.currentTimeMillis();
				mouseScrolledInternal(autoScrolling, mouseX, mouseY, optionLimit);
			}
			Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
		} else if(options.length > 1 && active) {
			setClosed(false);
			scroll = 0;
		}
	}
	
	public void mouseReleased(int mouseX, int mouseY, int mouseButton, int scaledHeight) {
		autoScrolling = 0;
	}
	
	private int getHoveredId(int mouseX, int mouseY, boolean scrolling, int optionLimit){
		int yOnMenu = mouseY - getYWithOffset();
		int visibleSlotIndex = (openingUp ? (-yOnMenu - 1) : yOnMenu) / LINE_HEIGHT;
		if(scrolling && visibleSlotIndex == 0)
			return -1;
		else if(visibleSlotIndex >= optionLimit + (scrolling ? 1 : 0))
			return -2;
		int slot = scroll + visibleSlotIndex - (scrolling ? 1 : 0);
		if(slot >= options.length)
			slot = options.length - 1;
		return slot;
	}

	public boolean onDropDown(int mouseX, int mouseY, int scaledHeight){
		int optionLimit = optionLimit(scaledHeight);
		return onDropDown(mouseX, mouseY, scrolling(optionLimit), optionLimit);
	}

	public boolean onDropDown(int mouseX, int mouseY, boolean scrolling, int optionLimit){
		int menuTop = getYWithOffset();
		int menuHeight = closed? LINE_HEIGHT : (Math.min(options.length, optionLimit) + (scrolling ? 2 : 0))* LINE_HEIGHT;
		if(openingUp)
			menuTop -= menuHeight;
		int xOnMenu = mouseX - getXWithOffset();
		int yOnMenu = mouseY - menuTop;
		if(xOnMenu < 0 || yOnMenu < 0 || xOnMenu > width || yOnMenu >= menuHeight)
			return false;
		return true;
	}

	private boolean onDropDownSlot(int mouseX, int mouseY, int id, boolean scrolling, int optionLimit){
		if(!onDropDown(mouseX, mouseY, scrolling, optionLimit))
			return false;
		int hoveredSlot = getHoveredId(mouseX, mouseY, scrolling, optionLimit);
		return hoveredSlot == id;
	}

	public void selectId(int id, boolean callCallback) {
		if(id == -1) {
			setClosed(true);
			return;
		}
		boolean newId = id != selected;
		if(newId && (!callCallback || callback.onSelected(this, id)))
			selected = id;
		setClosed(true);
		//options[0] = realOptions[selected];
	}

	@Override
	public void render(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partial) {
		int scaledHeight = Minecraft.getInstance().screen.height;
		isHovered = visible && onDropDown(mouseX, mouseY, scaledHeight);
		if(!visible)
			return;
		render(guiGraphics, mouseX, mouseY, Minecraft.getInstance().screen.height, true);
	}

	@Override
	public void renderWidget(GuiGraphics guiGraphics, int var2, int var3, float var4) {
	}

	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, int scaledHeight, boolean closedOnly){
		if(!closed && closedOnly)
			return;
		int optionLimit = optionLimit(scaledHeight);
		if(autoScrolling != 0 && System.currentTimeMillis() - scrollTime > 100) {
			scrollTime = System.currentTimeMillis();
			mouseScrolledInternal(autoScrolling, mouseX, mouseY, optionLimit);
		}
		drawMenu(guiGraphics, closed ? 1 : Math.min(optionLimit, options.length), mouseX, mouseY, scaledHeight, optionLimit);
	}
	
	public boolean isClosed() {
		return closed;
	}

	public void setClosed(boolean closed) {
		if(this.closed != closed) {
			if (!closed) {
				Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));//only when opened to avoid weird double sound effects
				container.onDropdownOpen(this);
			} else
				container.onDropdownClosed(this);
		}
		this.closed = closed;
	}

	public void mouseScrolled(int wheel, int mouseXScaled, int mouseYScaled, int scaledHeight) {
		mouseScrolledInternal(wheel * (openingUp ? -1 : 1), mouseXScaled, mouseYScaled, optionLimit(scaledHeight));
	}

	private void mouseScrolledInternal(int wheel, int mouseXScaled, int mouseYScaled, int optionLimit) {
		int newScroll = scroll - wheel;
		if(newScroll + optionLimit > options.length)
			newScroll = options.length - optionLimit;
		if(newScroll < 0)
			newScroll = 0;
		scroll = newScroll;
	}

	private int optionLimit(int scaledHeight) {
		return Math.max(1, scaledHeight / LINE_HEIGHT - 2);
	}
	
	public int getSelected() {
		return selected;
	}

	@Override
	public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
		narrationElementOutput.add(NarratedElementType.TITLE, createNarrationMessage());
	}

	@Override
	protected MutableComponent createNarrationMessage() {
		MutableComponent narrationMessage = Component.literal("");
		narrationMessage.getSiblings().add(getMessage());
		narrationMessage.getSiblings().add(Component.literal(". "));
		narrationMessage.getSiblings().add(Component.translatable("gui.xaero_dropdown_selected_narration", realOptions[selected]));
		return narrationMessage;
	}

	@Override
	public void mouseMoved(double $$0, double $$1) {
		super.mouseMoved($$0, $$1);
	}

	@Override
	public boolean mouseScrolled(double mouseXScaled, double mouseYScaled, double wheel) {
		if (!isClosed()) {
			mouseScrolled((int) wheel, (int) mouseXScaled, (int) mouseYScaled, Minecraft.getInstance().screen.height);
			return true;
		}
		return super.mouseScrolled(mouseXScaled, mouseYScaled, wheel);
	}

	@Override
	public boolean keyPressed(int $$0, int $$1, int $$2) {
		if($$0 == GLFW.GLFW_KEY_ENTER || $$0 == GLFW.GLFW_KEY_SPACE) {
			int nextSelection;
			if(Screen.hasShiftDown()) {
				nextSelection = getSelected() - 1;
				if(nextSelection < 0)
					nextSelection = realOptions.length - 1;
			} else
				nextSelection = (getSelected() + 1) % realOptions.length;
			selectId(nextSelection, true);
			return true;
		}
		return super.keyPressed($$0, $$1, $$2);
	}

	@Override
	public boolean keyReleased(int $$0, int $$1, int $$2) {
		return super.keyReleased($$0, $$1, $$2);
	}

	@Override
	public boolean charTyped(char $$0, int $$1) {
		return super.charTyped($$0, $$1);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if(isHovered) {
			int scaledHeight = Minecraft.getInstance().screen.height;
			mouseClicked((int) mouseX, (int) mouseY, button, scaledHeight);
			return true;
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		int scaledHeight = Minecraft.getInstance().screen.height;
		mouseReleased((int) mouseX, (int) mouseY, button, scaledHeight);
		return false;
	}

	public static final class Builder {

		private String[] options;
		private int x;
		private int y;
		private int w;
		private Integer selected;
		private boolean openingUp;
		private IDropDownWidgetCallback callback;
		private IDropDownContainer container;
		private boolean hasEmptyOption;
		private Component narrationTitle;

		private Builder (){}

		public Builder setDefault(){
			setOptions(null);
			setX(0);
			setY(0);
			setW(0);
			setSelected(null);
			setOpeningUp(false);
			setCallback(null);
			setHasEmptyOption(true);
			setNarrationTitle(null);
			return this;
		}

		public Builder setOptions(String[] options) {
			this.options = options;
			return this;
		}

		public Builder setX(int x) {
			this.x = x;
			return this;
		}

		public Builder setY(int y) {
			this.y = y;
			return this;
		}

		public Builder setW(int w) {
			this.w = w;
			return this;
		}

		public Builder setSelected(Integer selected) {
			this.selected = selected;
			return this;
		}

		public Builder setOpeningUp(boolean openingUp) {
			this.openingUp = openingUp;
			return this;
		}

		public Builder setCallback(IDropDownWidgetCallback callback) {
			this.callback = callback;
			return this;
		}

		public Builder setContainer(IDropDownContainer container) {
			this.container = container;
			return this;
		}

		public Builder setHasEmptyOption(boolean hasEmptyOption) {
			this.hasEmptyOption = hasEmptyOption;
			return this;
		}

		public Builder setNarrationTitle(Component narrationTitle) {
			this.narrationTitle = narrationTitle;
			return this;
		}

		public DropDownWidget build(){
			if(options == null || w == 0 || selected == null || callback == null || narrationTitle == null || container == null)
				throw new IllegalStateException();
			return new DropDownWidget(options, x, y, w, selected, openingUp, callback, container, hasEmptyOption, narrationTitle);
		}

		public static Builder begin(){
			return new Builder().setDefault();
		}

	}
	
}
