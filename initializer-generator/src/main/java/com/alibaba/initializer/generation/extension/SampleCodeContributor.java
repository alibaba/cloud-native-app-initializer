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

package com.alibaba.initializer.generation.extension;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.alibaba.initializer.core.constants.ErrorCodeEnum;
import com.alibaba.initializer.core.exception.BizRuntimeException;
import com.alibaba.initializer.core.template.CodeTemplateRepo;
import com.alibaba.initializer.core.template.CodeTemplateRepoRenderer;
import com.alibaba.initializer.core.template.RepoRenderResult;
import com.alibaba.initializer.core.template.loader.RootRepoTemplateLoader;
import com.alibaba.initializer.generation.constants.BootstrapTemplateRenderConstants;
import com.alibaba.initializer.metadata.EnhancedDependency;
import com.alibaba.initializer.metadata.Architecture;
import com.alibaba.initializer.metadata.DependencyArchConfig;
import com.alibaba.initializer.metadata.InitializerMetadata;
import com.alibaba.initializer.metadata.Module;
import com.alibaba.initializer.metadata.ModuleConfig;
import com.alibaba.initializer.project.InitializerProjectDescription;
import io.spring.initializr.generator.buildsystem.Dependency;
import io.spring.initializr.generator.language.Language;
import io.spring.initializr.generator.language.SourceStructure;
import io.spring.initializr.generator.project.contributor.ProjectContributor;
import io.spring.initializr.metadata.DependencyGroup;
import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;
import org.springframework.util.FileCopyUtils;

import static com.alibaba.initializer.generation.constants.BootstrapTemplateRenderConstants.KEY_ARTIFACT_ID;

/**
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 */
public class SampleCodeContributor implements ProjectContributor {

    @Value("${application.democode-path}")
    private String templates;

    private static final String PACKAGE_PATTEN = "package ([a-z0-9\\.]+)\\;";

    @Autowired
    private Module module;

    @Autowired
    protected InitializerProjectDescription description;

    @Autowired
    protected RootRepoTemplateLoader loader;

    @Autowired
    protected CodeTemplateRepoRenderer renderer;

    @Autowired
    private InitializerMetadata meta;

    @Override
    public void contribute(Path projectRoot) throws IOException {

        Language language = description.getLanguage();

        Map<String, String> params = new HashMap<>();
        params.put(BootstrapTemplateRenderConstants.KEY_APP_SHOTNAME, description.getName());
        params.put(BootstrapTemplateRenderConstants.KEY_APPLICATION_NAME, description.getApplicationName());
        params.put(BootstrapTemplateRenderConstants.KEY_BASE_PACKAGE, description.getPackageName());
        params.put(BootstrapTemplateRenderConstants.KEY_SPRINGBOOT_VERSION, description.getPlatformVersion().toString());
        params.put(BootstrapTemplateRenderConstants.KEY_JAVA_VERSION, description.getLanguage().jvmVersion());
        params.put(KEY_ARTIFACT_ID, description.getArtifactId());
        params.put(BootstrapTemplateRenderConstants.KEY_BASE_VERSION, description.getVersion());
        SourceStructure structure = description.getBuildSystem().getMainSource(projectRoot, language);

        Map<Dependency, String> dependencyRepoUris = getRepos(description);

        dependencyRepoUris.forEach((dependency, uri) -> {
            CodeTemplateRepo repo = loader.load(uri);
            RepoRenderResult result = renderer.render(repo, params);

            // write code
            List<RepoRenderResult.TemplateRenderResult> codes = result.getResults(language.id());
            codes.forEach(res -> writeCode(res, language, projectRoot, structure));

            // write resources
            List<RepoRenderResult.TemplateRenderResult> resources = result.getResults("resources");
            resources.forEach(res -> writeResources(res, language, projectRoot, structure));

            // write root
            if (module.isRoot()) {
                List<RepoRenderResult.TemplateRenderResult> roots = result.getResults("root");
                roots.forEach(res -> writeRoot(res, language, projectRoot, structure));
            }
        });
    }

    protected boolean filter(RepoRenderResult.TemplateRenderResult templateRenderResult) {

        return !(templateRenderResult.getFileName().endsWith("md") && templateRenderResult.getFileName().startsWith("README"));
    }

