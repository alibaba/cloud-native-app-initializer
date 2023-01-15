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

import com.alibaba.initializer.generation.InitializerProjectGenerationConfiguration;
import com.alibaba.initializer.generation.condition.ConditionalOnArchitectured;
import com.alibaba.initializer.generation.condition.ConditionalOnModule;
import com.alibaba.initializer.generation.condition.ConditionalOnRequestedArchitecture;
import com.alibaba.initializer.generation.extension.architecture.layered.LayeredHelpCustomizer;
import com.alibaba.initializer.generation.extension.architecture.mvc.MvcHelpCustomizer;
import io.spring.initializr.generator.buildsystem.maven.MavenBuildSystem;
import io.spring.initializr.generator.condition.ConditionalOnBuildSystem;
import io.spring.initializr.generator.io.IndentingWriterFactory;
import io.spring.initializr.generator.io.template.TemplateRenderer;
import io.spring.initializr.generator.project.ProjectDescription;
import io.spring.initializr.metadata.InitializrMetadata;

import org.springframework.context.annotation.Bean;

/**
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 */
@InitializerProjectGenerationConfiguration
public class ArchitectureProjectGenerationConfiguration {

    private final InitializrMetadata metadata;

    private final ProjectDescription description;

    private final IndentingWriterFactory indentingWriterFactory;

    private final TemplateRenderer templateRenderer;

    public ArchitectureProjectGenerationConfiguration(InitializrMetadata metadata,
                                                      ProjectDescription description, IndentingWriterFactory indentingWriterFactory,
                                                      TemplateRenderer templateRenderer) {
        this.metadata = metadata;
        this.description = description;
        this.indentingWriterFactory = indentingWriterFactory;
        this.templateRenderer = templateRenderer;
    }

    @Bean
    @ConditionalOnArchitectured
    @ConditionalOnModule(root = true)
    public MulitModuleBuildCustomizer mulitModuleBuildCustomizer() {
        return new MulitModuleBuildCustomizer();
    }

    @Bean
    @ConditionalOnArchitectured
    public ArchitectureBuildCustomizer architectureBuildCustomizer() {
        return new ArchitectureBuildCustomizer();
    }

    @Bean
    @ConditionalOnRequestedArchitecture("layered")
    public LayeredHelpCustomizer layeredHelpCustomizer() {
        return new LayeredHelpCustomizer();
    }

    @Bean
    @ConditionalOnRequestedArchitecture("mvc")
    public MvcHelpCustomizer mvcHelpCustomizer() {
        return new MvcHelpCustomizer();
    }

    @Bean
    @ConditionalOnArchitectured
    @ConditionalOnBuildSystem(MavenBuildSystem.ID)
    public MavenArchitectureModuleContributor mavenArchitectureModuleContributir() {
        return new MavenArchitectureModuleContributor();
    }

    @Bean
    @ConditionalOnArchitectured
    public ModuleDescriptionContributor moduleDescriptionContributor() {
        return new ModuleDescriptionContributor();
    }
}
