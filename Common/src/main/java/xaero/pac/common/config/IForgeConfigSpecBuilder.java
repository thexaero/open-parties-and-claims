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
import org.apache.commons.lang3.tuple.Pair;
import xaero.pac.common.config.value.*;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface IForgeConfigSpecBuilder {

    public <T> IForgeConfigValue<T> define(String path, T defaultValue);

    public <T> IForgeConfigValue<T> define(List<String> path, T defaultValue);

    public <T> IForgeConfigValue<T> define(String path, T defaultValue, Predicate<Object> validator);

    public <T> IForgeConfigValue<T> define(List<String> path, T defaultValue, Predicate<Object> validator);

    public <T> IForgeConfigValue<T> define(String path, Supplier<T> defaultSupplier, Predicate<Object> validator);

    public <T> IForgeConfigValue<T> define(List<String> path, Supplier<T> defaultSupplier, Predicate<Object> validator);

    public <T> IForgeConfigValue<T> define(List<String> path, Supplier<T> defaultSupplier, Predicate<Object> validator, Class<?> clazz);

    public <T> IForgeConfigValue<T> define(List<String> path, IForgeConfigValueSpec value, Supplier<T> defaultSupplier);

    public <V extends Comparable<? super V>> IForgeConfigValue<V> defineInRange(String path, V defaultValue, V min, V max, Class<V> clazz);

    public <V extends Comparable<? super V>> IForgeConfigValue<V> defineInRange(List<String> path,  V defaultValue, V min, V max, Class<V> clazz);

    public <V extends Comparable<? super V>> IForgeConfigValue<V> defineInRange(String path, Supplier<V> defaultSupplier, V min, V max, Class<V> clazz);

    public <V extends Comparable<? super V>> IForgeConfigValue<V> defineInRange(List<String> path, Supplier<V> defaultSupplier, V min, V max, Class<V> clazz);

    public <T> IForgeConfigValue<T> defineInList(String path, T defaultValue, Collection<? extends T> acceptableValues);

    public <T> IForgeConfigValue<T> defineInList(String path, Supplier<T> defaultSupplier, Collection<? extends T> acceptableValues);

    public <T> IForgeConfigValue<T> defineInList(List<String> path, T defaultValue, Collection<? extends T> acceptableValues);

    public <T> IForgeConfigValue<T> defineInList(List<String> path, Supplier<T> defaultSupplier, Collection<? extends T> acceptableValues);

    public <T> IForgeConfigValue<List<? extends T>> defineList(String path, List<? extends T> defaultValue, Predicate<Object> elementValidator);
    public <T> IForgeConfigValue<List<? extends T>> defineList(String path, Supplier<List<? extends T>> defaultSupplier, Predicate<Object> elementValidator);
    public <T> IForgeConfigValue<List<? extends T>> defineList(List<String> path, List<? extends T> defaultValue, Predicate<Object> elementValidator);
    public <T> IForgeConfigValue<List<? extends T>> defineList(List<String> path, Supplier<List<? extends T>> defaultSupplier, Predicate<Object> elementValidator);

    public <T> IForgeConfigValue<List<? extends T>> defineListAllowEmpty(List<String> path, Supplier<List<? extends T>> defaultSupplier, Predicate<Object> elementValidator);

    //Enum
    public <V extends Enum<V>> IForgeConfigEnumValue<V> defineEnum(String path, V defaultValue);
    public <V extends Enum<V>> IForgeConfigEnumValue<V> defineEnum(String path, V defaultValue, EnumGetMethod converter);
    public <V extends Enum<V>> IForgeConfigEnumValue<V> defineEnum(List<String> path, V defaultValue);
    public <V extends Enum<V>> IForgeConfigEnumValue<V> defineEnum(List<String> path, V defaultValue, EnumGetMethod converter);
    @SuppressWarnings("unchecked")
    public <V extends Enum<V>> IForgeConfigEnumValue<V> defineEnum(String path, V defaultValue, V... acceptableValues);
    @SuppressWarnings("unchecked")
    public <V extends Enum<V>> IForgeConfigEnumValue<V> defineEnum(String path, V defaultValue, EnumGetMethod converter, V... acceptableValues);
    @SuppressWarnings("unchecked")
    public <V extends Enum<V>> IForgeConfigEnumValue<V> defineEnum(List<String> path, V defaultValue, V... acceptableValues);
    @SuppressWarnings("unchecked")
    public <V extends Enum<V>> IForgeConfigEnumValue<V> defineEnum(List<String> path, V defaultValue, EnumGetMethod converter, V... acceptableValues);
    public <V extends Enum<V>> IForgeConfigEnumValue<V> defineEnum(String path, V defaultValue, Collection<V> acceptableValues);
    public <V extends Enum<V>> IForgeConfigEnumValue<V> defineEnum(String path, V defaultValue, EnumGetMethod converter, Collection<V> acceptableValues);
    public <V extends Enum<V>> IForgeConfigEnumValue<V> defineEnum(List<String> path, V defaultValue, Collection<V> acceptableValues);
    public <V extends Enum<V>> IForgeConfigEnumValue<V> defineEnum(List<String> path, V defaultValue, EnumGetMethod converter, Collection<V> acceptableValues);
    public <V extends Enum<V>> IForgeConfigEnumValue<V> defineEnum(String path, V defaultValue, Predicate<Object> validator);
    public <V extends Enum<V>> IForgeConfigEnumValue<V> defineEnum(String path, V defaultValue, EnumGetMethod converter, Predicate<Object> validator);
    public <V extends Enum<V>> IForgeConfigEnumValue<V> defineEnum(List<String> path, V defaultValue, Predicate<Object> validator);
    public <V extends Enum<V>> IForgeConfigEnumValue<V> defineEnum(List<String> path, V defaultValue, EnumGetMethod converter, Predicate<Object> validator);
    public <V extends Enum<V>> IForgeConfigEnumValue<V> defineEnum(String path, Supplier<V> defaultSupplier, Predicate<Object> validator, Class<V> clazz);
    public <V extends Enum<V>> IForgeConfigEnumValue<V> defineEnum(String path, Supplier<V> defaultSupplier, EnumGetMethod converter, Predicate<Object> validator, Class<V> clazz);
    public <V extends Enum<V>> IForgeConfigEnumValue<V> defineEnum(List<String> path, Supplier<V> defaultSupplier, Predicate<Object> validator, Class<V> clazz);
    public <V extends Enum<V>> IForgeConfigEnumValue<V> defineEnum(List<String> path, Supplier<V> defaultSupplier, EnumGetMethod converter, Predicate<Object> validator, Class<V> clazz);

    //boolean
    public IForgeConfigBooleanValue define(String path, boolean defaultValue);
    public IForgeConfigBooleanValue define(List<String> path, boolean defaultValue);
    public IForgeConfigBooleanValue define(String path, Supplier<Boolean> defaultSupplier);
    public IForgeConfigBooleanValue define(List<String> path, Supplier<Boolean> defaultSupplier);

    //Double
    public IForgeConfigDoubleValue defineInRange(String path, double defaultValue, double min, double max);
    public IForgeConfigDoubleValue defineInRange(List<String> path, double defaultValue, double min, double max);
    public IForgeConfigDoubleValue defineInRange(String path, Supplier<Double> defaultSupplier, double min, double max);
    public IForgeConfigDoubleValue defineInRange(List<String> path, Supplier<Double> defaultSupplier, double min, double max);

    //Ints
    public IForgeConfigIntValue defineInRange(String path, int defaultValue, int min, int max);
    public IForgeConfigIntValue defineInRange(List<String> path, int defaultValue, int min, int max);
    public IForgeConfigIntValue defineInRange(String path, Supplier<Integer> defaultSupplier, int min, int max);
    public IForgeConfigIntValue defineInRange(List<String> path, Supplier<Integer> defaultSupplier, int min, int max);

    //Longs
    public IForgeConfigLongValue defineInRange(String path, long defaultValue, long min, long max);
    public IForgeConfigLongValue defineInRange(List<String> path, long defaultValue, long min, long max);
    public IForgeConfigLongValue defineInRange(String path, Supplier<Long> defaultSupplier, long min, long max);
    public IForgeConfigLongValue defineInRange(List<String> path, Supplier<Long> defaultSupplier, long min, long max);

    public IForgeConfigSpecBuilder comment(String comment);
    public IForgeConfigSpecBuilder comment(String... comment);

    public IForgeConfigSpecBuilder translation(String translationKey);

    public IForgeConfigSpecBuilder worldRestart();

    public IForgeConfigSpecBuilder push(String path);

    public IForgeConfigSpecBuilder push(List<String> path);

    public IForgeConfigSpecBuilder pop();

    public IForgeConfigSpecBuilder pop(int count);

    public <T> Pair<T, IForgeConfigSpec> configure(Function<IForgeConfigSpecBuilder, T> consumer);

    public IForgeConfigSpec build();
}
