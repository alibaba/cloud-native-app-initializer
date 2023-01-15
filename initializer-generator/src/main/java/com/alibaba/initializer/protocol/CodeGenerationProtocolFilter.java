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

package com.alibaba.initializer.protocol;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.initializer.core.ProjectGenerator;
import com.alibaba.initializer.protocol.archive.ProjectArchiveHandler;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.http.server.GitFilter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.filter.OrderedFilter;

/**
 * filter http request and route to the corresponding ProjectGenerator
 *
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 * @see ProjectGenerator
 */
@Slf4j
public class CodeGenerationProtocolFilter extends HttpFilter implements OrderedFilter {

    private static final long serialVersionUID = 3412652566849540790L;

    private static final String PROTOCOL_PATH_PATTERN = "([\\w|\\-]+)\\.([\\w|\\.]+)";
    private static final String PATH_SPLITTER = "/";

    private static final List<String> SKIP_PRESETS = Lists.newArrayList("/api/", "/nginx_status");

    private final Consumer<FilterConfig> initCallBack;

    @Autowired
    private GitFilter gitFilter;

    @Autowired
    private ProjectArchiveHandler projectArchiveHandler;

    public CodeGenerationProtocolFilter(Consumer<FilterConfig> initCallBack) {
        this.initCallBack = initCallBack;
    }

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String uri = request.getRequestURI();

        if (SKIP_PRESETS.stream().anyMatch(uri::startsWith)) {
            super.doFilter(request, response, chain);
            return;
        }

        try {
            String protocol = findProtocol(uri);

            switch (protocol) {
                case "git":
                    gitFilter.doFilter(new HttpServletRequestWrapper(request) {
                        @Override
                        public String getPathInfo() {
                            return request.getRequestURI();
                        }
                    }, response, chain);
                    break;
                case "zip":
                case "tar":
                case "tar.gz":
                    projectArchiveHandler.handle(request, response, chain, protocol);
                    break;
                default:
                    super.doFilter(request, response, chain);
            }
        } catch (Exception e) {
            log.error("code generate error", e);
        }
    }

    private String findProtocol(String uri) {
        String name = Stream.of(uri.split(PATH_SPLITTER))
                .sorted(Comparator.reverseOrder())
                .filter(path -> Pattern.matches(PROTOCOL_PATH_PATTERN, path))
                .findFirst().orElse(null);

        if (name == null) {
            return "unknown";
        }
        Matcher matcher = Pattern.compile(PROTOCOL_PATH_PATTERN).matcher(name);

        matcher.find();

        return matcher.group(2);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        super.init(filterConfig);
        initCallBack.accept(filterConfig);
    }

    @Override
    public int getOrder() {
        return REQUEST_WRAPPER_FILTER_MAX_ORDER + 106;
    }
}
