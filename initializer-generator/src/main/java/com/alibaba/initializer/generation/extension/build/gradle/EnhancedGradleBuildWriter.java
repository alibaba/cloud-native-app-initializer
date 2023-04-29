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
import io.spring.initializr.generator.buildsystem.Dependency;
import io.spring.initializr.generator.buildsystem.DependencyContainer;
import io.spring.initializr.generator.buildsystem.DependencyScope;
import io.spring.initializr.generator.buildsystem.gradle.GradleBuild;
import io.spring.initializr.generator.buildsystem.gradle.GradleBuildSettings;
import io.spring.initializr.generator.buildsystem.gradle.GradleBuildWriter;
import io.spring.initializr.generator.io.IndentingWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Extension GradleBuildWriter to support multi-project
 *
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 * @see GradleBuildWriter
 */
public class EnhancedGradleBuildWriter {

    @Autowired
    private GradleBuildWriter delegated;

    @Autowired
    private Module module;

    @Autowired
    private InitializerProjectDescription description;

    public void writeTo(IndentingWriter writer, GradleBuild build) {
        Architecture arch = description.getArchitecture();

        if (CollectionUtils.isEmpty(arch.getSubModules())) {
            delegated.writeTo(writer, build);
            return;
        }
//        if (GroovyLanguage.ID.equals(description.getLanguage().id())) {
//            if (!build.plugins().has("groovy")) {
//                build.plugins().add("groovy");
//            }
//        }

        GradleBuildSettings settings = build.getSettings();
        if (module.isRoot()) {
            run("writeImports", writer, build.tasks());
            run("writeBuildscript", writer, build);
            run("writePlugins", writer, build);
            run("writeConfigurations", writer, build.configurations());
            run("writeProperties", writer, build.properties());
            writer.println();

            writer.println("allprojects {");
            writer.indented(() -> {
                run("writeProperty", writer, "group", settings.getGroup());
                run("writeProperty", writer, "version", settings.getVersion());
                run("writeRepositories", writer, build);
            });
            writer.println("}");
            writer.println();

            writer.println("subprojects {");
            writer.indented(() -> {
                writeApplies(writer, build);
                run("writeJavaSourceCompatibility", writer, settings);
                run("writeBoms", writer, build);
                run("writeTasks", writer, build.tasks());
            });
            writer.println("}");
        } else {
            writeDependencies(writer, build);
        }
    }

    private void writeDependencies(IndentingWriter writer, GradleBuild build) {
        Set<Dependency> sortedDependencies = new LinkedHashSet<>();
        DependencyContainer dependencies = build.dependencies();
        sortedDependencies
                .addAll(filterDependencies(dependencies, (scope) -> scope == null || scope == DependencyScope.COMPILE));
        sortedDependencies.addAll(filterDependencies(dependencies, hasScope(DependencyScope.COMPILE_ONLY)));
        sortedDependencies.addAll(filterDependencies(dependencies, hasScope(DependencyScope.RUNTIME)));
        sortedDependencies.addAll(filterDependencies(dependencies, hasScope(DependencyScope.ANNOTATION_PROCESSOR)));
        sortedDependencies.addAll(filterDependencies(dependencies, hasScope(DependencyScope.PROVIDED_RUNTIME)));
        sortedDependencies.addAll(filterDependencies(dependencies, hasScope(DependencyScope.TEST_COMPILE)));
        sortedDependencies.addAll(filterDependencies(dependencies, hasScope(DependencyScope.TEST_RUNTIME)));

        List<String> modules = writeModuleDependencies();
        if (!sortedDependencies.isEmpty() || !modules.isEmpty()) {
            writer.println();
            writer.println("dependencies" + " {");
            writer.indented(() -> {
                modules.forEach(module -> writeModuleDependency(writer, module));
                sortedDependencies.forEach((dependency) -> writeDependency(writer, dependency));
            });
            writer.println("}");
        }
    }

    private void writeApplies(IndentingWriter writer, GradleBuild build) {
        build.plugins().values().forEach(plugin -> writer.println("apply plugin: '" + plugin.getId() + "'"));
    }

    private List<String> writeModuleDependencies() {
        List<String> modules = new ArrayList<>();
        Architecture arch = description.getArchitecture();

        if (module.isMain()) {
            // main module depend all other submodules
            List<Module> subModules = arch.getSubModules();
            for (Module subModule : subModules) {
                if (subModule == module) {
                    continue;
                }
                modules.add(subModule.getName());
            }
        } else {
            List<String> dependModules = module.getDependModules();
            if (dependModules != null) {
                modules.addAll(dependModules);
            }
        }
        return modules.stream().distinct().collect(Collectors.toList());
    }

    private void writeModuleDependency(IndentingWriter writer, String subModule) {
        writer.println(String.format("implementation project(':%s')", subModule));
    }

    protected void writeDependency(IndentingWriter writer, Dependency dependency) {
        run("writeDependency", writer, dependency);
    }

    private Collection<Dependency> filterDependencies(DependencyContainer dependencies,
                                                      Predicate<DependencyScope> filter) {
        return run("filterDependencies", dependencies, filter);
    }

    private Predicate<DependencyScope> hasScope(DependencyScope... validScopes) {
        return (scope) -> Arrays.asList(validScopes).contains(scope);
    }

    private <T> T run(String methodName, Object... args) {
        Method method = null;
        Class<?> clazz = delegated.getClass();
        for (; ; ) {
            Method[] methods = clazz.getDeclaredMethods();
            for (Method m : methods) {
                if (m.getName().equalsIgnoreCase(methodName)) {
                    method = m;
                    break;
                }
            }
            if (method != null) {
                break;
            }
            clazz = clazz.getSuperclass();
            if (clazz == Object.class) {
                break;
            }
        }

        if (method == null) {
            throw new RuntimeException("can't find method " + methodName);
        }

        try {
            method.setAccessible(true);
            return (T) method.invoke(delegated, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
