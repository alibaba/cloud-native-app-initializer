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

package com.alibaba.initializer.generation.extension.architecture;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import io.spring.initializr.generator.io.IndentingWriterFactory;
import io.spring.initializr.generator.project.contributor.ProjectContributor;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * re write pom.xml, append sub modules to it.
 *
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 */
public class MavenArchitectureModuleContributor implements ProjectContributor {

    @Autowired
    private IndentingWriterFactory indentingWriterFactory;

    @Override
    public void contribute(Path projectRoot) throws IOException {
        List<String> moduleNames = Arrays.stream(projectRoot.toFile().listFiles())
                .filter(File::isDirectory)
                .filter(file -> !file.isHidden())
                .map(File::getName)
                .sorted()
                .collect(Collectors.toList());

        if (moduleNames.size() == 0) {
            return;
        }


    }

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }
}
