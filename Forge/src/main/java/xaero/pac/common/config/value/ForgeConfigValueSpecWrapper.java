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

package xaero.pac.common.config.value;

import net.minecraftforge.common.ForgeConfigSpec;

public class ForgeConfigValueSpecWrapper implements IForgeConfigValueSpec {

    private final ForgeConfigSpec.ValueSpec valueSpec;

    public ForgeConfigValueSpecWrapper(ForgeConfigSpec.ValueSpec valueSpec) {
        this.valueSpec = valueSpec;
    }

    public ForgeConfigSpec.ValueSpec getValueSpec() {
        return valueSpec;
    }

    @Override
    public String getComment() {
        return valueSpec.getComment();
    }

    @Override
    public String getTranslationKey() {
        return valueSpec.getTranslationKey();
    }

    @Override
    public boolean needsWorldRestart() {
        return valueSpec.needsWorldRestart();
    }

    @Override
    public Class<?> getClazz() {
        return valueSpec.getClazz();
    }

    @Override
    public boolean test(Object value) {
        return valueSpec.test(value);
    }

    @Override
    public Object correct(Object value) {
        return valueSpec.correct(value);
    }

    @Override
    public Object getDefault() {
        return valueSpec.getDefault();
    }
}