    protected Map<Dependency, String> getRepos(InitializerProjectDescription description) {
        Map<String, Dependency> dependencyMap = description.getRequestedDependencies();
        Architecture arch = description.getArchitecture();

        if (CollectionUtils.isEmpty(dependencyMap)) {
            return Collections.emptyMap();
        }

        return dependencyMap.entrySet().stream()
                .filter(entry -> this.filterByModule(entry, arch))
                .collect(Collectors.toMap(Map.Entry::getValue, entity -> toPath(entity.getKey())));
    }

    private String toPath(String s) {
        String prefix = templates;

        if (!prefix.endsWith("/")) {
            prefix += "/";
        }
        return prefix + s;
    }


    private boolean filterByModule(Map.Entry<String, Dependency> entry, Architecture arch) {

        String id = entry.getKey();
        EnhancedDependency dep = getMetaDependency(id);

        if (dep == null) {
            return true;
        }

        Map<String, DependencyArchConfig> cfgs = dep.getArchCfg();

        DependencyArchConfig cfg = cfgs != null ? cfgs.get(arch.getId()) : null;

        if (cfg != null) {
            if (cfg.getSupported() != null && !cfg.getSupported()) {
                // not supported explicitly
                return false;
            }

            Map<String, ModuleConfig> moduleCfgs = cfg.getModules();
            if (moduleCfgs == null || moduleCfgs.size() == 0) {
                return module.isMain();
            }

            ModuleConfig moduleCfg = moduleCfgs.get(module.getName());

            if (moduleCfg == null) {
                return false;
            }

            return true;
        } else {
            return module.isMain();
        }
    }

    private EnhancedDependency getMetaDependency(String id) {

        for (DependencyGroup group : meta.getDependencies().getContent()) {
            for (io.spring.initializr.metadata.Dependency dependency : group.getContent()) {
                if (StringUtils.equalsIgnoreCase(dependency.getId(), id)) {
                    return (EnhancedDependency) dependency;
                }
            }
        }

        return null;
    }

    protected void writeCode(RepoRenderResult.TemplateRenderResult result, Language language, Path projectRoot, SourceStructure structure) {
        String content = result.getContent();
        Path path = reslovePackagePath(structure.getSourcesDirectory(), content);
        path = path.resolve(result.getFileName());

        try {
            doWirte(path, content, false);
        } catch (IOException e) {
            throw new BizRuntimeException(ErrorCodeEnum.SYSTEM_ERROR, "write code error", e);
        }
    }

    protected void writeResources(RepoRenderResult.TemplateRenderResult result, Language language, Path projectRoot, SourceStructure structure) {
        String content = result.getContent();
        Path path = result.getPath();

        path = structure.getResourcesDirectory().resolve(path.subpath(1, path.getNameCount()));

        try {
            doWirte(path, content, true);
        } catch (IOException e) {
            throw new BizRuntimeException(ErrorCodeEnum.SYSTEM_ERROR, "write code error", e);
        }
    }

    protected void writeRoot(RepoRenderResult.TemplateRenderResult result, Language language, Path projectRoot, SourceStructure structure) {
        Path path = projectRoot.resolve(result.getPath().subpath(1, result.getPath().getNameCount()));

        String content = result.getContent();
        try {
            doWirte(path, content, true);
        } catch (IOException e) {
            throw new BizRuntimeException(ErrorCodeEnum.SYSTEM_ERROR, "write code error", e);
        }
    }

    protected Path reslovePackagePath(Path sourceRoot, String content) {
        Path path = sourceRoot;
        Matcher matcher = Pattern.compile(PACKAGE_PATTEN).matcher(content);
        if (matcher.find()) {
            String pkg = matcher.group(1);
            for (String s : pkg.split("\\.")) {
                path = path.resolve(s);
            }
        }

        return path;
    }

    protected void doWirte(Path filePath, String content, boolean append) throws IOException {
        Path folderPath = filePath.getParent();
        Files.createDirectories(folderPath);

        File file = filePath.toFile();

        if (!append || !file.exists()) {
            Files.writeString(filePath, content);
        } else {
            FileCopyUtils.copy(
                    new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)),
                    Files.newOutputStream(filePath, StandardOpenOption.APPEND));
        }
    }
}
