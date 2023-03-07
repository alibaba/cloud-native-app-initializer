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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import org.springframework.util.CollectionUtils;

/**
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 */
public class RepoRenderResult {

    /**
     * the original code template repository
     */
    private CodeTemplateRepo repo;

    /**
     * the render result
     */
    private List<TemplateRenderResult> results;

    public RepoRenderResult(CodeTemplateRepo repo, List<TemplateRenderResult> results) {
        this.repo = repo;
        this.results = results;
    }

    public List<TemplateRenderResult> getResults() {
        return results;
    }

    public List<TemplateRenderResult> getResults(String prefix) {
        return results.stream().filter(res -> res.getPath().startsWith(prefix)).collect(Collectors.toList());
    }

    public TemplateRenderResult getResult(String path) {
        if (CollectionUtils.isEmpty(results)) {
            return null;
        }
        return results.stream().filter(res -> StringUtils.equals(path, res.getPath().toString())).findFirst().orElse(null);
    }

    public String getStringResult(String path) {
        TemplateRenderResult result = getResult(path);
        return result != null ? result.getContent() : null;
    }

    @Getter
    @Setter
    public static class TemplateRenderResult {

        private Path folder;

        private String fileName;

        private String content;

        public TemplateRenderResult(String fileName, Path folder, String content) {
            this.content = content;
            this.fileName = fileName;
            this.folder = folder;
        }

        public Path getPath() {
            if (folder == null) {
                return Paths.get(fileName);
            } else {
                return folder.resolve(fileName);
            }
        }

    }
}
