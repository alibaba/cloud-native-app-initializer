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
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;

/**
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 */
public class EnhancedGradleSettingsWriter {

    private final GradleSettingsWriter writer;
    private final Module module;
    private final InitializerProjectDescription description;

    public EnhancedGradleSettingsWriter(GradleSettingsWriter writer, Module module, InitializerProjectDescription description) {
        this.writer = writer;
        this.module = module;
        this.description = description;
    }

    public void writeTo(IndentingWriter writer, GradleBuild build) {
        this.writer.writeTo(writer, build);

        Architecture arch = description.getArchitecture();
        if (!CollectionUtils.isEmpty(arch.getSubModules())) {
            arch.getSubModules().forEach(sub -> writer.println(String.format("include %s", wrapWithQuotes(sub.getName()))));
        }
    }

    protected String wrapWithQuotes(String value) {
        try {
            Method wrapWithQuotesMethod = writer.getClass().getDeclaredMethod("wrapWithQuotes", String.class);
            wrapWithQuotesMethod.setAccessible(true);
            return (String) wrapWithQuotesMethod.invoke(writer, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
