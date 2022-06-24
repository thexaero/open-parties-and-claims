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

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.FormattedCharSequence;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.client.player.config.PlayerConfigClientStorage;
import xaero.pac.common.misc.ListFactory;
import xaero.pac.common.server.player.config.PlayerConfigHexOptionSpec;
import xaero.pac.common.server.player.config.PlayerConfigStringOptionSpec;
import xaero.pac.common.server.player.config.api.PlayerConfigType;

import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public final class PlayerConfigScreen extends WidgetListScreen {
	
	private final BiConsumer<PlayerConfigScreen, Button> refreshHandler;

	private PlayerConfigScreen(List<WidgetListElement<?>> elements, List<EditBox> tickableBoxes, BiConsumer<PlayerConfigScreen, Button> refreshHandler, Screen escape, Screen parent, Component title) {
		super(elements, tickableBoxes, escape, parent, title);
		this.refreshHandler = refreshHandler;
	}
	
	@Override
	protected void init() {
		super.init();
		addRenderableWidget(new Button(5, 5, 60, 20, new TranslatableComponent("gui.xaero_pac_ui_player_config_refresh"), b -> refreshHandler.accept(this, b)));
	}
	
	public final static class Builder {
		
		private final ListFactory listFactory;
		private PlayerConfigClientStorage data;
		private PlayerConfigClientStorage defaultPlayerConfigData;
		private Screen escape;
		private Screen parent;
		private Component title;
		private String otherPlayerName;
		
		private Builder(ListFactory listFactory) {
			super();
			this.listFactory = listFactory;
		}

		public Builder setDefault() {
			setEscape(null);
			setParent(null);
			setData(null);
			setTitle(null);
			return this;
		}
		
		public Builder setData(PlayerConfigClientStorage data) {
			this.data = data;
			return this;
		}
		
		public Builder setDefaultPlayerConfigData(PlayerConfigClientStorage defaultPlayerConfigData) {
			this.defaultPlayerConfigData = defaultPlayerConfigData;
			return this;
		}
		
		public Builder setEscape(Screen escape) {
			this.escape = escape;
			return this;
		}
		
		public Builder setParent(Screen parent) {
			this.parent = parent;
			return this;
		}
		
		public Builder setTitle(Component title) {
			this.title = title;
			return this;
		}
		
		public Builder setOtherPlayerName(String otherPlayerName) {
			this.otherPlayerName = otherPlayerName;
			return this;
		}
		
		public PlayerConfigScreen build() {
			if(data == null || data.getType() == PlayerConfigType.PLAYER && defaultPlayerConfigData == null)
				throw new IllegalStateException();
			if(title == null)
				title = new TranslatableComponent("gui.xaero_pac_ui_player_config");
			boolean anotherPlayer = data.getType() == PlayerConfigType.PLAYER && data.getOwner() != null;
			if(anotherPlayer && otherPlayerName == null)
				throw new IllegalStateException();
			List<WidgetListElement<?>> elements = listFactory.get();
			int elementWidth = 200;
			int elementHeight = 20;
			data.optionStream().forEach(option -> {
				Class<?> type = option.getType();
				Component optionTitle = new TranslatableComponent(option.getTranslation());
				String commentTranslated = I18n.get("gui.xaero_pac_player_config_tooltip_" + option.getId());
				if(commentTranslated.equals("default"))
					commentTranslated = option.getComment();
				if(option.getTooltipPrefix() != null)
					commentTranslated = option.getTooltipPrefix() + "\n" + commentTranslated;
				List<FormattedCharSequence> tooltip = Minecraft.getInstance().font.split(new TextComponent(commentTranslated), 200);
				Object value;
				if(option.isDefaulted() && data.getType() == PlayerConfigType.PLAYER)
					value = defaultPlayerConfigData.getOptionStorage(option.getOption()).getValue();
				else
					value = option.getValue();
				if(type == Boolean.class) {
					BiFunction<SimpleValueWidgetListElement.Final<Boolean>, Vec3i, AbstractWidget> widgetSupplier = (el, xy) -> CycleButton.onOffBuilder(el.getDraftValue()).create(xy.getX(), xy.getY(), elementWidth, elementHeight, optionTitle, 
							(b, v) -> {
								if(!option.isMutable())
									return;
								//on value change
								el.setDraftValue(v);
								option.setCastValue(v);
								OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getPlayerConfigClientSynchronizer().syncToServer(data, option);
							});
					elements.add(SimpleValueWidgetListElement.FinalBuilder.<Boolean>begin()
							.setW(elementWidth)
							.setH(elementHeight)
							.setWidgetSupplier(widgetSupplier)
							.setTooltip(tooltip)
							.setMutable(option.isMutable())
							.setStartValue(value != null && (boolean) value)
							.build());
				} else {
					Predicate<String> filter = Objects::nonNull;
					if(type == Integer.class) {
						if(option.getOption() instanceof PlayerConfigHexOptionSpec)
							filter = s -> s != null && s.matches("^[0-9A-Fa-f]*$");
						else
							filter = s -> s != null && s.matches("^[-0-9]*$");
						
					} else if(type == Double.class || type == Float.class)
						filter = s -> s != null && s.matches("^[-\\.0-9]*$");
					
					TextWidgetListElement.Builder elementBuilder = TextWidgetListElement.Builder.begin()
							.setW(elementWidth)
							.setH(elementHeight)
							.setTitle(optionTitle)
							.setTooltip(tooltip)
							.setMutable(option.isMutable())
							.setStartValue(option.getCommandOutputWriterCast().apply(value))
							.setFilter(filter)
							.setValidator(option.getStringValidator())
							.setResponder(s -> {
								if(!option.isMutable())
									return;
								//on value change
								option.setCastValue(option.getCommandInputParser().apply(s));
								OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getPlayerConfigClientSynchronizer().syncToServer(data, option);
							});
					if(option.getOption() instanceof PlayerConfigStringOptionSpec)
						elementBuilder.setMaxLength(((PlayerConfigStringOptionSpec) option.getOption()).getMaxLength());
					else if(option.getOption() instanceof PlayerConfigHexOptionSpec)
						elementBuilder.setMaxLength(8);
					elements.add(elementBuilder.build());
				}
			});
			BiConsumer<PlayerConfigScreen, Button> refreshHandler;
			if(anotherPlayer)
				refreshHandler = (s,b) -> s.minecraft.setScreen(new OtherPlayerConfigWaitScreen(s.escape, s.parent, otherPlayerName));
			else
				refreshHandler = (s,b) -> s.minecraft.setScreen(build());
			
			return new PlayerConfigScreen(elements, listFactory.get(), refreshHandler, escape, parent, title);	
		}
		
		public static Builder begin(ListFactory listFactory) {
			return new Builder(listFactory).setDefault();
		}
		
	}

}
