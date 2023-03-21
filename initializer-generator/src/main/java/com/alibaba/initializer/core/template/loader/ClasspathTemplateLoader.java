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

package com.alibaba.initializer.core.template.loader;

import com.alibaba.initializer.core.constants.ErrorCodeEnum;
import com.alibaba.initializer.core.exception.BizRuntimeException;
import com.alibaba.initializer.core.template.CodeTemplate;
import com.alibaba.initializer.core.template.CodeTemplateRepo;
import com.alibaba.initializer.core.template.CodeTemplateRepoLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import static org.springframework.util.ResourceUtils.JAR_URL_SEPARATOR;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * load template file from classpath
 *
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 */
public class ClasspathTemplateLoader implements CodeTemplateRepoLoader {

    private static final String CLASSPAHT_PREFIX = "classpath:";

    @Autowired
    private ApplicationContext resourceLoader;

    @Override
    public CodeTemplateRepo load(String uriStr) {
        try {
            URI uri = new URI(uriStr);

            Path rootPath = Paths.get(uri.getPath());

            Resource rootResource = resourceLoader.getResource(CLASSPAHT_PREFIX + rootPath);
            if (!rootResource.exists()) {
                return new CodeTemplateRepo(uri, Collections.emptyList());
            }

            Resource[] resources = resourceLoader.getResources(CLASSPAHT_PREFIX + rootPath + "/**");

            List<CodeTemplate> templates = Arrays.stream(resources)
                    .filter(Resource::isReadable)
                    .map(item -> toTemplate(item, rootPath))
                    .collect(Collectors.toList());

            return new CodeTemplateRepo(uri, templates);
        } catch (Exception e) {
            throw new BizRuntimeException(ErrorCodeEnum.SYSTEM_ERROR, "load template error", e);
        }
    }

    private CodeTemplate toTemplate(Resource resource, Path scanRootPath) {

        try {
            URL url = resource.getURL();

            String urlFile = url.getFile();

            int separatorIndex = urlFile.lastIndexOf(JAR_URL_SEPARATOR);

            if (separatorIndex > -1) {
                urlFile = urlFile.substring(separatorIndex + 1);
            } else {
                String classpathPath = ClasspathTemplateLoader.class.getProtectionDomain().getCodeSource().getLocation().getPath();
                urlFile = urlFile.startsWith(classpathPath) ? urlFile.replace(classpathPath, "/") : urlFile;
            }

            Path entryPath = Paths.get(urlFile);
            String fileName = entryPath.getFileName().toString();

            Path relativePath = entryPath.subpath(scanRootPath.getNameCount(), entryPath.getNameCount());
            Path relativeFolderPath = relativePath.getNameCount() == 1
                    ? null
                    : relativePath.subpath(0,
                    relativePath.getNameCount() - 1);

            return new CodeTemplate(relativeFolderPath, fileName) {
                @Override
                public Reader getReader() throws IOException {
                    return new InputStreamReader(resource.getInputStream());
                }
            };
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getProtocol() {
        return "classpath";
    }
}
