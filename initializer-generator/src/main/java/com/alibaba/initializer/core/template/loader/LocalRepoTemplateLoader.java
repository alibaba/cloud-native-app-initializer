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

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import com.alibaba.initializer.core.template.CodeTemplate;
import com.alibaba.initializer.core.template.CodeTemplateRepo;
import com.alibaba.initializer.core.template.CodeTemplateRepoLoader;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * load template file from local file system
 * <pre>
 *     This used for test case
 * </pre>
 *
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 */
@Slf4j
public class LocalRepoTemplateLoader implements CodeTemplateRepoLoader {

    private static final Set<String> IGNORE_FILE_NAMES = Sets.newHashSet(".git");

    @SneakyThrows
    @Override
    public CodeTemplateRepo load(String uriStr) {

        URI uri = new URI(uriStr);

        String path = uri.getPath();

        File file = new File(path);

        return scanGitRepoFiles(uri, file);
    }

    protected CodeTemplateRepo scanGitRepoFiles(URI path, File file) {
        List<CodeTemplate> templates = Lists.newArrayList();
        if (file != null && file.listFiles() != null) {
            for (File listFile : file.listFiles()) {
                scan(templates, Paths.get(""), listFile);
            }
        }
        return new CodeTemplateRepo(path, templates);
    }

    protected void scan(List<CodeTemplate> templates, Path parent, File file) {
        if (IGNORE_FILE_NAMES.contains(file.getName())) {
            return;
        }

        if (file.isDirectory()) {
            for (File listFile : file.listFiles()) {
                scan(templates, parent.resolve(file.getName()), listFile);
            }
        } else {
            CodeTemplate template = new CodeTemplate(parent, file.getName()) {
                @SneakyThrows
                @Override
                public Reader getReader() {
                    return new FileReader(file);
                }
            };
            templates.add(template);
        }
    }

    @Override
    public String getProtocol() {
        return "local";
    }
}
