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

import java.util.List;
import java.util.stream.Collectors;

import com.alibaba.initializer.generation.condition.ConditionalOnArchitectured;
import com.alibaba.initializer.generation.condition.ConditionalOnModule;
import io.spring.initializr.generator.buildsystem.BuildItemResolver;
import io.spring.initializr.generator.buildsystem.maven.MavenBuild;
import io.spring.initializr.generator.buildsystem.maven.MavenBuildSystem;
import io.spring.initializr.generator.condition.ConditionalOnBuildSystem;
import io.spring.initializr.generator.condition.ConditionalOnPackaging;
import io.spring.initializr.generator.io.IndentingWriterFactory;
import io.spring.initializr.generator.packaging.war.WarPackaging;
import io.spring.initializr.generator.project.ProjectDescription;
import io.spring.initializr.generator.project.ProjectGenerationConfiguration;
import io.spring.initializr.generator.spring.build.BuildCustomizer;
import io.spring.initializr.generator.spring.build.maven.MavenBuildProjectContributor;
import io.spring.initializr.generator.spring.util.LambdaSafe;
import io.spring.initializr.metadata.InitializrMetadata;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 */
@ProjectGenerationConfiguration
@ConditionalOnBuildSystem(MavenBuildSystem.ID)
public class ArchedMavenProjectGenerationConfiguration {

    @Bean
    public MavenBuild mavenBuild(ObjectProvider<BuildItemResolver> buildItemResolver,
                                 ObjectProvider<BuildCustomizer<?>> buildCustomizers) {
        return createBuild(buildItemResolver.getIfAvailable(),
                buildCustomizers.orderedStream().collect(Collectors.toList()));
    }

    @SuppressWarnings("unchecked")
    private MavenBuild createBuild(BuildItemResolver buildItemResolver, List<BuildCustomizer<?>> buildCustomizers) {
        MavenBuild build = (buildItemResolver != null) ? new MavenBuild(buildItemResolver) : new MavenBuild();
        LambdaSafe.callbacks(BuildCustomizer.class, buildCustomizers, build)
                .invoke((customizer) -> customizer.customize(build));
        return build;
    }

    @Bean
    public SpringBootBomMavenCustomizer springBootBomMavenCustomizer(
            ProjectDescription description, InitializrMetadata metadata) {
        return new SpringBootBomMavenCustomizer(description, metadata);
    }

    @Bean
    public MavenComplierPluginCustomizer mavenComplierPluginCustomizer(
            ProjectDescription description) {
        return new MavenComplierPluginCustomizer(description);
    }

    @Bean
    @ConditionalOnPackaging(WarPackaging.ID)
    @ConditionalOnModule(main = true)
    public BuildCustomizer<MavenBuild> mavenWarPackagingConfigurer() {
        return (build) -> build.settings().packaging("war");
    }

    @Bean
    public MulitModuleMavenBuildProjectContributor mulitModuleavenBuildProjectContributor(
            MavenBuild build, IndentingWriterFactory indentingWriterFactory,InitializrMetadata metadata) {
        return new MulitModuleMavenBuildProjectContributor(build, indentingWriterFactory, metadata);
    }

}
