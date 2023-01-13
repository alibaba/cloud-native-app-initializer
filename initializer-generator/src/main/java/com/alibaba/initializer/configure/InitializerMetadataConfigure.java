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

package com.alibaba.initializer.configure;

import java.io.IOException;
import java.nio.file.Files;

import com.alibaba.initializer.metadata.InitializerMetadata;
import com.alibaba.initializer.metadata.InitializerMetadataBuilder;
import com.alibaba.initializer.metadata.InitializerProperties;
import io.spring.initializr.metadata.InitializrMetadata;
import io.spring.initializr.metadata.InitializrMetadataProvider;
import io.spring.initializr.versionresolver.DependencyManagementVersionResolver;
import io.spring.initializr.web.support.DefaultInitializrMetadataProvider;
import io.spring.start.site.support.CacheableDependencyManagementVersionResolver;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.ResourceLoader;

/**
 * Initializr configure, overwrite default configure
 *
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 * @see io.spring.initializr.web.autoconfigure.InitializrAutoConfiguration
 */
@Configuration
@PropertySource(value = "${application.metadata-path:classpath:metadata.yaml}", factory = YamlPropertiesSourceFactory.class)
@EnableConfigurationProperties({InitializerProperties.class})
public class InitializerMetadataConfigure {

    @Bean
    public DependencyManagementVersionResolver dependencyManagementVersionResolver()
            throws IOException {
        return new CacheableDependencyManagementVersionResolver(
                DependencyManagementVersionResolver.withCacheLocation(Files.createTempDirectory("version-resolver-cache-")));
    }

    @Bean
    public InitializrMetadataProvider initializrMetadataProvider(InitializerProperties properties, ResourceLoader resourceLoader) {

        InitializerMetadata metadata = InitializerMetadataBuilder.fromInitializrProperties(properties).build();

        return new DefaultInitializrMetadataProvider(metadata, (InitializrMetadata meta) -> meta);
    }

}
