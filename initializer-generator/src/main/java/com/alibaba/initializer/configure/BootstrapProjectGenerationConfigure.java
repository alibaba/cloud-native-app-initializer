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

import com.alibaba.initializer.controller.InitializerProjectMetadataController;
import com.alibaba.initializer.controller.InitializerProjectRequestToDescriptionConverter;
import com.alibaba.initializer.core.template.CodeTemplateRepoRenderer;
import com.alibaba.initializer.core.template.TemplateStringRender;
import com.alibaba.initializer.core.template.loader.ClasspathTemplateLoader;
import com.alibaba.initializer.core.template.loader.FileTemplateLoader;
import com.alibaba.initializer.core.template.loader.LocalRepoTemplateLoader;
import com.alibaba.initializer.core.template.loader.RootRepoTemplateLoader;
import com.alibaba.initializer.generation.BootstrapProjectGenerator;
import com.alibaba.initializer.generation.InitializerProjectGenerationInvoker;
import com.alibaba.initializer.generation.extension.dependency.DependencyOfDependencyDescriptionCustomizer;
import com.alibaba.initializer.protocol.CodeGenerationProtocolFilter;
import com.alibaba.initializer.protocol.archive.ProjectArchiveHandler;
import com.alibaba.initializer.protocol.git.ProjectGenerationResolver;
import com.alibaba.initializer.protocol.resolver.DefaultRequestPathValve;
import com.alibaba.initializer.protocol.resolver.RequestConverter;
import com.google.common.collect.Maps;
import io.spring.initializr.generator.io.IndentingWriterFactory;
import io.spring.initializr.generator.io.SimpleIndentStrategy;
import io.spring.initializr.metadata.DependencyMetadataProvider;
import io.spring.initializr.metadata.InitializrMetadataProvider;
import io.spring.start.site.project.JavaVersionProjectDescriptionCustomizer;
import static java.util.concurrent.TimeUnit.SECONDS;
import org.eclipse.jgit.http.server.GitFilter;
import org.eclipse.jgit.transport.resolver.RepositoryResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.cache.JCacheManagerCustomizer;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.Map;

/**
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 */
@Configuration
public class BootstrapProjectGenerationConfigure {

    @Bean
    public InitializerProjectGenerationInvoker initializerProjectGenerationInvoker(ApplicationContext applicationContext) {
        InitializerProjectRequestToDescriptionConverter convertor = new InitializerProjectRequestToDescriptionConverter();
        InitializerProjectGenerationInvoker invoker = new InitializerProjectGenerationInvoker(applicationContext, convertor);
        return invoker;
    }

    @Bean
    public InitializerProjectMetadataController projectMetadataController(
            InitializrMetadataProvider metadataProvider, DependencyMetadataProvider dependencyMetadataProvider) {
        return new InitializerProjectMetadataController(metadataProvider, dependencyMetadataProvider);
    }

    @Bean
    public JavaVersionProjectDescriptionCustomizer javaVersionProjectDescriptionCustomizer() {
        return new JavaVersionProjectDescriptionCustomizer();
    }

    @Bean
    public IndentingWriterFactory indentingWriterFactory() {
        return IndentingWriterFactory.create(new SimpleIndentStrategy("    "));
    }


    @Bean
    public TemplateStringRender templateStringRender() {
        return new TemplateStringRender();
    }

    @Bean
    public ProjectArchiveHandler projectArchiveHandler() {
        return new ProjectArchiveHandler();
    }

    @Bean
    public FilterRegistrationBean<CodeGenerationProtocolFilter> codeGenerationRouteFilterRegistration(CodeGenerationProtocolFilter filter) {
        FilterRegistrationBean<CodeGenerationProtocolFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(filter);
        bean.addUrlPatterns("/*");
        bean.setOrder(filter.getOrder());
        return bean;
    }

    @Bean
    public GitFilter gitFilter(RepositoryResolver<HttpServletRequest> resolver) {
        Map<String, String> params = Maps.newLinkedHashMap();
        params.put("base-path", "/tmp/resp");
        params.put("export-all", "true");

        GitFilter filter = new GitFilter() {
            @Override
            public void init(FilterConfig filterConfig) throws ServletException {
                super.init(new FilterConfig() {
                    @Override
                    public String getFilterName() {
                        return GitFilter.class.getName();
                    }

                    @Override
                    public ServletContext getServletContext() {
                        return filterConfig.getServletContext();
                    }

                    @Override
                    public String getInitParameter(String name) {
                        if (params.containsKey(name)) {
                            return params.get(name);
                        }
                        return filterConfig.getInitParameter(name);
                    }

                    @Override
                    public Enumeration<String> getInitParameterNames() {
                        return filterConfig.getInitParameterNames();
                    }
                });
            }
        };
        filter.setRepositoryResolver(resolver);

        return filter;
    }

    @Bean
    public RepositoryResolver<HttpServletRequest> repositoryResolver() {
        return new ProjectGenerationResolver();
    }

    @Bean
    public CodeGenerationProtocolFilter codeGenerationRouteFilter(GitFilter gitFilter) {
        return new CodeGenerationProtocolFilter(filterConfig -> {
            try {
                gitFilter.init(filterConfig);
            } catch (ServletException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Bean
    public ClasspathTemplateLoader classpathTemplateLoader() {
        return new ClasspathTemplateLoader();
    }

    // only works on none-production environment
    @Bean
    @Profile("!production")
    public LocalRepoTemplateLoader localRepoTemplateLoader() {
        return new LocalRepoTemplateLoader();
    }

    @Bean
    public FileTemplateLoader fileLoader() {
        return new FileTemplateLoader();
    }

    @Bean("rootRepoTemplateLoader")
    public RootRepoTemplateLoader rootRepoTemplateLoader(ApplicationContext applicationContext) {
        return new RootRepoTemplateLoader(applicationContext);
    }

    @Bean
    public RequestConverter requestConverter() {
        return new RequestConverter();
    }

    @Bean("bootstrapProjectGenerator")
    public BootstrapProjectGenerator bootstrapProjectGenerator() {
        return new BootstrapProjectGenerator();
    }

    @Bean
    public DefaultRequestPathValve defaultRequestPathValve() {
        return new DefaultRequestPathValve();
    }

    @Bean
    public CodeTemplateRepoRenderer codeTemplateRepoRenderer() {
        return new CodeTemplateRepoRenderer();
    }

    @Bean("templateLoaderCacheCustomizer")
    public JCacheManagerCustomizer templateLoaderCacheCustomizerTest(@Value("${application.cache.repo.expTime.local}") Long expire) {
        Duration duration;
        if (expire == null || expire <= 0) {
            duration = Duration.ZERO;
        } else {
            duration = new Duration(SECONDS, expire);
        }

        return (cacheManager) -> cacheManager.createCache("initializr.template.repo", new MutableConfiguration<>()
                .setStoreByValue(false)
                .setManagementEnabled(true)
                .setStatisticsEnabled(true)
                .setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(duration)));
    }

    @Bean
    public DependencyOfDependencyDescriptionCustomizer dependencyOfDependencyDescriptionCustomizer(InitializrMetadataProvider metadataProvider) {
        return new DependencyOfDependencyDescriptionCustomizer(metadataProvider);
    }
}
