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

package com.alibaba.initializer.generation;

import java.nio.file.Path;
import java.util.Map;

import com.alibaba.initializer.controller.ProjectRequest;
import com.alibaba.initializer.core.ProjectGenerationRequest;
import com.alibaba.initializer.core.ProjectGenerationResult;
import com.alibaba.initializer.core.ProjectGenerator;
import io.spring.initializr.generator.project.ProjectDescription;
import io.spring.initializr.metadata.InitializrMetadataProvider;
import io.spring.initializr.web.project.ProjectGenerationInvoker;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;

/**
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 */
public class BootstrapProjectGenerator implements ProjectGenerator {

    @Autowired
    private InitializrMetadataProvider metadataProvider;

    @Autowired
    private ConversionService conversionService;

    @Autowired
    private ProjectGenerationInvoker<ProjectRequest> projectGenerationInvoker;

    @Override
    public ProjectGenerationResult generate(ProjectGenerationRequest request) {
        Map<String, Object> params = request.getParameters();

        ProjectRequest projReq = new ProjectRequest();
        projReq.getParameters().putAll(params);
        projReq.initialize(metadataProvider.get());

        // use spring component to set properties
        BeanWrapper beanWrapper = new BeanWrapperImpl(projReq);
        beanWrapper.setExtractOldValueForEditor(true);
        beanWrapper.setConversionService(conversionService);
        beanWrapper.setPropertyValues(new MutablePropertyValues(params), true);

        io.spring.initializr.web.project.ProjectGenerationResult pRes = this.projectGenerationInvoker.invokeProjectStructureGeneration(projReq);
        ProjectDescription pDesc = pRes.getProjectDescription();

        ProjectGenerationResult result = new ProjectGenerationResult() {
            @Override
            public String getName() {
                return pDesc.getArtifactId();
            }

            @Override
            public Path getProjectRoot() {
                return pRes.getRootDirectory();
            }

            @Override
            public void cleanUp() {
                projectGenerationInvoker.cleanTempFiles(getProjectRoot());
            }
        };

        return result;
    }

}
