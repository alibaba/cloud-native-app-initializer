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

package com.alibaba.initializer.generation;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.initializer.controller.InitializerProjectRequestToDescriptionConverter;
import com.alibaba.initializer.controller.ProjectRequest;
import com.alibaba.initializer.metadata.Architecture;
import com.alibaba.initializer.metadata.InitializerMetadata;
import com.alibaba.initializer.metadata.Module;
import com.alibaba.initializer.project.InitializerProjectDescription;
import io.spring.initializr.generator.buildsystem.BuildItemResolver;
import io.spring.initializr.generator.buildsystem.BuildWriter;
import io.spring.initializr.generator.project.DefaultProjectAssetGenerator;
import io.spring.initializr.generator.project.MutableProjectDescription;
import io.spring.initializr.generator.project.ProjectAssetGenerator;
import io.spring.initializr.generator.project.ProjectDescription;
import io.spring.initializr.generator.project.ProjectDirectoryFactory;
import io.spring.initializr.generator.project.ProjectGenerationContext;
import io.spring.initializr.generator.project.ProjectGenerationException;
import io.spring.initializr.metadata.InitializrMetadata;
import io.spring.initializr.metadata.InitializrMetadataProvider;
import io.spring.initializr.metadata.support.MetadataBuildItemResolver;
import io.spring.initializr.web.project.MetadataProjectDescriptionCustomizer;
import io.spring.initializr.web.project.ProjectFailedEvent;
import io.spring.initializr.web.project.ProjectGeneratedEvent;
import io.spring.initializr.web.project.ProjectGenerationResult;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.util.FileSystemUtils;

