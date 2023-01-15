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

import java.util.List;

import com.alibaba.initializer.generation.extension.build.DependencyBillOfMaterials;
import com.alibaba.initializer.metadata.InitializerMetadata;
import com.alibaba.initializer.project.InitializerProjectDescription;
import io.spring.initializr.generator.buildsystem.Build;
import io.spring.initializr.generator.spring.build.BuildCustomizer;
import io.spring.initializr.generator.version.VersionReference;
import io.spring.initializr.metadata.BillOfMaterials;
import io.spring.initializr.metadata.Dependency;
import io.spring.initializr.metadata.InitializrMetadataProvider;
import io.spring.initializr.metadata.support.MetadataBuildItemMapper;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 */
public class ArchitectureBuildCustomizer implements BuildCustomizer<Build> {

    @Autowired
    private InitializrMetadataProvider metadataProvider;

    @Autowired
    private InitializerProjectDescription initializerProjectDescription;

    @Override
    public void customize(Build build) {
        List<String> dependOn = initializerProjectDescription.getArchitecture().getRequiredDependency();

        if (dependOn == null) {
            return;
        }
        InitializerMetadata metadata = (InitializerMetadata) metadataProvider.get();

        dependOn.forEach(id -> {
            Dependency dependency = metadata.getDependencies().get(id);
            if (dependency == null) {
                BillOfMaterials bom = metadata.getConfiguration().getEnv().getBoms()
                        .get(id).resolve(initializerProjectDescription.getPlatformVersion());
                build.boms().add(id, MetadataBuildItemMapper.toBom(bom));
            } else if (dependency.getVersion() != null) {
                io.spring.initializr.generator.buildsystem.BillOfMaterials dbom = DependencyBillOfMaterials
                        .withCoordinates(dependency.getGroupId(), dependency.getArtifactId())
                        .scope(null)
                        .type("jar")
                        .version(VersionReference.ofValue(dependency.getVersion()))
                        .build();
                build.boms().add(id, dbom);
            }
        });
    }
}
