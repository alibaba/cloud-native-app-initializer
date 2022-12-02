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

package com.alibaba.initializer.generation.extension.build.maven;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.alibaba.initializer.generation.extension.build.DependencyBillOfMaterials;
import com.alibaba.initializer.metadata.Architecture;
import com.alibaba.initializer.metadata.Module;
import com.alibaba.initializer.project.InitializerProjectDescription;
import io.spring.initializr.generator.buildsystem.Dependency;
import io.spring.initializr.generator.buildsystem.PropertyContainer;
import io.spring.initializr.generator.buildsystem.maven.MavenBuild;
import io.spring.initializr.generator.io.IndentingWriter;
import io.spring.initializr.generator.io.IndentingWriterFactory;
import io.spring.initializr.generator.spring.build.maven.MavenBuildProjectContributor;
import io.spring.initializr.generator.version.VersionProperty;
import io.spring.initializr.generator.version.VersionReference;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 */
@Slf4j
public class MulitModuleMavenBuildProjectContributor extends MavenBuildProjectContributor {

    private final MavenBuild build;

    private final IndentingWriterFactory indentingWriterFactory;

    private final MulitModuleMavenBuildWriter buildWriter;

    public MulitModuleMavenBuildProjectContributor(MavenBuild build, IndentingWriterFactory indentingWriterFactory) {
        super(build, indentingWriterFactory);
        this.build = build;
        this.indentingWriterFactory = indentingWriterFactory;
        this.buildWriter = new MulitModuleMavenBuildWriter();

        // use reflext to replace MavenBuildWriter
        try {
            Field buildWriterField = MavenBuildProjectContributor.class.getDeclaredField("buildWriter");
            buildWriterField.setAccessible(true);
            buildWriterField.set(this, this.buildWriter);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Autowired
    protected InitializerProjectDescription description;

    @Autowired
    private Module module;

    @Override
    public void contribute(Path projectRoot) throws IOException {

        Architecture arch = description.getArchitecture();

        if (module.isRoot()) {

            // add spring boot plugin
            this.build.plugins().remove("org.springframework.boot", "spring-boot-maven-plugin");
            this.build.plugins().add("org.springframework.boot", "spring-boot-maven-plugin", builder -> {
                builder.version("${spring-boot.version}");//这个要用spring version么？
                builder.execution("repackage", execution -> execution.goal("repackage"));
                builder.configuration(conf -> conf.add("mainClass", description.getPackageName() + "." + description.getApplicationName()).add("skip", "true"));
            });

            // add submodule in root dependencymanager
            for (Module subModule : arch.getSubModules()) {
                this.build.boms().add(subModule.getName(),
                        DependencyBillOfMaterials
                                .withCoordinates(description.getGroupId(), subModule.getName())
                                .type(null)
                                .scope(null)
                                .version(VersionReference.ofValue(description.getVersion()))
                );
            }

            // create new pom.xml file
            super.contribute(projectRoot);

            // insert <modules /> to pom.xml
            List<Module> modules = description.getArchitecture().getSubModules();
            Path pomFile = projectRoot.resolve("pom.xml");
            try (RandomAccessFile rw = new RandomAccessFile(pomFile.toFile(), "rw")) {
                StringBuffer rewrited = new StringBuffer();
                //not utf-8 but ISO-8859-1 encodng
                String rawLine;
                int index = -1;
                int insetIndex = findModulesInsertLine(pomFile);
                while ((rawLine = rw.readLine()) != null) {
                    index++;

                    //trans encoding style
                    String line = new String(rawLine.getBytes("ISO-8859-1"), "UTF-8");
                    rewrited.append(line).append("\n");

                    if (index == insetIndex) {
                        StringWriter sw = new StringWriter();
                        IndentingWriter writer = indentingWriterFactory
                                .createIndentingWriter("maven", sw);

                        writer.println();
                        writer.indented(() -> {
                            writer.println("<modules>");
                            writer.indented(() -> modules.stream()
                                    .map(name -> "<module>" + name.getName() + "</module>")
                                    .forEach(writer::println));
                            writer.println("</modules>\n");
                        });

                        rewrited.append(sw);
                    }
                }
                rw.seek(0);
                rw.write(rewrited.toString().getBytes());
            }
        } else {

            if (module.isMain()) {
                // main module depend all other submodules
                List<Module> subModules = arch.getSubModules();
                for (Module subModule : subModules) {
                    if (subModule == module) {
                        continue;
                    }
                    addModuleDependency(subModule.getName());
                }
            } else {
                List<String> dependModules = module.getDependModules();
                if (dependModules != null) {
                    for (String dependModule : dependModules) {
                        addModuleDependency(dependModule);
                    }
                }
            }

            // remove all dependencymanager
            this.build.boms().ids().collect(Collectors.toList()).forEach(id -> this.build.boms().remove(id));

            // set parent
            this.build.settings().parent(description.getGroupId(), description.getArtifactId(), description.getVersion(), "../pom.xml");
            this.build.settings().name(module.getName());
            this.build.settings().artifact(module.getName());
            this.build.settings().group(null);
            this.build.settings().version(null);
            this.build.settings().description(module.getDescription());
            this.cleanupProperties();

            super.contribute(projectRoot);
        }
    }

    private void cleanupProperties() {
        try {
            PropertyContainer propertyContainer = this.build.properties();

            Field propertiesField = PropertyContainer.class.getDeclaredField("properties");
            propertiesField.setAccessible(true);
            Map<String, String> properties = (Map<String, String>) propertiesField.get(propertyContainer);
            properties.clear();

            Field versionsField = PropertyContainer.class.getDeclaredField("versions");
            versionsField.setAccessible(true);
            Map<VersionProperty, String> versions = (Map<VersionProperty, String>) versionsField.get(propertyContainer);
            versions.clear();

        } catch (Exception e) {
            throw new RuntimeException("cleanup maven properties failed", e);
        }
    }

    private void addModuleDependency(String subModule) {
        this.build.dependencies().add(subModule, Dependency.withCoordinates(description.getGroupId(), subModule));
    }

    private int findModulesInsertLine(Path pomFile) throws IOException {
        int index = -1;
        int insertIndex = -1;
        String rawLine;
        try (RandomAccessFile rw = new RandomAccessFile(pomFile.toFile(), "r")) {
            while ((rawLine = rw.readLine()) != null) {
                index++;

                //trans encoding style
                String line = new String(rawLine.getBytes("ISO-8859-1"), "UTF-8");

                if (line.endsWith("</packaging>")) {
                    insertIndex = index;
                } else if (line.endsWith("</properties>")) {
                    insertIndex = index;
                }
            }
        }
        return insertIndex;
    }

}
