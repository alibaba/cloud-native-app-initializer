package com.alibaba.initializer.generation.extension.build.gradle;

import io.spring.initializr.generator.buildsystem.gradle.GradleBuildSystem;
import io.spring.initializr.generator.condition.ConditionalOnBuildSystem;
import io.spring.initializr.generator.project.ProjectGenerationConfiguration;

import org.springframework.context.annotation.Import;

/**
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 */
@ProjectGenerationConfiguration
@ConditionalOnBuildSystem(GradleBuildSystem.ID)
@Import(io.spring.initializr.generator.spring.build.gradle.GradleProjectGenerationConfiguration.class)
public class ArchedGradleProjectGenerationConfiguration {

}
