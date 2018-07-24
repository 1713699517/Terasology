/*
 * Copyright 2017 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.reflection;

import org.terasology.utilities.ReflectionUtil;

import java.lang.reflect.Type;

/**
 * Represents the type {@link T}. The type information generated is more comprehensive than {@link Class},
 * and {@link #type} correctly represents {@link T} whether it is generic or a wildcard type.
 *
 * <p>
 * Clients must create a subclass so that the full type information can be retrieved at run-time:
 *
 * <p>
 * {@code TypeInfo<List<String>> list = new TypeInfo<List<String>>() {}; }
 *
 * <p>
 * However, if the type is a simple class, {@link TypeInfo#of(Class)} will suffice:
 *
 * <p>
 * {@code TypeInfo<String> list = TypeInfo.of(String.class); }
 *
 * @param <T> The type for which type information is to be generated.
 */
public class TypeInfo<T> {
    private final Class<? super T> rawType;
    private final Type type;
    private final int hashCode;

    /**
     * Constructs a new {@link TypeInfo} where the represented type is derived from the type parameter.
     */
    @SuppressWarnings("unchecked")
    protected TypeInfo() {
        this.type = ReflectionUtil.getTypeParameterForSuper(getClass(), TypeInfo.class, 0);
        this.rawType = (Class<? super T>) ReflectionUtil.getClassOfType(type);
        this.hashCode = type.hashCode();
    }

    /**
     * Constructs a new {@link TypeInfo} directly from the type.
     */
    @SuppressWarnings("unchecked")
    protected TypeInfo(Type type) {
        this.type = type;
        this.rawType = (Class<? super T>) ReflectionUtil.getClassOfType(type);
        this.hashCode = type.hashCode();
    }

    public Class<? super T> getRawType() {
        return rawType;
    }

    public Type getType() {
        return type;
    }

    @Override
    public final int hashCode() {
        return this.hashCode;
    }

    /**
     * Create a {@link TypeInfo} for the given {@link Class}.
     */
    public static <T> TypeInfo<T> of(Class<T> type) {
        return new TypeInfo<>(type);
    }
}
