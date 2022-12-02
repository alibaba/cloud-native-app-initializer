/*
 * Copyright 2022 the original author or authors.
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

package com.alibaba.initializer.generation.extension.dependency;

import java.util.Set;

import com.alibaba.initializer.metadata.InitializerMetadata;
import com.alibaba.initializer.metadata.Module;
import io.spring.initializr.generator.project.MutableProjectDescription;
import io.spring.initializr.generator.project.ProjectDescriptionCustomizer;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * filter dependency for multi-arch module
 *
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 */
public class MultiArchModuleDependencyFilterDescriptionCustomizer implements ProjectDescriptionCustomizer {

    @Autowired
    private Module module;

    @Autowired
    private InitializerMetadata metadata;

    @Override
    public void customize(MutableProjectDescription description) {

        Set<String> ids = description.getRequestedDependencies().keySet();

        if (!module.isMain()) {
            ids.forEach(id -> description.getRequestedDependencies().remove(id));
        }

    }

}
