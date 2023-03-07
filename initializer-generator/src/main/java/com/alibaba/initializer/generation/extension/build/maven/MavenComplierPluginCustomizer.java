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

import io.spring.initializr.generator.buildsystem.maven.MavenBuild;
import io.spring.initializr.generator.project.ProjectDescription;
import io.spring.initializr.generator.spring.build.BuildCustomizer;

/**
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 */
public class MavenComplierPluginCustomizer implements BuildCustomizer<MavenBuild> {

    private final ProjectDescription description;

    public MavenComplierPluginCustomizer(ProjectDescription description) {
        this.description = description;
    }

    @Override
    public void customize(MavenBuild build) {
        String javaVersion = description.getLanguage().jvmVersion();
        build.plugins().add("org.apache.maven.plugins", "maven-compiler-plugin",
                builder -> {
                    builder.configuration(configuration -> {
                                configuration.add("source", javaVersion);
                                configuration.add("target", javaVersion);
                                configuration.add("encoding", "UTF-8");
                            }

                    );
                    builder.version("3.8.1");
                }
        );
    }
}
