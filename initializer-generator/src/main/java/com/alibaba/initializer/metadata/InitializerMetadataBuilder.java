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

package com.alibaba.initializer.metadata;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.spring.initializr.metadata.DependencyGroup;
import io.spring.initializr.metadata.InitializrMetadata;
import io.spring.initializr.metadata.InitializrMetadataCustomizer;

import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 * @see io.spring.initializr.metadata.InitializrMetadataBuilder
 */
public class InitializerMetadataBuilder {

    private final List<InitializrMetadataCustomizer> customizers = new ArrayList<>();

    private final InitializerProperties properties;

    private InitializerMetadataBuilder(InitializerProperties properties) {
        this.properties = properties;
    }

    public InitializerMetadataBuilder withInitializrProperties(InitializerProperties properties) {
        return withInitializrProperties(properties, false);
    }

    public InitializerMetadataBuilder withInitializrProperties(InitializerProperties properties, boolean mergeConfiguration) {
        if (mergeConfiguration) {
            this.properties.merge(properties);
        }
        return withCustomizer(new InitializerPropertiesCustomizer(properties));
    }

    public InitializerMetadataBuilder withCustomizer(InitializrMetadataCustomizer customizer) {
        this.customizers.add(customizer);
        return this;
    }

    public static InitializerMetadataBuilder fromInitializrProperties(InitializerProperties properties) {
        return new InitializerMetadataBuilder(properties).withInitializrProperties(properties);
    }

    public InitializerMetadata build() {
        InitializerProperties config = (this.properties != null) ? this.properties : new InitializerProperties();
        InitializerMetadata metadata = createInstance(config);
        for (InitializrMetadataCustomizer customizer : this.customizers) {
            customizer.customize(metadata);
        }
        applyDefaults(metadata);
        metadata.validate();
        return metadata;
    }

    private InitializerMetadata createInstance(InitializerProperties configuration) {
        return new InitializerMetadata(configuration);
    }

    protected void applyDefaults(InitializerMetadata metadata) {
        if (!StringUtils.hasText(metadata.getName().getContent())) {
            metadata.getName().setContent("demo");
        }
        if (!StringUtils.hasText(metadata.getDescription().getContent())) {
            metadata.getDescription().setContent("Demo Project");
        }
        if (!StringUtils.hasText(metadata.getGroupId().getContent())) {
            metadata.getGroupId().setContent("com.example");
        }
        if (!StringUtils.hasText(metadata.getVersion().getContent())) {
            metadata.getVersion().setContent("0.0.1-SNAPSHOT");
        }
        if (CollectionUtils.isEmpty(metadata.getArchitecture().getContent())) {
            metadata.getArchitecture().getContent().add(new Architecture("none", "None", null));
        }
    }

    private static class InitializerPropertiesCustomizer implements InitializrMetadataCustomizer {

        private final InitializerProperties properties;

        InitializerPropertiesCustomizer(InitializerProperties properties) {
            this.properties = properties;
        }

        @Override
        public void customize(InitializrMetadata metadata) {
            InitializerMetadata initializerMetadata = (InitializerMetadata) metadata;
            initializerMetadata.getArchitecture().merge(this.properties.getArchitecture());
            initializerMetadata.getDependencies().merge(new ArrayList<>(this.properties.getDependencies()));
            metadata.getTypes().merge(this.properties.getTypes());
            metadata.getBootVersions().merge(this.properties.getBootVersions());
            metadata.getPackagings().merge(this.properties.getPackagings());
            metadata.getJavaVersions().merge(this.properties.getJavaVersions());
            metadata.getLanguages().merge(this.properties.getLanguages());
            this.properties.getGroupId().apply(metadata.getGroupId());
            this.properties.getArtifactId().apply(metadata.getArtifactId());
            this.properties.getVersion().apply(metadata.getVersion());
            this.properties.getName().apply(metadata.getName());
            this.properties.getDescription().apply(metadata.getDescription());
            this.properties.getPackageName().apply(metadata.getPackageName());
        }

    }
}
