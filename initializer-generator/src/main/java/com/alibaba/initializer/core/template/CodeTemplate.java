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

package com.alibaba.initializer.core.template;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.alibaba.initializer.core.constants.ErrorCodeEnum;
import com.alibaba.initializer.core.exception.BizRuntimeException;
import lombok.Getter;

/**
 * the code template meta info,
 * represent a explicitly template file;
 *
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 */
@Getter
public abstract class CodeTemplate {

    /**
     * the path from repo root
     */
    private final Path path;

    /**
     * the template file name
     */
    private final String fileName;

    public CodeTemplate(Path folder, String fileName) {
        this.path = folder;
        this.fileName = fileName;
    }

    public Path getFullPath() {
        if (path == null) {
            return Paths.get(fileName);
        } else {
            return path.resolve(fileName);
        }
    }

    public abstract Reader getReader() throws IOException;

    public void cleanup() {
        throw new BizRuntimeException(ErrorCodeEnum.SYSTEM_ERROR, "not impl");
    }
}
