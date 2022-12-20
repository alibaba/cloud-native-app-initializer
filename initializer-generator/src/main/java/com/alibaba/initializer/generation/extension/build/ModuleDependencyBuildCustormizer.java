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

package com.alibaba.initializer.generation.extension.build;

import com.alibaba.initializer.metadata.Module;
import io.spring.initializr.generator.buildsystem.Build;
import io.spring.initializr.generator.spring.build.BuildCustomizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;

/**
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 */
public class ModuleDependencyBuildCustormizer implements BuildCustomizer<Build> {

    @Autowired
    private Module module;

    @Override
    public void customize(Build build) {
        if (module.isRoot() && !module.isMain()) {
            build.dependencies()
                    .ids()
                    .toList()
                    .forEach(id -> build.dependencies().remove(id));
        }
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
