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
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;

/**
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 */
public class CodeTemplateRepoRenderer {

    private static final String MUSTACHE_SUFIX = ".mustache";

    private final Mustache.Compiler mustache;

    private final Function<String, String> keyGenerator;

    private final Cache templateCache;

    @Autowired
    private TemplateStringRender templateStringRender;

    public CodeTemplateRepoRenderer() {
        this.mustache = Mustache.compiler().escapeHTML(false);
        this.keyGenerator = null;
        this.templateCache = null;
    }

    public RepoRenderResult render(CodeTemplateRepo repo, Map<String, String> params) {

        List<RepoRenderResult.TemplateRenderResult> list = repo
                .getTemplates()
                .stream()
                .map(temp -> render(temp, params))
                .collect(Collectors.toList());

        return new RepoRenderResult(repo, list);
    }

    @SneakyThrows
    public RepoRenderResult.TemplateRenderResult render(CodeTemplate template, Map<String, String> params) {
        String fileName = template.getFileName();
        fileName = StringUtils.removeEnd(fileName, MUSTACHE_SUFIX);

        Path path = template.getFullPath();

        Path folder = path.getParent();

        folder = templateStringRender.renderPath(folder, params);
        fileName = templateStringRender.renderString(fileName, params);

        String content = renderStringContent(template, params);

        return new RepoRenderResult.TemplateRenderResult(fileName, folder, content);
    }

    public String renderStringContent(CodeTemplate template) throws IOException {
        return renderStringContent(template, null);
    }

    public String renderStringContent(CodeTemplate template, Map<String, String> params) throws IOException {
        String fileName = template.getFileName();
        String content;
        if (fileName.endsWith(MUSTACHE_SUFIX)) {
            content = getTemplate(template).execute(params);
        } else {
            content = getStringContent(template);
        }
        return content;
    }

    private String getStringContent(CodeTemplate template) throws IOException {
        try (Reader reader = template.getReader()) {
            return IOUtils.toString(reader);
        }
    }

    private Template getTemplate(CodeTemplate template) {
        try {
            if (this.templateCache != null) {
                try {
                    return this.templateCache.get(this.keyGenerator.apply(template.getFileName()), () -> loadTemplate(template));
                } catch (Cache.ValueRetrievalException ex) {
                    throw ex.getCause();
                }
            }
            return loadTemplate(template);
        } catch (Throwable ex) {
            throw new IllegalStateException("Cannot load template " + template.getFileName(), ex);
        }
    }

    private Template loadTemplate(CodeTemplate codeTemplate) throws Exception {
        try (Reader reader = codeTemplate.getReader()) {
            return this.mustache.compile(reader);
        }
    }
}
