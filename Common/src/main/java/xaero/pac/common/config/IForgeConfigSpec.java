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

package xaero.pac.common.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.ConfigSpec;
import com.electronwill.nightconfig.core.UnmodifiableConfig;

public interface IForgeConfigSpec {

    public void setConfig(CommentedConfig config);

    public void acceptConfig(final CommentedConfig data);

    public boolean isCorrecting();

    public boolean isLoaded();

    public UnmodifiableConfig getSpec();

    public UnmodifiableConfig getValues();

    public void afterReload();

    public void save();

    public boolean isCorrect(CommentedConfig config);

    public int correct(CommentedConfig config);

    public int correct(CommentedConfig config, ConfigSpec.CorrectionListener listener);

    public int correct(CommentedConfig config, ConfigSpec.CorrectionListener listener, ConfigSpec.CorrectionListener commentListener);

}
