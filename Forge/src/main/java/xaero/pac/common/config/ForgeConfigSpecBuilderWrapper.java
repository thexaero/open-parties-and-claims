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

import com.electronwill.nightconfig.core.EnumGetMethod;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;
import xaero.pac.common.config.value.*;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ForgeConfigSpecBuilderWrapper implements IForgeConfigSpecBuilder {

    private final ForgeConfigSpec.Builder builder;

    public ForgeConfigSpecBuilderWrapper(ForgeConfigSpec.Builder builder) {
        this.builder = builder;
    }

    private ForgeConfigSpecBuilderWrapper wrapped(ForgeConfigSpec.Builder b){
        if(builder == b)
            return this;
        else
            return new ForgeConfigSpecBuilderWrapper(b);
    }

    @Override
    public <T> IForgeConfigValue<T> define(String path, T defaultValue) {
        return new ForgeConfigValueWrapper<>(builder.define(path, defaultValue));
    }

    @Override
    public <T> IForgeConfigValue<T> define(List<String> path, T defaultValue) {
        return new ForgeConfigValueWrapper<>(builder.define(path, defaultValue));
    }

    @Override
    public <T> IForgeConfigValue<T> define(String path, T defaultValue, Predicate<Object> validator) {
        return new ForgeConfigValueWrapper<>(builder.define(path, defaultValue, validator));
    }

    @Override
    public <T> IForgeConfigValue<T> define(List<String> path, T defaultValue, Predicate<Object> validator) {
        return new ForgeConfigValueWrapper<>(builder.define(path, defaultValue, validator));
    }

    @Override
    public <T> IForgeConfigValue<T> define(String path, Supplier<T> defaultSupplier, Predicate<Object> validator) {
        return new ForgeConfigValueWrapper<>(builder.define(path, defaultSupplier, validator));
    }

    @Override
    public <T> IForgeConfigValue<T> define(List<String> path, Supplier<T> defaultSupplier, Predicate<Object> validator) {
        return new ForgeConfigValueWrapper<>(builder.define(path, defaultSupplier, validator));
    }

    @Override
    public <T> IForgeConfigValue<T> define(List<String> path, Supplier<T> defaultSupplier, Predicate<Object> validator, Class<?> clazz) {
        return new ForgeConfigValueWrapper<>(builder.define(path, defaultSupplier, validator, clazz));
    }

    @Override
    public <T> IForgeConfigValue<T> define(List<String> path, IForgeConfigValueSpec value, Supplier<T> defaultSupplier) {
        return new ForgeConfigValueWrapper<>(builder.define(path, ((ForgeConfigValueSpecWrapper)value).getValueSpec(), defaultSupplier));
    }

    @Override
    public <V extends Comparable<? super V>> IForgeConfigValue<V> defineInRange(String path, V defaultValue, V min, V max, Class<V> clazz) {
        return new ForgeConfigValueWrapper<>(builder.defineInRange(path, defaultValue, min, max, clazz));
    }

    @Override
    public <V extends Comparable<? super V>> IForgeConfigValue<V> defineInRange(List<String> path, V defaultValue, V min, V max, Class<V> clazz) {
        return new ForgeConfigValueWrapper<>(builder.defineInRange(path, defaultValue, min, max, clazz));
    }

    @Override
    public <V extends Comparable<? super V>> IForgeConfigValue<V> defineInRange(String path, Supplier<V> defaultSupplier, V min, V max, Class<V> clazz) {
        return new ForgeConfigValueWrapper<>(builder.defineInRange(path, defaultSupplier, min, max, clazz));
    }

    @Override
    public <V extends Comparable<? super V>> IForgeConfigValue<V> defineInRange(List<String> path, Supplier<V> defaultSupplier, V min, V max, Class<V> clazz) {
        return new ForgeConfigValueWrapper<>(builder.defineInRange(path, defaultSupplier, min, max, clazz));
    }

    @Override
    public <T> IForgeConfigValue<T> defineInList(String path, T defaultValue, Collection<? extends T> acceptableValues) {
        return new ForgeConfigValueWrapper<>(builder.defineInList(path, defaultValue, acceptableValues));
    }

    @Override
    public <T> IForgeConfigValue<T> defineInList(String path, Supplier<T> defaultSupplier, Collection<? extends T> acceptableValues) {
        return new ForgeConfigValueWrapper<>(builder.defineInList(path, defaultSupplier, acceptableValues));
    }

    @Override
    public <T> IForgeConfigValue<T> defineInList(List<String> path, T defaultValue, Collection<? extends T> acceptableValues) {
        return new ForgeConfigValueWrapper<>(builder.defineInList(path, defaultValue, acceptableValues));
    }

    @Override
    public <T> IForgeConfigValue<T> defineInList(List<String> path, Supplier<T> defaultSupplier, Collection<? extends T> acceptableValues) {
        return new ForgeConfigValueWrapper<>(builder.defineInList(path, defaultSupplier, acceptableValues));
    }

    @Override
    public <T> IForgeConfigValue<List<? extends T>> defineList(String path, List<? extends T> defaultValue, Predicate<Object> elementValidator) {
        return new ForgeConfigValueWrapper<>(builder.defineList(path, defaultValue, elementValidator));
    }

    @Override
    public <T> IForgeConfigValue<List<? extends T>> defineList(String path, Supplier<List<? extends T>> defaultSupplier, Predicate<Object> elementValidator) {
        return new ForgeConfigValueWrapper<>(builder.defineList(path, defaultSupplier, elementValidator));
    }

    @Override
    public <T> IForgeConfigValue<List<? extends T>> defineList(List<String> path, List<? extends T> defaultValue, Predicate<Object> elementValidator) {
        return new ForgeConfigValueWrapper<>(builder.defineList(path, defaultValue, elementValidator));
    }

    @Override
    public <T> IForgeConfigValue<List<? extends T>> defineList(List<String> path, Supplier<List<? extends T>> defaultSupplier, Predicate<Object> elementValidator) {
        return new ForgeConfigValueWrapper<>(builder.defineList(path, defaultSupplier, elementValidator));
    }

    @Override
    public <T> IForgeConfigValue<List<? extends T>> defineListAllowEmpty(List<String> path, Supplier<List<? extends T>> defaultSupplier, Predicate<Object> elementValidator) {
        return new ForgeConfigValueWrapper<>(builder.defineListAllowEmpty(path, defaultSupplier, elementValidator));
    }

    @Override
    public <V extends Enum<V>> IForgeConfigEnumValue<V> defineEnum(String path, V defaultValue) {
        return new ForgeConfigEnumValueWrapper<>(builder.defineEnum(path, defaultValue));
    }

    @Override
    public <V extends Enum<V>> IForgeConfigEnumValue<V> defineEnum(String path, V defaultValue, EnumGetMethod converter) {
        return new ForgeConfigEnumValueWrapper<>(builder.defineEnum(path, defaultValue, converter));
    }

    @Override
    public <V extends Enum<V>> IForgeConfigEnumValue<V> defineEnum(List<String> path, V defaultValue) {
        return new ForgeConfigEnumValueWrapper<>(builder.defineEnum(path, defaultValue));
    }

    @Override
    public <V extends Enum<V>> IForgeConfigEnumValue<V> defineEnum(List<String> path, V defaultValue, EnumGetMethod converter) {
        return new ForgeConfigEnumValueWrapper<>(builder.defineEnum(path, defaultValue, converter));
    }

    @Override
    public <V extends Enum<V>> IForgeConfigEnumValue<V> defineEnum(String path, V defaultValue, V... acceptableValues) {
        return new ForgeConfigEnumValueWrapper<>(builder.defineEnum(path, defaultValue, acceptableValues));
    }

    @Override
    public <V extends Enum<V>> IForgeConfigEnumValue<V> defineEnum(String path, V defaultValue, EnumGetMethod converter, V... acceptableValues) {
        return new ForgeConfigEnumValueWrapper<>(builder.defineEnum(path, defaultValue, converter, acceptableValues));
    }

    @Override
    public <V extends Enum<V>> IForgeConfigEnumValue<V> defineEnum(List<String> path, V defaultValue, V... acceptableValues) {
        return new ForgeConfigEnumValueWrapper<>(builder.defineEnum(path, defaultValue, acceptableValues));
    }

    @Override
    public <V extends Enum<V>> IForgeConfigEnumValue<V> defineEnum(List<String> path, V defaultValue, EnumGetMethod converter, V... acceptableValues) {
        return new ForgeConfigEnumValueWrapper<>(builder.defineEnum(path, defaultValue, converter, acceptableValues));
    }

    @Override
    public <V extends Enum<V>> IForgeConfigEnumValue<V> defineEnum(String path, V defaultValue, Collection<V> acceptableValues) {
        return new ForgeConfigEnumValueWrapper<>(builder.defineEnum(path, defaultValue, acceptableValues));
    }

    @Override
    public <V extends Enum<V>> IForgeConfigEnumValue<V> defineEnum(String path, V defaultValue, EnumGetMethod converter, Collection<V> acceptableValues) {
        return new ForgeConfigEnumValueWrapper<>(builder.defineEnum(path, defaultValue, converter, acceptableValues));
    }

    @Override
    public <V extends Enum<V>> IForgeConfigEnumValue<V> defineEnum(List<String> path, V defaultValue, Collection<V> acceptableValues) {
        return new ForgeConfigEnumValueWrapper<>(builder.defineEnum(path, defaultValue, acceptableValues));
    }

    @Override
    public <V extends Enum<V>> IForgeConfigEnumValue<V> defineEnum(List<String> path, V defaultValue, EnumGetMethod converter, Collection<V> acceptableValues) {
        return new ForgeConfigEnumValueWrapper<>(builder.defineEnum(path, defaultValue, converter, acceptableValues));
    }

    @Override
    public <V extends Enum<V>> IForgeConfigEnumValue<V> defineEnum(String path, V defaultValue, Predicate<Object> validator) {
        return new ForgeConfigEnumValueWrapper<>(builder.defineEnum(path, defaultValue, validator));
    }

    @Override
    public <V extends Enum<V>> IForgeConfigEnumValue<V> defineEnum(String path, V defaultValue, EnumGetMethod converter, Predicate<Object> validator) {
        return new ForgeConfigEnumValueWrapper<>(builder.defineEnum(path, defaultValue, converter, validator));
    }

    @Override
    public <V extends Enum<V>> IForgeConfigEnumValue<V> defineEnum(List<String> path, V defaultValue, Predicate<Object> validator) {
        return new ForgeConfigEnumValueWrapper<>(builder.defineEnum(path, defaultValue, validator));
    }

    @Override
    public <V extends Enum<V>> IForgeConfigEnumValue<V> defineEnum(List<String> path, V defaultValue, EnumGetMethod converter, Predicate<Object> validator) {
        return new ForgeConfigEnumValueWrapper<>(builder.defineEnum(path, defaultValue, converter, validator));
    }

    @Override
    public <V extends Enum<V>> IForgeConfigEnumValue<V> defineEnum(String path, Supplier<V> defaultSupplier, Predicate<Object> validator, Class<V> clazz) {
        return new ForgeConfigEnumValueWrapper<>(builder.defineEnum(path, defaultSupplier, validator, clazz));
    }

    @Override
    public <V extends Enum<V>> IForgeConfigEnumValue<V> defineEnum(String path, Supplier<V> defaultSupplier, EnumGetMethod converter, Predicate<Object> validator, Class<V> clazz) {
        return new ForgeConfigEnumValueWrapper<>(builder.defineEnum(path, defaultSupplier, converter, validator, clazz));
    }

    @Override
    public <V extends Enum<V>> IForgeConfigEnumValue<V> defineEnum(List<String> path, Supplier<V> defaultSupplier, Predicate<Object> validator, Class<V> clazz) {
        return new ForgeConfigEnumValueWrapper<>(builder.defineEnum(path, defaultSupplier, validator, clazz));
    }

    @Override
    public <V extends Enum<V>> IForgeConfigEnumValue<V> defineEnum(List<String> path, Supplier<V> defaultSupplier, EnumGetMethod converter, Predicate<Object> validator, Class<V> clazz) {
        return new ForgeConfigEnumValueWrapper<>(builder.defineEnum(path, defaultSupplier, converter, validator, clazz));
    }

    @Override
    public IForgeConfigBooleanValue define(String path, boolean defaultValue) {
        return new ForgeConfigBooleanValueWrapper(builder.define(path, defaultValue));
    }

    @Override
    public IForgeConfigBooleanValue define(List<String> path, boolean defaultValue) {
        return new ForgeConfigBooleanValueWrapper(builder.define(path, defaultValue));
    }

    @Override
    public IForgeConfigBooleanValue define(String path, Supplier<Boolean> defaultSupplier) {
        return new ForgeConfigBooleanValueWrapper(builder.define(path, defaultSupplier));
    }

    @Override
    public IForgeConfigBooleanValue define(List<String> path, Supplier<Boolean> defaultSupplier) {
        return new ForgeConfigBooleanValueWrapper(builder.define(path, defaultSupplier));
    }

    @Override
    public IForgeConfigDoubleValue defineInRange(String path, double defaultValue, double min, double max) {
        return new ForgeConfigDoubleValueWrapper(builder.defineInRange(path, defaultValue, min, max));
    }

    @Override
    public IForgeConfigDoubleValue defineInRange(List<String> path, double defaultValue, double min, double max) {
        return new ForgeConfigDoubleValueWrapper(builder.defineInRange(path, defaultValue, min, max));
    }

    @Override
    public IForgeConfigDoubleValue defineInRange(String path, Supplier<Double> defaultSupplier, double min, double max) {
        return new ForgeConfigDoubleValueWrapper(builder.defineInRange(path, defaultSupplier, min, max));
    }

    @Override
    public IForgeConfigDoubleValue defineInRange(List<String> path, Supplier<Double> defaultSupplier, double min, double max) {
        return new ForgeConfigDoubleValueWrapper(builder.defineInRange(path, defaultSupplier, min, max));
    }

    @Override
    public IForgeConfigIntValue defineInRange(String path, int defaultValue, int min, int max) {
        return new ForgeConfigIntValueWrapper(builder.defineInRange(path, defaultValue, min, max));
    }

    @Override
    public IForgeConfigIntValue defineInRange(List<String> path, int defaultValue, int min, int max) {
        return new ForgeConfigIntValueWrapper(builder.defineInRange(path, defaultValue, min, max));
    }

    @Override
    public IForgeConfigIntValue defineInRange(String path, Supplier<Integer> defaultSupplier, int min, int max) {
        return new ForgeConfigIntValueWrapper(builder.defineInRange(path, defaultSupplier, min, max));
    }

    @Override
    public IForgeConfigIntValue defineInRange(List<String> path, Supplier<Integer> defaultSupplier, int min, int max) {
        return new ForgeConfigIntValueWrapper(builder.defineInRange(path, defaultSupplier, min, max));
    }

    @Override
    public IForgeConfigLongValue defineInRange(String path, long defaultValue, long min, long max) {
        return new ForgeConfigILongValueWrapper(builder.defineInRange(path, defaultValue, min, max));
    }

    @Override
    public IForgeConfigLongValue defineInRange(List<String> path, long defaultValue, long min, long max) {
        return new ForgeConfigILongValueWrapper(builder.defineInRange(path, defaultValue, min, max));
    }

    @Override
    public IForgeConfigLongValue defineInRange(String path, Supplier<Long> defaultSupplier, long min, long max) {
        return new ForgeConfigILongValueWrapper(builder.defineInRange(path, defaultSupplier, min, max));
    }

    @Override
    public IForgeConfigLongValue defineInRange(List<String> path, Supplier<Long> defaultSupplier, long min, long max) {
        return new ForgeConfigILongValueWrapper(builder.defineInRange(path, defaultSupplier, min, max));
    }

    @Override
    public IForgeConfigSpecBuilder comment(String comment) {
        return wrapped(builder.comment(comment));
    }

    @Override
    public IForgeConfigSpecBuilder comment(String... comment) {
        return wrapped(builder.comment(comment));
    }

    @Override
    public IForgeConfigSpecBuilder translation(String translationKey) {
        return wrapped(builder.translation(translationKey));
    }

    @Override
    public IForgeConfigSpecBuilder worldRestart() {
        return wrapped(builder.worldRestart());
    }

    @Override
    public IForgeConfigSpecBuilder push(String path) {
        return wrapped(builder.push(path));
    }

    @Override
    public IForgeConfigSpecBuilder push(List<String> path) {
        return wrapped(builder.push(path));
    }

    @Override
    public IForgeConfigSpecBuilder pop() {
        return wrapped(builder.pop());
    }

    @Override
    public IForgeConfigSpecBuilder pop(int count) {
        return wrapped(builder.pop(count));
    }

    @Override
    public <T> Pair<T, IForgeConfigSpec> configure(Function<IForgeConfigSpecBuilder, T> consumer) {
        Pair<T, ForgeConfigSpec> result = builder.configure(b -> consumer.apply(new ForgeConfigSpecBuilderWrapper(b)));
        return Pair.of(result.getLeft(), new ForgeConfigSpecWrapper(result.getRight()));
    }

    @Override
    public IForgeConfigSpec build() {
        return new ForgeConfigSpecWrapper(builder.build());
    }
}
