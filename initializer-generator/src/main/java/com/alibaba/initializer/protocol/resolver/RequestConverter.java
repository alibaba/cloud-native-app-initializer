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

package com.alibaba.initializer.protocol.resolver;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.initializer.core.ProjectGenerationRequest;
import com.google.common.base.Joiner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

/**
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 */
public class RequestConverter {

    @Autowired
    private ApplicationContext applicationContext;

    private List<IRequestPathValve> converters;

    public ProjectGenerationRequest convert(HttpServletRequest request) {
        ProjectGenerationRequest projReq = new ProjectGenerationRequest();

        parseParameterFromParameterAndHeader(projReq, request);

        List<IRequestPathValve> converters = getConvertors();

        Path paths = Paths.get(request.getRequestURI());

        for (IRequestPathValve converter : converters) {
            converter.invoke(paths, projReq);
        }

        return projReq;
    }

    private List<IRequestPathValve> getConvertors() {
        if (converters == null) {
            synchronized (this) {
                if (converters == null) {
                    Map<String, IRequestPathValve> map = applicationContext.getBeansOfType(IRequestPathValve.class);
                    List<IRequestPathValve> tmp = new ArrayList<>(map.values());
                    AnnotationAwareOrderComparator.sort(tmp);
                    converters = tmp;
                }
            }
        }

        return converters;
    }

    private void parseParameterFromParameterAndHeader(ProjectGenerationRequest projReq, HttpServletRequest httpRequest) {
        // param from header
        Enumeration<String> iter = httpRequest.getHeaderNames();
        while (iter.hasMoreElements()) {
            String key = iter.nextElement();
            String value = httpRequest.getHeader(key);
            projReq.setParam(key, value);
        }

        // param from request param
        iter = httpRequest.getParameterNames();
        while (iter.hasMoreElements()) {
            String key = iter.nextElement();
            String[] values = httpRequest.getParameterValues(key);
            if (values == null || values.length == 0) {
                continue;
            }
            String value = Joiner.on(',').join(values);
            projReq.setParam(key, value);
        }
    }
}
