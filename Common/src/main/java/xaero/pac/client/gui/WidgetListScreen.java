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

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.apache.commons.lang3.tuple.Pair;
import xaero.pac.client.gui.util.EditBoxUtils;
import xaero.pac.client.gui.widget.FixedEditBox;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public abstract class WidgetListScreen<C> extends XPACScreen {
	
	public final static int ROW_HEIGHT = 24;
	private final static int PER_PAGE = 12;
	
	private int page;
	private int pageCount;
	protected final List<WidgetListElement<C, ?>> elements;
	protected final List<WidgetListElement<C, ?>> filteredElements;
	private final List<EditBox> tickableBoxes;
	private final Component searchName;
	private final Component noOptionsComponent;
	private EditBox searchBox;
	private String searchValue;
	private boolean shouldRefocusSearch;
	private Component displayedTitle;
	private final int searchAlignment;
	private boolean searchResponderPaused;
	protected final C[] categories;
	protected C chosenCategory;
	
	private Button nextButton;
	private Button prevButton;

	protected WidgetListScreen(
			List<WidgetListElement<C, ?>> elements,
			List<WidgetListElement<C, ?>> filteredElements,
			List<EditBox> tickableBoxes,
			Screen escape,
			Screen parent,
			Component p_96550_,
			Component searchName,
			Component noOptionsComponent,
			int searchAlignment,
			C[] categories
	) {
		super(escape, parent, p_96550_);
		this.elements = elements;
		this.tickableBoxes = tickableBoxes;
		this.filteredElements = filteredElements;
		this.searchName = searchName;
		this.noOptionsComponent = noOptionsComponent;
		this.searchAlignment = searchAlignment;
		this.searchValue = "";
		this.categories = categories;
		updateFilteredElements();
	}
	
	@Override
	protected void init() {
		super.init();
		if(page >= pageCount)
			page = Math.max(0, pageCount - 1);
		tickableBoxes.clear();
		int xAnchor = width / 2;
		int yAnchor = height / 7 + ROW_HEIGHT;
		if(searchBox == null) {
			searchBox = new FixedEditBox(
					font, width / 2 - 100 + 105 * searchAlignment, height / 7,
					200, 20,
					searchName
			);
			searchBox.setValue(searchValue);
			searchBox.setResponder(this::onSearchInput);
		} else {
			searchBox.setX(width / 2 - 100 + 105 * searchAlignment);
			searchBox.setY(height / 7);
		}
		tickableBoxes.add(searchBox);
		addRenderableWidget(searchBox);
		int startIndex = getStartIndex();
		int endIndex = getEndIndex();
		for(int index = startIndex; index < endIndex; index++) {
			int indexOff = index - startIndex;
			int x = xAnchor - 205 + 210 * (indexOff / 6);
			int y = yAnchor + (indexOff % 6) * ROW_HEIGHT;
			filteredElements.get(index).screenInit(x, y, this, tickableBoxes);
		}
		addRenderableWidget(Button.builder(Component.translatable("gui.xaero_pac_back"), this::onBackButton).bounds(xAnchor - 100, yAnchor + 147, 200, 20).build());

		prevButton = Button.builder(Component.translatable("gui.xaero_pac_previous", new Object[0]), this::onPrevButton).bounds(this.width / 2 - 205, yAnchor + 147, 75, 20).build();
		nextButton = Button.builder(Component.translatable("gui.xaero_pac_next", new Object[0]), this::onNextButton).bounds(this.width / 2 + 131, yAnchor + 147, 75, 20).build();
		if(pageCount > 1) {
			this.addRenderableWidget(prevButton);
			this.addRenderableWidget(nextButton);
			prevButton.active = page > 0;
			nextButton.active = page < pageCount - 1;
		}
		displayedTitle = Component.literal("");
		displayedTitle.getSiblings().add(title);
		displayedTitle.getSiblings().add(Component.literal(" (" + (page + 1) + "/" + pageCount + ")"));

		if(shouldRefocusSearch) {
			setFocused(searchBox);
			searchBox.setFocused(true);
			shouldRefocusSearch = false;
		}
	}

	public void onSearchInput(String input){
		if(searchResponderPaused)
			return;
		if(input.equals(searchValue))
			return;
		searchValue = input;
		shouldRefocusSearch = true;
		updateFilteredElements();
		init(minecraft, width, height);
	}

	private void updateFilteredElements(){
		filteredElements.clear();
		boolean searching = !searchValue.isEmpty();
		List<Pair<WidgetListElement<C, ?>, Integer>> matchesToSort = new ArrayList<>();
		String lowercasedSearchValue = searchValue.toLowerCase();
		for (WidgetListElement<C, ?> element : elements) {
			if(!searching || chosenCategory != null) {
				C elementCategory = element.getCategory();
				if (elementCategory != chosenCategory)
					continue;
			}
			String elementSearchText = element.getSearchText().toLowerCase();
			int matchPos = elementSearchText.toLowerCase().indexOf(lowercasedSearchValue);
			if(matchPos == -1)
				continue;
			matchesToSort.add(Pair.of(element, matchPos));
		}
		if(!matchesToSort.isEmpty()) {
			matchesToSort.sort(Comparator.comparing(Pair::getRight));
			matchesToSort.stream().map(Pair::getLeft).forEach(filteredElements::add);
		}
		if(!searching && !elements.isEmpty() && chosenCategory == null && categories != null) {
			for (C category : categories) {
				Component categoryName = getCategoryName(category);
				WidgetListElement<C, ?> categoryWidget = SimpleWidgetListElement.Builder.<C>begin()
						.setW(200)
						.setH(20)
						.setMutable(true)
						.setSearchName(categoryName::getString)
						.setWidgetSupplier((el, xy) ->
								Button.builder(categoryName,
										b -> {
											chosenCategory = category;
											updateFilteredElements();
											init(minecraft, width, height);
										}
								).bounds(xy.getX(), xy.getY(), 200, 20).build()
						).build();
				filteredElements.add(categoryWidget);
			}
		}
		page = 0;
		pageCount = (filteredElements.size() + PER_PAGE - 1) / PER_PAGE;
	}
	
	private int getStartIndex() {
		return page * PER_PAGE;
	}
	
	private int getEndIndex() {
		return Math.min((page + 1) * PER_PAGE, filteredElements.size());
	}
	
	@Override
	protected <T extends GuiEventListener & Renderable & NarratableEntry> T addRenderableWidget(T widget) {
		return super.addRenderableWidget(widget);
	}
	
	@Override
	protected <T extends Renderable> T addRenderableOnly(T widget) {
		return super.addRenderableOnly(widget);
	}
	
	@Override
	protected <T extends GuiEventListener & NarratableEntry> T addWidget(T p_96625_) {
		return super.addWidget(p_96625_);
	}
	
	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partial) {
		renderBackground(guiGraphics);
		guiGraphics.drawCenteredString(font, displayedTitle, width / 2, height / 7 - 17, -1);
		boolean searchBoxNeedsHint = !searchBox.isFocused() && searchBox.getValue().isEmpty();
		if(searchBoxNeedsHint){
			searchResponderPaused = true;
			EditBoxUtils.setEditBoxValueChecked(searchBox, I18n.get("gui.xaero_pac_ui_widget_list_search_box_hint"), 0xFF555555);
			searchResponderPaused = false;
		}
		super.render(guiGraphics, mouseX, mouseY, partial);
		if(searchBoxNeedsHint){
			searchResponderPaused = true;
			EditBoxUtils.setEditBoxValueChecked(searchBox, "", EditBox.DEFAULT_TEXT_COLOR);
			searchResponderPaused = false;
		}
	}

	@Override
	protected void renderPreDropdown(GuiGraphics guiGraphics, int mouseX, int mouseY, float partial) {
		super.renderPreDropdown(guiGraphics, mouseX, mouseY, partial);
		if((chosenCategory != null || !searchValue.isEmpty()) && filteredElements.isEmpty())
			guiGraphics.drawCenteredString(font, noOptionsComponent, width / 2, height / 7 + ROW_HEIGHT + 10, 0xFFAAAAAA);
		int startIndex = getStartIndex();
		int endIndex = getEndIndex();
		WidgetListElement<C, ?> tooltipElement = null;
		for (int index = startIndex; index < endIndex; index++) {
			WidgetListElement<C, ?> e = filteredElements.get(index);
			e.render(guiGraphics);
			if (e.isOver(mouseX, mouseY))
				tooltipElement = e;
		}
		if ((openDropdown == null || !openDropdown.isHovered()) && tooltipElement != null) {
			List<FormattedCharSequence> tooltip = tooltipElement.getTooltip();
			if (tooltip != null)
				setTooltipForNextRenderPass(tooltip, OPACTooltipPositioner.INSTANCE, true);
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
		if(chosenCategory != null){
			chosenCategory = null;
			searchValue = "";
			searchBox.setValue(searchValue);
			updateFilteredElements();
			init(minecraft, width, height);
			return;
		}
		goBack();
	}

	public void preservePageAndSearch(WidgetListScreen<C> another){
		this.searchValue = another.searchValue;
		this.chosenCategory = another.chosenCategory;
		updateFilteredElements();
		this.page = another.page;
	}

	protected abstract Component getCategoryName(C category);

}
