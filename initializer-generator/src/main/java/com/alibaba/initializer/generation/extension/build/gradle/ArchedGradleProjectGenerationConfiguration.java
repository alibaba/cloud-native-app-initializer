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
package com.alibaba.initializer.generation.extension.build.gradle;

import com.alibaba.initializer.generation.extension.build.ModuleDependencyBuildCustormizer;
import io.spring.initializr.generator.buildsystem.gradle.GradleBuildSystem;
import io.spring.initializr.generator.buildsystem.gradle.GradleBuildWriter;
import io.spring.initializr.generator.condition.ConditionalOnBuildSystem;
import io.spring.initializr.generator.io.IndentingWriterFactory;
import io.spring.initializr.generator.project.ProjectGenerationConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 */
@ProjectGenerationConfiguration
@ConditionalOnBuildSystem(GradleBuildSystem.ID)
@Import(io.spring.initializr.generator.spring.build.gradle.GradleProjectGenerationConfiguration.class)
public class ArchedGradleProjectGenerationConfiguration {

    private final IndentingWriterFactory indentingWriterFactory;

    public ArchedGradleProjectGenerationConfiguration(IndentingWriterFactory indentingWriterFactory) {
        this.indentingWriterFactory = indentingWriterFactory;
    }

    @Bean
    public ModuleDependencyBuildCustormizer moduleDependencyBuildCustormizer() {
        return new ModuleDependencyBuildCustormizer();
    }

    @Bean
    public SettingsGradleProjectContributorAspect gradleSettingsWriterAspect() {
        return new SettingsGradleProjectContributorAspect();
    }

    @Bean
    public GradleWrapperContributorAspect gradleWrapperContributorAspect() {
        return new GradleWrapperContributorAspect();
    }

    @Bean
    public MulitModuleGradleBuildCustoimzer mulitModuleGradleBuildCustoimzer() {
        return new MulitModuleGradleBuildCustoimzer();
    }

    // overwrite GradleBuildProjectContributor from GradleProjectGenerationConfiguration
    @Bean
    public GradleBuildProjectContributorAspect gradleBuildProjectContributorAspect() {
        return new GradleBuildProjectContributorAspect();
    }

    @Bean
    public EnhancedGradleBuildWriter enhancedGradleBuildWriterDelegate() {
        return new EnhancedGradleBuildWriter();
    }

    // overwrite GradleBuild from GradleProjectGenerationConfiguration
//    @Bean
//    @Primary
//    public GradleBuild gradleBuild(ObjectProvider<BuildItemResolver> buildItemResolver,
//                                   ObjectProvider<BuildCustomizer<?>> buildCustomizers) {
//        return createGradleBuild(buildItemResolver.getIfAvailable(), buildCustomizers.orderedStream().collect(Collectors.toList()));
//    }

//    @SuppressWarnings("unchecked")
//    private GradleBuild createGradleBuild(BuildItemResolver buildItemResolver,
//                                          List<BuildCustomizer<?>> buildCustomizers) {
//        EnhancedGradleBuild build = (buildItemResolver != null) ? new EnhancedGradleBuild(buildItemResolver) : new EnhancedGradleBuild();
//        LambdaSafe.callbacks(BuildCustomizer.class, buildCustomizers, build)
//                .invoke((customizer) -> customizer.customize(build));
//        return build;
//    }
}
