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

package xaero.pac.common.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.ConfigSpec;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import net.minecraftforge.common.ForgeConfigSpec;

public class ForgeConfigSpecWrapper implements IForgeConfigSpec {

	private final ForgeConfigSpec forgeConfigSpec;

	public ForgeConfigSpecWrapper(ForgeConfigSpec forgeConfigSpec) {
		this.forgeConfigSpec = forgeConfigSpec;
	}

	public ForgeConfigSpec getForgeConfigSpec() {
		return forgeConfigSpec;
	}

	@Override
	public void setConfig(CommentedConfig config) {
		forgeConfigSpec.setConfig(config);
	}

	@Override
	public void acceptConfig(CommentedConfig data) {
		forgeConfigSpec.acceptConfig(data);
	}

	@Override
	public boolean isCorrecting() {
		return forgeConfigSpec.isCorrecting();
	}

	@Override
	public boolean isLoaded() {
		return forgeConfigSpec.isLoaded();
	}

	@Override
	public UnmodifiableConfig getSpec() {
		return forgeConfigSpec.getSpec();
	}

	@Override
	public UnmodifiableConfig getValues() {
		return forgeConfigSpec.getValues();
	}

	@Override
	public void afterReload() {
		forgeConfigSpec.afterReload();
	}

	@Override
	public void save() {
		forgeConfigSpec.save();
	}

	@Override
	public boolean isCorrect(CommentedConfig config) {
		return forgeConfigSpec.isCorrect(config);
	}

	@Override
	public int correct(CommentedConfig config) {
		return forgeConfigSpec.correct(config);
	}

	@Override
	public int correct(CommentedConfig config, ConfigSpec.CorrectionListener listener) {
		return forgeConfigSpec.correct(config, listener);
	}

	@Override
	public int correct(CommentedConfig config, ConfigSpec.CorrectionListener listener, ConfigSpec.CorrectionListener commentListener) {
		return forgeConfigSpec.correct(config, listener, commentListener);
	}
}
