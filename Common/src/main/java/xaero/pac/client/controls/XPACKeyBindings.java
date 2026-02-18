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

package xaero.pac.client.controls;


import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.client.controls.api.OPACKeyBindingsAPI;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class XPACKeyBindings implements OPACKeyBindingsAPI {

	private final KeyMapping.Category category =
			new KeyMapping.Category(Identifier.fromNamespaceAndPath(OpenPartiesAndClaims.MOD_ID, "controls"));
	private final List<KeyMapping> keyBindings;
	public final KeyMapping openModMenu;

	public XPACKeyBindings() {
		keyBindings = new ArrayList<>();
		keyBindings.add(openModMenu = new KeyMapping("gui.xaero_pac_key_open_menu", GLFW.GLFW_KEY_APOSTROPHE, category));
	}
	
	public void register(Consumer<KeyMapping> registry, Consumer<KeyMapping.Category> categoryRegistry) {
		categoryRegistry.accept(category);
		keyBindings.forEach(registry::accept);
	}
	
	@Nonnull
	@Override
	public KeyMapping getOpenModMenuKeyBinding() {
		return openModMenu;
	}
	
}
