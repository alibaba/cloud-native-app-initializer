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
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 */
public class TemplateStringRender {

    private static final String PATTERN = "\\$\\{(.*?)\\}";

    public Path renderPath(Path input, Map<String, String> params) {
        if (input == null) {
            return null;
        }
        return Paths.get(renderString(input.toString(), params));
    }

    public String renderString(String input, Map<String, String> params) {
        if (!input.contains("${")) {
            return input;
        }

        Matcher m = Pattern.compile(PATTERN).matcher(input);

        String renderedStr = input;

        while (m.find()) {
            String key = m.group(1);
            String patten = m.group(0);
            String value = params.get(key);
            while (value != null && renderedStr.contains(patten)) {
                renderedStr = renderedStr.replace(patten, value);
            }
        }

        return renderedStr;
    }
}