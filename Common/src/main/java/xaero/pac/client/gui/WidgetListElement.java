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

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.Vec3i;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public abstract class WidgetListElement<C, E extends WidgetListElement<C, E>> {

	protected final E self;
	private final BiFunction<E, Vec3i, AbstractWidget> widgetSupplier;
	private final Supplier<String> searchName;
	private final Supplier<C> category;
	private final List<FormattedCharSequence> tooltip;
	private String tooltipTextCache;
	protected int x;
	protected int y;
	protected final int w;
	protected final int h;
	protected final boolean mutable;
	
	@SuppressWarnings("unchecked")
	protected WidgetListElement(
			int w,
			int h,
			boolean mutable,
			BiFunction<E, Vec3i, AbstractWidget> widgetSupplier,
			Supplier<String> searchName,
			Supplier<C> category,
			List<FormattedCharSequence> tooltip
	) {
		super();
		this.searchName = searchName;
		this.category = category;
		this.self = (E) this;
		this.w = w;
		this.h = h;
		this.mutable = mutable;
		this.widgetSupplier = widgetSupplier;
		this.tooltip = tooltip;
	}
	
	public AbstractWidget screenInit(int x, int y, WidgetListScreen screen, List<EditBox> tickableBoxes) {
		this.x = x;
		this.y = y;
		AbstractWidget widget = widgetSupplier.apply(self, new Vec3i(x, y, 0));
		screen.addRenderableWidget(widget);
		widget.active = mutable;
		if(widget instanceof EditBox) {
			tickableBoxes.add((EditBox) widget);
			((EditBox) widget).setEditable(mutable);
		}
		return widget;
	}
	
	public boolean isOver(int mouseX, int mouseY) {
		return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
	}
	
	public List<FormattedCharSequence> getTooltip() {
		return tooltip;
	}
	
	public void render(GuiGraphicsExtractor guiGraphics) {
	}

	protected String getSearchTextNoTooltip(){
		return searchName.get();
	}

	public String getSearchText(){
		StringBuilder searchTextBuilder = new StringBuilder(getSearchTextNoTooltip());
		if(tooltip != null) {
			if(tooltipTextCache == null) {
				StringBuilder tooltipTextBuilder = new StringBuilder();
				for (FormattedCharSequence formattedCharSequence : tooltip) {
					formattedCharSequence.accept((a, s, c) -> {
						tooltipTextBuilder.append((char) c);
						return true;
					});
					tooltipTextBuilder.append(" ");
				}
				tooltipTextCache = tooltipTextBuilder.toString();
			}
			searchTextBuilder.append("\n");
			searchTextBuilder.append(tooltipTextCache);
		}
		return searchTextBuilder.toString();
	}

	public C getCategory() {
		return category == null ? null : category.get();
	}

	public static abstract class Builder<C, E extends WidgetListElement<C, E>, B extends Builder<C, E, B>> {
		
		protected final B self;
		protected BiFunction<E, Vec3i, AbstractWidget> widgetSupplier;
		protected int w;
		protected int h;
		protected Supplier<String> searchName;
		protected Supplier<C> category;
		protected List<FormattedCharSequence> tooltip;
		protected boolean mutable;
		
		@SuppressWarnings("unchecked")
		public Builder() {
			super();
			this.self = (B) this;
		}

		public B setDefault() {
			setW(0);
			setH(0);
			setSearchName(null);
			setCategory(null);
			setTooltip(null);
			setMutable(true);
			setWidgetSupplier(null);
			return self;
		}
		
		public B setWidgetSupplier(BiFunction<E, Vec3i, AbstractWidget> widgetSupplier) {
			this.widgetSupplier = widgetSupplier;
			return self;
		}
		
		public B setW(int w) {
			this.w = w;
			return self;
		}
		
		public B setH(int h) {
			this.h = h;
			return self;
		}

		public B setSearchName(Supplier<String> searchName) {
			this.searchName = searchName;
			return self;
		}

		public B setCategory(Supplier<C> category) {
			this.category = category;
			return self;
		}

		public B setTooltip(List<FormattedCharSequence> tooltip) {
			this.tooltip = tooltip;
			return self;
		}
		
		public B setMutable(boolean mutable) {
			this.mutable = mutable;
			return self;
		}
		
		public E build() {
			if(widgetSupplier == null || searchName == null)
				throw new IllegalStateException();
			return buildInternal();
		}
		
		protected abstract E buildInternal();
		
	}

}
