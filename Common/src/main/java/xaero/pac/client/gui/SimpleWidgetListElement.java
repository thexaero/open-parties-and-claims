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
import net.minecraft.core.Vec3i;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class SimpleWidgetListElement<C> extends WidgetListElement<C, SimpleWidgetListElement<C>> {
	
	protected SimpleWidgetListElement(
			int w,
			int h,
			boolean mutable,
			BiFunction<SimpleWidgetListElement<C>, Vec3i, AbstractWidget> widgetSupplier,
			Supplier<String> searchName,
			Supplier<C> category,
			List<FormattedCharSequence> tooltip
	) {
		super(w, h, mutable, widgetSupplier, searchName, category, tooltip);
	}
	
	@Override
	public final void render(GuiGraphicsExtractor guiGraphics) {
		super.render(guiGraphics);
	}
	
	public static final class Builder<C> extends WidgetListElement.Builder<C, SimpleWidgetListElement<C>, Builder<C>> {
		
		@Override
		protected SimpleWidgetListElement<C> buildInternal() {
			return new SimpleWidgetListElement<C>(w, h, mutable, widgetSupplier, searchName, category, tooltip);
		}
		
		public static <C> Builder<C> begin() {
			return new Builder<C>().setDefault();
		}
		
	}

}
