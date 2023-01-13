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

package com.alibaba.initializer.generation.extension.dependency;

import com.alibaba.initializer.metadata.EnhancedDependency;
import io.spring.initializr.generator.project.MutableProjectDescription;
import io.spring.initializr.generator.project.ProjectDescriptionCustomizer;
import io.spring.initializr.metadata.DependenciesCapability;
import io.spring.initializr.metadata.Dependency;
import io.spring.initializr.metadata.InitializrMetadata;
import io.spring.initializr.metadata.InitializrMetadataProvider;
import io.spring.initializr.metadata.support.MetadataBuildItemMapper;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Set;

/**
 * auto add dependency of dependency
 *
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 */
public class DependencyOfDependencyDescriptionCustomizer implements ProjectDescriptionCustomizer {

    private final InitializrMetadataProvider provider;

    public DependencyOfDependencyDescriptionCustomizer(InitializrMetadataProvider dependencyMetadataProvider) {
        this.provider = dependencyMetadataProvider;
    }

    @Override
    public void customize(MutableProjectDescription description) {
        InitializrMetadata metadata = provider.get();

        DependenciesCapability allDependency = metadata.getDependencies();

        Set<String> ids = description.getRequestedDependencies().keySet();

        ids.stream().map(allDependency::get).forEach(dep -> this.appendSubDep(dep, description, allDependency));
    }

    private void appendSubDep(Dependency dependency, MutableProjectDescription description, DependenciesCapability allDependency) {

        if (!description.getRequestedDependencies().containsKey(dependency.getId())) {
            description.addDependency(dependency.getId(), MetadataBuildItemMapper.toDependency(dependency));
        }

        if (!(dependency instanceof EnhancedDependency edep)) {
            return;
        }

        List<String> subDeps = edep.getDependencies();

        if (CollectionUtils.isEmpty(subDeps)) {
            return;
        }

        subDeps.stream().map(allDependency::get).forEach(dep -> this.appendSubDep(dep, description, allDependency));
    }

}
