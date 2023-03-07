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

package com.alibaba.initializer.protocol.archive;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.initializer.core.ProjectGenerationRequest;
import com.alibaba.initializer.core.ProjectGenerationResult;
import com.alibaba.initializer.core.ProjectGenerator;
import com.alibaba.initializer.core.constants.ErrorCodeEnum;
import com.alibaba.initializer.core.exception.BizRuntimeException;
import com.alibaba.initializer.protocol.resolver.RequestConverter;
import com.google.common.collect.Lists;
import io.spring.initializr.web.controller.ProjectGenerationController;
import io.spring.initializr.web.project.ProjectRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.UnixStat;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.HttpEntityMethodProcessor;

/**
 * backage generated code and output with http protocol
 *
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 */
@Slf4j
public class ProjectArchiveHandler {

    @Autowired
    private ProjectGenerator routeGenerator;

    @Autowired
    private RequestConverter requestConverter;

    private HttpEntityMethodProcessor httpEntityMethodProcessor = new HttpEntityMethodProcessor(Lists.newArrayList(new ByteArrayHttpMessageConverter()));

    private void responseArchiveBytes(ResponseEntity<byte[]> entity, HttpServletRequest request, HttpServletResponse response) {

        try {
            // using spring compone to response archive bytes
            Method method = ProjectGenerationController.class.getDeclaredMethod("springZip", ProjectRequest.class);

            MethodParameter parameter = new MethodParameter(method, -1);

            httpEntityMethodProcessor.handleReturnValue(entity, parameter, new ModelAndViewContainer(), new ServletWebRequest(request, response));
        } catch (Exception e) {
            log.error("write archive error", e);
        }
    }

    public void handle(HttpServletRequest request, HttpServletResponse response, FilterChain chain, String protocol) {
        ProjectGenerationRequest projRequest = requestConverter.convert(request);

        ProjectGenerationResult projResult = routeGenerator.generate(projRequest);

        // create archive bytes
        ResponseEntity<byte[]> entity;
        Path archive = null;
        try {
            switch (protocol) {
                case "zip":
                    archive = createArchive(projResult, "zip", ZipArchiveOutputStream::new, ZipArchiveEntry::new, ZipArchiveEntry::setUnixMode);
                    entity = upload(archive, generateFileName(projResult, "zip"), "application/zip");
                    break;
                case "tar":
                case "tar.gz":
                    archive = createArchive(projResult, "tar.gz", this::createTarArchiveOutputStream, TarArchiveEntry::new, TarArchiveEntry::setMode);
                    entity = upload(archive, generateFileName(projResult, "tar.gz"), "application/x-compress");
                    break;
                default:
                    throw new BizRuntimeException(ErrorCodeEnum.UNSUPPORTED, "not support archive type");
            }
        } catch (Exception e) {
            throw new BizRuntimeException(ErrorCodeEnum.SYSTEM_ERROR, "create archive error", e);
        } finally {
            projResult.cleanUp();
            try {
                FileSystemUtils.deleteRecursively(archive);
            } catch (IOException ignoe) {
            }
        }

        // response archive bytes
        responseArchiveBytes(entity, request, response);
    }


    private String generateFileName(ProjectGenerationResult request, String extension) {
        String candidate = request.getName();
        String tmp = candidate.replaceAll(" ", "_");
        try {
            return URLEncoder.encode(tmp, "UTF-8") + "." + extension;
        } catch (UnsupportedEncodingException ex) {
            throw new IllegalStateException("Cannot encode URL", ex);
        }
    }

    private ResponseEntity<byte[]> upload(Path archive, String fileName, String contentType)
            throws IOException {
        byte[] bytes = Files.readAllBytes(archive);
        ResponseEntity<byte[]> result = createResponseEntity(bytes, contentType, fileName);
        return result;
    }

    private ResponseEntity<byte[]> createResponseEntity(byte[] content, String contentType, String fileName) {
        String contentDispositionValue = "attachment; filename=\"" + fileName + "\"";
        return ResponseEntity.ok().header("Content-Type", contentType)
                .header("Content-Disposition", contentDispositionValue).body(content);
    }

    private TarArchiveOutputStream createTarArchiveOutputStream(OutputStream output) {
        try {
            return new TarArchiveOutputStream(new GzipCompressorOutputStream(output));
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private <T extends ArchiveEntry> Path createArchive(ProjectGenerationResult result, String fileExtension,
                                                        Function<OutputStream, ? extends ArchiveOutputStream> archiveOutputStream,
                                                        BiFunction<File, String, T> archiveEntry, BiConsumer<T, Integer> setMode) throws IOException {
        Path root = result.getProjectRoot();
        Path archive = root.resolveSibling(root.getFileName() + "." + fileExtension);
        try (ArchiveOutputStream output = archiveOutputStream.apply(Files.newOutputStream(archive))) {
            Files.walk(result.getProjectRoot())
                    .filter((path) -> !result.getProjectRoot().equals(path))
                    .forEach((path) -> {
                        try {
                            String entryName = getEntryName(result.getProjectRoot(), path);
                            T entry = archiveEntry.apply(path.toFile(), entryName);
                            setMode.accept(entry, getUnixMode(path));
                            output.putArchiveEntry(entry);
                            if (!Files.isDirectory(path)) {
                                Files.copy(path, output);
                            }
                            output.closeArchiveEntry();
                        } catch (IOException ex) {
                            throw new IllegalStateException(ex);
                        }
                    });
        }
        return archive;
    }

    private String getEntryName(Path root, Path path) {
        String entryName = root.relativize(path).toString().replace('\\', '/');
        if (Files.isDirectory(path)) {
            entryName += "/";
        }
        return entryName;
    }

    private int getUnixMode(Path path) {
        if (Files.isDirectory(path)) {
            return UnixStat.DIR_FLAG | UnixStat.DEFAULT_DIR_PERM;
        } else {
            return UnixStat.FILE_FLAG | UnixStat.DEFAULT_FILE_PERM;
        }
    }

}
