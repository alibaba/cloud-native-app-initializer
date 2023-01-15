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

package com.alibaba.initializer.core.template;

import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

/**
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 */
public class CodeTemplateRepo {

    private final URI uri;

    private final List<CodeTemplate> templates;

    public CodeTemplateRepo(URI uri, List<CodeTemplate> templates) {
        this.templates = Collections.unmodifiableList(templates);
        this.uri = uri;
    }

    public List<CodeTemplate> getTemplates() {
        return templates;
    }

    public URI getUri() {
        return uri;
    }

    public CodeTemplate getTemplate(String path) {
        Path p = Paths.get(path);
        return templates.stream().filter(temp -> temp.getFullPath().equals(p)).findFirst().orElse(null);
    }

    @SneakyThrows
    public CodeTemplateRepo subRepo(String subRoot) {
        if (StringUtils.isBlank(subRoot) || ".".equalsIgnoreCase(subRoot)) {
            return this;
        }

        Path subPathPrefix = Paths.get(subRoot);

        List<CodeTemplate> subResourcesList = templates
                .stream()
                .filter(temp -> temp.getFullPath().startsWith(subPathPrefix))
                .map(source -> {
                    Path folder = subPathPrefix.relativize(source.getFullPath());
                    String name = source.getFileName();
                    return new CodeTemplate(folder, name) {
                        @Override
                        public Reader getReader() throws IOException {
                            return source.getReader();
                        }
                    };
                })
                .collect(Collectors.toList());


        String path = uri.getPath();
        if (path == null) {
            path = "";
        }

        if (path.endsWith("/")) {
            path += subRoot;
        } else {
            path += "/" + subRoot;
        }
        URI subUri = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), path, uri.getQuery(), uri.getFragment());
        return new CodeTemplateRepo(subUri, subResourcesList);
    }

    public void cleanup() {
        if (templates == null) {
            return;
        }

        templates.forEach(CodeTemplate::cleanup);
    }
}
