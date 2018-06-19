/*
 *   Copyright 2017 Observational Health Data Sciences and Informatics
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 *   Authors: Vitaly Koulakov
 *
 */

package org.ohdsi.circe.check.operations;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public class Operations<T, V> implements ConditionalOperations<T, V>, ExecutiveOperations<T, V> {

    private Boolean result;

    private final T value;

    private V returnValue;

    private Operations(T value){

        this.value = value;
    }

    public static <T, V> ConditionalOperations<T, V> match(T value) {

        return new Operations<>(value);
    }

    public ExecutiveOperations<T, V> when(Function<T, Boolean> condition) {

        result = Objects.nonNull(value) && condition.apply(value);
        return this;
    }

    public ExecutiveOperations<T, V> isA(Class<?> clazz) {

        result = Objects.nonNull(clazz) && Objects.nonNull(value) &&
            clazz.isAssignableFrom(value.getClass());
        return this;
    }

    public ConditionalOperations<T, V> then(Consumer<T> consumer) {

        if (result) {
            consumer.accept(value);
        }
        return this;
    }

    public ConditionalOperations<T, V> then(Execution execution) {

        if (result) {
            execution.apply();
        }
        return this;
    }

    public ConditionalOperations<T, V> thenReturn(Function<T, V> function) {

        if (result) {
            returnValue = function.apply(value);
        }
        return this;
    }

    public void orElse(Consumer<T> consumer) {

        if (!result) {
            consumer.accept(value);
        }
    }

    public V value() {

        return returnValue;
    }
}
