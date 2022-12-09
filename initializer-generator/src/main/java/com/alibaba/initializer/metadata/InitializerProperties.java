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

package com.alibaba.initializer.metadata;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.spring.initializr.metadata.DefaultMetadataElement;
import io.spring.initializr.metadata.InitializrConfiguration;
import io.spring.initializr.metadata.InitializrProperties;
import io.spring.initializr.metadata.Type;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 * @see InitializrProperties
 */
@ConfigurationProperties(prefix = "initializr")
public class InitializerProperties extends InitializrConfiguration {

    /**
     * Available application architecture.
     */
    @JsonIgnore
    private final List<Architecture> architecture = new ArrayList<>();

    /**
     * Dependencies, organized in groups (i.e. themes).
     */
    @JsonIgnore
    private final List<EnhancedDependencyGroup> dependencies = new ArrayList<>();

    /**
     * Available project types.
     */
    @JsonIgnore
    private final List<Type> types = new ArrayList<>();

    /**
     * Available packaging types.
     */
    @JsonIgnore
    private final List<DefaultMetadataElement> packagings = new ArrayList<>();

    /**
     * Available java versions.
     */
    @JsonIgnore
    private final List<DefaultMetadataElement> javaVersions = new ArrayList<>();

    /**
     * Available programming languages.
     */
    @JsonIgnore
    private final List<DefaultMetadataElement> languages = new ArrayList<>();

    /**
     * Available Spring Boot versions.
     */
    @JsonIgnore
    private final List<DefaultMetadataElement> bootVersions = new ArrayList<>();

    /**
     * GroupId metadata.
     */
    @JsonIgnore
    private final InitializrProperties.SimpleElement groupId = new InitializrProperties.SimpleElement("com.example");

    /**
     * ArtifactId metadata.
     */
    @JsonIgnore
    private final InitializrProperties.SimpleElement artifactId = new InitializrProperties.SimpleElement(null);

    /**
     * Version metadata.
     */
    @JsonIgnore
    private final InitializrProperties.SimpleElement version = new InitializrProperties.SimpleElement("0.0.1-SNAPSHOT");

    /**
     * Name metadata.
     */
    @JsonIgnore
    private final InitializrProperties.SimpleElement name = new InitializrProperties.SimpleElement("demo");

    /**
     * Description metadata.
     */
    @JsonIgnore
    private final InitializrProperties.SimpleElement description = new InitializrProperties.SimpleElement("Demo project for Spring Boot");

    /**
     * Package name metadata.
     */
    @JsonIgnore
    private final InitializrProperties.SimpleElement packageName = new InitializrProperties.SimpleElement(null);

    public List<Architecture> getArchitecture() {
        return architecture;
    }

    public List<EnhancedDependencyGroup> getDependencies() {
        return dependencies;
    }

    public List<Type> getTypes() {
        return this.types;
    }

    public List<DefaultMetadataElement> getPackagings() {
        return this.packagings;
    }

    public List<DefaultMetadataElement> getJavaVersions() {
        return this.javaVersions;
    }

    public List<DefaultMetadataElement> getLanguages() {
        return this.languages;
    }

    public List<DefaultMetadataElement> getBootVersions() {
        return this.bootVersions;
    }

    public InitializrProperties.SimpleElement getGroupId() {
        return this.groupId;
    }

    public InitializrProperties.SimpleElement getArtifactId() {
        return this.artifactId;
    }

    public InitializrProperties.SimpleElement getVersion() {
        return this.version;
    }

    public InitializrProperties.SimpleElement getName() {
        return this.name;
    }

    public InitializrProperties.SimpleElement getDescription() {
        return this.description;
    }

    public InitializrProperties.SimpleElement getPackageName() {
        return this.packageName;
    }
}