/**
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 */
public class InitializerProjectGenerationInvoker
        extends io.spring.initializr.web.project.ProjectGenerationInvoker<ProjectRequest> {

    private final ApplicationContext parentAppCtx;
    private final ApplicationEventPublisher eventPublisher;
    private final InitializerProjectRequestToDescriptionConverter requestConverter;
    private transient Map<Path, List<Path>> temporaryFiles = new LinkedHashMap<>();

    public InitializerProjectGenerationInvoker(ApplicationContext parentApplicationContext,
                                          InitializerProjectRequestToDescriptionConverter requestConverter) {
        super(parentApplicationContext, requestConverter);
        this.parentAppCtx = parentApplicationContext;
        this.requestConverter = requestConverter;
        this.eventPublisher = parentApplicationContext;
    }

    /**
     * Invokes the project generation API that generates the entire project structure for
     * the specified {@link io.spring.initializr.web.project.ProjectRequest}.
     *
     * @param request the project request
     * @return the {@link ProjectGenerationResult}
     */
    public ProjectGenerationResult invokeProjectStructureGeneration(ProjectRequest request) {
        InitializerMetadata metadata = (InitializerMetadata) this.parentAppCtx.getBean(InitializrMetadataProvider.class).get();
        try {
            InitializerProjectDescription description = (InitializerProjectDescription) this.requestConverter.convert(request, metadata);

            Architecture arch = description.getArchitecture();

            ProjectGenerationResult result;
            // if request multiple module project, create new context to generate submodule codes
            if (!arch.isMultipleModule()) {
                // single module
                InitializerProjectGenerator projectGenerator = new InitializerProjectGenerator((ctx) -> customizeProjectGenerationContext(ctx, metadata, new Module(true, true)));
                result = projectGenerator.generate(description, generateProject(request, null));
            } else {
                // multiple modules - root module
                InitializerProjectGenerator projectGenerator = new InitializerProjectGenerator((ctx) -> customizeProjectGenerationContext(ctx, metadata, new Module(true, false)));
                result = projectGenerator.generate(description, generateProject(request, null));

                for (Module subModule : arch.getSubModules()) {
                    // hack base dir for sub module
                    MutableProjectDescription subDescription = description.createCopy();
                    subDescription.setBaseDirectory(null);
                    // multiple modules - sub module
                    InitializerProjectGenerator subProjectGenerator = new InitializerProjectGenerator((ctx) -> customizeProjectGenerationContext(ctx, metadata, subModule));
                    subProjectGenerator.generate(subDescription,
                            generateProject(request, (desc) -> result.getRootDirectory().resolve(description.getBaseDirectory()).resolve(desc.getName() + "-" + subModule.getName())));
                }
            }

            addTempFile(result.getRootDirectory(), result.getRootDirectory());
            return result;
        } catch (ProjectGenerationException ex) {
            publishProjectFailedEvent(request, metadata, ex);
            throw ex;
        }
    }

    private ProjectAssetGenerator<ProjectGenerationResult> generateProject(ProjectRequest request, ProjectDirectoryFactory factory) {
        return (context) -> {
            Path projectDir = new DefaultProjectAssetGenerator(factory).generate(context);
            publishProjectGeneratedEvent(request, context);
            Constructor<ProjectGenerationResult> constructor = null;
            try {
                constructor = ProjectGenerationResult.class.getDeclaredConstructor(ProjectDescription.class, Path.class);
                constructor.setAccessible(true);
                return constructor.newInstance(context.getBean(ProjectDescription.class), projectDir);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    private void customizeProjectGenerationContext(AnnotationConfigApplicationContext context, InitializerMetadata metadata, Module module) {
        context.setParent(this.parentAppCtx);
        context.register(Config.class);
        context.registerBean(InitializerMetadata.class, () -> metadata);
        context.registerBean(BuildItemResolver.class, () -> new MetadataBuildItemResolver(metadata, context.getBean(ProjectDescription.class).getPlatformVersion()));
        context.registerBean(MetadataProjectDescriptionCustomizer.class, () -> new MetadataProjectDescriptionCustomizer(metadata));
        context.registerBean(Module.class, () -> module);
    }

    /**
     * Create a file in the same directory as the given directory using the directory name
     * and extension.
     *
     * @param dir       the directory used to determine the path and name of the new file
     * @param extension the extension to use for the new file
     * @return the newly created file
     */
    public Path createDistributionFile(Path dir, String extension) {
        Path download = dir.resolveSibling(dir.getFileName() + extension);
        addTempFile(dir, download);
        return download;
    }

    private void addTempFile(Path group, Path file) {
        this.temporaryFiles.computeIfAbsent(group, (key) -> new ArrayList<>()).add(file);
    }

    /**
     * Clean all the temporary files that are related to this root directory.
     *
     * @param dir the directory to clean
     * @see #createDistributionFile
     */
    public void cleanTempFiles(Path dir) {
        List<Path> tempFiles = this.temporaryFiles.remove(dir);
        if (!tempFiles.isEmpty()) {
            tempFiles.forEach((path) -> {
                try {
                    FileSystemUtils.deleteRecursively(path);
                } catch (IOException ex) {
                    // Continue
                }
            });
        }
    }

    private byte[] generateBuild(ProjectGenerationContext context) throws IOException {
        ProjectDescription description = context.getBean(ProjectDescription.class);
        StringWriter out = new StringWriter();
        BuildWriter buildWriter = context.getBeanProvider(BuildWriter.class)
                .getIfAvailable();
        if (buildWriter != null) {
            buildWriter.writeBuild(out);
            return out.toString().getBytes();
        } else {
            throw new IllegalStateException("No BuildWriter implementation found for "
                    + description.getLanguage());
        }
    }

    @Configuration
    @EnableAspectJAutoProxy(proxyTargetClass = true)
    public static class Config {

    }

    private void publishProjectGeneratedEvent(ProjectRequest request,
                                              ProjectGenerationContext context) {
        InitializrMetadata metadata = context.getBean(InitializrMetadata.class);
        ProjectGeneratedEvent event = new ProjectGeneratedEvent(request, metadata);
        this.eventPublisher.publishEvent(event);
    }

    private void publishProjectFailedEvent(ProjectRequest request,
                                           InitializrMetadata metadata, Exception cause) {
        ProjectFailedEvent event = new ProjectFailedEvent(request, metadata, cause);
        this.eventPublisher.publishEvent(event);
    }

}
