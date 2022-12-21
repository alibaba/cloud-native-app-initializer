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

import io.spring.initializr.generator.buildsystem.gradle.GradleBuildSystem;
import io.spring.initializr.generator.condition.ConditionalOnBuildSystem;
import io.spring.initializr.generator.condition.ConditionalOnLanguage;
import io.spring.initializr.generator.language.groovy.GroovyLanguage;
import io.spring.initializr.generator.project.ProjectDescription;
import io.spring.initializr.generator.project.ProjectGenerationConfiguration;
import io.spring.initializr.generator.version.VersionParser;
import io.spring.initializr.generator.version.VersionRange;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

/**
 * Beacuse of GroovyGradleBuildCustomizer GroovyDependenciesConfigurer is packaged modifier.
 *
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 */
@ConditionalOnLanguage(GroovyLanguage.ID)
@ConditionalOnBuildSystem(GradleBuildSystem.ID)
@Import({
        GroovyCustomizerConfiguration.GroovyGradleBuildCustomizer.class,
        GroovyCustomizerConfiguration.GroovyDependenciesConfigurer.class
})
@ProjectGenerationConfiguration
public class GroovyCustomizerConfiguration {

    private static final VersionRange GROOVY4 = VersionParser.DEFAULT.parseRange("3.0.0-M2");

    static class GroovyGradleBuildCustomizer implements ImportBeanDefinitionRegistrar {
        @Override
        public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
            AbstractBeanDefinition beanDefinition = BeanDefinitionBuilder
                    .rootBeanDefinition("io.spring.initializr.generator.spring.code.groovy.GroovyGradleBuildCustomizer")
                    .getBeanDefinition();
            registry.registerBeanDefinition("groovyGradleBuildCustomizer", beanDefinition);
        }
    }

    static class GroovyDependenciesConfigurer implements ImportBeanDefinitionRegistrar {

        @Override
        public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
            ProjectDescription description = ((DefaultListableBeanFactory) registry).getBean(ProjectDescription.class);
            AbstractBeanDefinition def = BeanDefinitionBuilder
                    .rootBeanDefinition("io.spring.initializr.generator.spring.code.groovy.GroovyDependenciesConfigurer")
                    .addConstructorArgValue(GROOVY4.match(description.getPlatformVersion()))
                    .getBeanDefinition();
            registry.registerBeanDefinition("groovyDependenciesConfigurer", def);
        }
    }
}
