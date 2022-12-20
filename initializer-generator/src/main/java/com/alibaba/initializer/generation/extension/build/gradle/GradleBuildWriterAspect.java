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

import io.spring.initializr.generator.buildsystem.Dependency;
import io.spring.initializr.generator.io.IndentingWriter;
import io.spring.initializr.generator.project.ProjectDescription;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 * @see io.spring.initializr.generator.buildsystem.gradle.GradleBuildWriter
 */
@Aspect
public class GradleBuildWriterAspect {

    @Autowired
    private ProjectDescription description;

    @Around("target(io.spring.initializr.generator.buildsystem.gradle.GradleBuildWriter+) && execution(* *.writeDependency(..))")
    public void writeDependency(ProceedingJoinPoint joinPoint) throws Throwable {

        IndentingWriter writer = (IndentingWriter) joinPoint.getArgs()[0];
        Dependency dependency = (Dependency) joinPoint.getArgs()[1];

        if ("submodule".equalsIgnoreCase(dependency.getType())) {
            writer.println(String.format("implementation project(':%s')", dependency.getArtifactId()));
        } else {
            joinPoint.proceed();
        }
    }

}