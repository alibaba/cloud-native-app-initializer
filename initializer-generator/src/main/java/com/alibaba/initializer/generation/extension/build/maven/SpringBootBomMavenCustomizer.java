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

package com.alibaba.initializer.generation.extension.build.maven;

import java.lang.reflect.Field;

import io.spring.initializr.generator.buildsystem.BillOfMaterials;
import io.spring.initializr.generator.buildsystem.maven.MavenBuild;
import io.spring.initializr.generator.project.ProjectDescription;
import io.spring.initializr.generator.spring.build.BuildCustomizer;
import io.spring.initializr.metadata.InitializrConfiguration;
import io.spring.initializr.metadata.InitializrMetadata;

/**
 * after DefaultMavenBuildCustomizer,
 * <pre>
 * remove parent node from pom.xml
 * add version property to plugin spring-boot-maven-plugin
 * </pre>
 *
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 * @see io.spring.initializr.generator.spring.build.maven.DefaultMavenBuildCustomizer
 */
public class SpringBootBomMavenCustomizer implements BuildCustomizer<MavenBuild> {

    private final ProjectDescription description;

    private final InitializrMetadata metadata;

    public SpringBootBomMavenCustomizer(ProjectDescription description, InitializrMetadata metadata) {
        this.description = description;
        this.metadata = metadata;
    }

    @Override
    public void customize(MavenBuild build) {
        InitializrConfiguration.Env.Maven maven = this.metadata.getConfiguration().getEnv().getMaven();
        String springBootVersion = this.description.getPlatformVersion().toString();
        InitializrConfiguration.Env.Maven.ParentPom parentPom = maven.resolveParentPom(springBootVersion);

        if (!build.plugins().has("org.springframework.boot", "spring-boot-maven-plugin")) {
            return;
       }
        if (!parentPom.isIncludeSpringBootBom()) {
            return;
        }

        // ugly !!!!!!
        try {
            Field parendField = build.settings().getClass().getDeclaredField("parent");
            parendField.setAccessible(true);
            parendField.set(build.settings(), null);
        } catch (Exception e) {
            throw new IllegalStateException("remove parent node error", e);
        }

        build.plugins().remove("org.springframework.boot", "spring-boot-maven-plugin");
        build.plugins().add("org.springframework.boot", "spring-boot-maven-plugin", builder -> {
                    builder.version(springBootVersion);
                    builder.execution("repackage", execution -> execution.goal("repackage"));
                    builder.configuration(conf -> conf.add("mainClass", description.getPackageName() + "." + description.getApplicationName()));
                }
        );
    }


    private boolean hasBom(MavenBuild build, BillOfMaterials bom) {
        return build.boms().items().anyMatch((candidate) -> candidate.getGroupId().equals(bom.getGroupId())
                && candidate.getArtifactId().equals(bom.getArtifactId()));
    }

    @Override
    public int getOrder() {
        return 1;
    }
}
