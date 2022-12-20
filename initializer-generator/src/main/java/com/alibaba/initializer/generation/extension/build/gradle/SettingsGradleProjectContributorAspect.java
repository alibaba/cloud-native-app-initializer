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

import com.alibaba.initializer.metadata.Architecture;
import com.alibaba.initializer.metadata.Module;
import com.alibaba.initializer.project.InitializerProjectDescription;
import io.spring.initializr.generator.buildsystem.gradle.GradleBuild;
import io.spring.initializr.generator.buildsystem.gradle.GradleSettingsWriter;
import io.spring.initializr.generator.io.IndentingWriter;
import io.spring.initializr.generator.io.IndentingWriterFactory;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 */
@Aspect
public class SettingsGradleProjectContributorAspect {

    @Autowired
    private Module module;

    @Autowired
    protected InitializerProjectDescription description;

    @Around("execution(* io.spring.initializr.generator.spring.build.gradle.SettingsGradleProjectContributor.contribute(..))")
    public void around(JoinPoint joinPoint) {
        Object target = joinPoint.getTarget();

        try {
            Path projectRoot = (Path) joinPoint.getArgs()[0];

            Field settingsWriterField = target.getClass().getDeclaredField("settingsWriter");
            Field buildField = target.getClass().getDeclaredField("build");
            Field indentingWriterFactoryField = target.getClass().getDeclaredField("indentingWriterFactory");
            Field settingsFileNameField = target.getClass().getDeclaredField("settingsFileName");

            settingsWriterField.setAccessible(true);
            buildField.setAccessible(true);
            indentingWriterFactoryField.setAccessible(true);
            settingsFileNameField.setAccessible(true);

            GradleSettingsWriter originSettingsWriter = (GradleSettingsWriter) settingsWriterField.get(target);
            GradleBuild build = (GradleBuild) buildField.get(target);
            IndentingWriterFactory indentingWriterFactory = (IndentingWriterFactory) indentingWriterFactoryField.get(target);
            String settingsFileName = (String) settingsFileNameField.get(target);

            EnhancedGradleSettingsWriter enhancedSettingsWriter = new EnhancedGradleSettingsWriter(originSettingsWriter, module, description);

            Architecture arch = description.getArchitecture();

            if (CollectionUtils.isEmpty(arch.getSubModules()) || module.isRoot()) {
                Path settingsGradle = Files.createFile(projectRoot.resolve(settingsFileName));
                try (IndentingWriter writer = indentingWriterFactory.createIndentingWriter("gradle",
                        Files.newBufferedWriter(settingsGradle))) {
                    enhancedSettingsWriter.writeTo(writer, build);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
