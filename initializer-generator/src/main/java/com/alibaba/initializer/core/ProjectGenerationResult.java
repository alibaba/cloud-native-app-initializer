/*
 * Copyright 2022-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.initializer.core;

import java.nio.file.Path;

/**
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 */
public interface ProjectGenerationResult {

    /**
     * the generated project name
     */
    String getName();

    /**
     * code folder root path
     */
    Path getProjectRoot();

    default boolean success() {
        return true;
    }

    default String getErrorMsg() {
        return null;
    }

    default void cleanUp() {
        // default do nothing
    }

    static ProjectGenerationResult error(String errorMessage) {
        return new ProjectGenerationResult() {
            @Override
            public String getName() {
                return null;
            }

            @Override
            public Path getProjectRoot() {
                return null;
            }

            @Override
            public boolean success() {
                return false;
            }

            @Override
            public String getErrorMsg() {
                return errorMessage;
            }
        };
    }
}
