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

package com.alibaba.initializer.core.template.loader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import com.alibaba.initializer.core.constants.ErrorCodeEnum;
import com.alibaba.initializer.core.exception.BizRuntimeException;
import com.alibaba.initializer.core.template.CodeTemplate;
import com.alibaba.initializer.core.template.CodeTemplateRepo;
import com.alibaba.initializer.core.template.CodeTemplateRepoLoader;
import com.google.common.collect.Lists;
import lombok.SneakyThrows;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.util.ResourceUtils;

import static org.springframework.util.ResourceUtils.JAR_URL_SEPARATOR;

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

            Resource[] resources = resourceLoader.getResources(CLASSPAHT_PREFIX + uri.getPath());

            List<CodeTemplate> templates = Arrays.stream(resources)
                    .filter(Resource::exists)
                    .map(this::scanTemplte)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());

            return new CodeTemplateRepo(uri, templates);
        } catch (Exception e) {
            throw new BizRuntimeException(ErrorCodeEnum.SYSTEM_ERROR, "load template error", e);
        }
    }

    @SneakyThrows
    private List<CodeTemplate> scanTemplte(Resource resource) {
        List<CodeTemplate> templates = Lists.newArrayList();

        URL url = resource.getURL();

        if (ResourceUtils.isFileURL(url)) {
            visitFileSystem(templates::add, resource);
        } else if (ResourceUtils.isJarURL(url)) {
            visitJarSystem(templates::add, resource);
        }

        return templates;
    }

    private void visitFileSystem(TempFileVisitor visitor, Resource resource)
            throws IOException {
        if (!resource.isFile()) {
            return;
        }
        File rootFile = resource.getFile();
        Path scanRoot = Paths.get(rootFile.getAbsolutePath());
        Files.walkFileTree(scanRoot, EnumSet.of(FileVisitOption.FOLLOW_LINKS),
                Integer.MAX_VALUE, new FileVisitor<Path>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir,
                                                             BasicFileAttributes attrs) {
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(Path originPath,
                                                     BasicFileAttributes attrs) {
                        String fileName = originPath.getFileName().toString();

                        Path relativePath = originPath.subpath(scanRoot.getNameCount(),
                                originPath.getNameCount());
                        Path relativeFolderPath = relativePath.getNameCount() == 1
                                ? null
                                : relativePath.subpath(0,
                                relativePath.getNameCount() - 1);

                        visitor.visit(new CodeTemplate(relativeFolderPath, fileName) {
                                          @Override
                                          public Reader getReader() {
                                              try {
                                                  return new FileReader(originPath.toFile());
                                              } catch (FileNotFoundException e) {
                                                  throw new BizRuntimeException(ErrorCodeEnum.SYSTEM_ERROR, "", e);
                                              }
                                          }
                                      }
                        );
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFileFailed(Path file, IOException exc) {
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                        return FileVisitResult.CONTINUE;
                    }
                });
    }

    private void visitJarSystem(TempFileVisitor visitor, Resource resource) throws IOException {
        URL url = resource.getURL();

        URL jarUrl = ResourceUtils.extractJarFileURL(url);

        JarFile jarFile = new JarFile(jarUrl.getFile());

        Enumeration<JarEntry> entries = jarFile.entries();

        String resourcePath = url.getFile();

        // the scan root from jar file
        String scanRoot = resourcePath.substring(resourcePath.indexOf(JAR_URL_SEPARATOR) + JAR_URL_SEPARATOR.length());
        Path scanRootPath = Paths.get(scanRoot);

        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();

            // the full name of jar entry
            String entryName = entry.getName();
            if (!entryName.startsWith(scanRoot) || entry.isDirectory()) {
                continue;
            }

            Path entryPath = Paths.get(entryName);
            String fileName = entryPath.getFileName().toString();

            Path relativePath = entryPath.subpath(scanRootPath.getNameCount(), entryPath.getNameCount());
            Path relativeFolderPath = relativePath.getNameCount() == 1
                    ? null
                    : relativePath.subpath(0,
                    relativePath.getNameCount() - 1);

            visitor.visit(new CodeTemplate(relativeFolderPath, fileName) {
                              @Override
                              public Reader getReader() {
                                  try {
                                      return new InputStreamReader(resourceLoader.getResource("classpath:/" + entryPath.toString()).getInputStream());
                                  } catch (IOException e) {
                                      throw new BizRuntimeException(ErrorCodeEnum.SYSTEM_ERROR, "load resource error", e);
                                  }
                              }
                          }
            );
        }
    }

    @FunctionalInterface
    public interface TempFileVisitor {
        void visit(CodeTemplate template);
    }

    @Override
    public String getProtocol() {
        return "classpath";
    }
}
