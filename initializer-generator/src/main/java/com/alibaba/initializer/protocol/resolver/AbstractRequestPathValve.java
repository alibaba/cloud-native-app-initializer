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

import java.util.regex.Pattern;

/**
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 */
public class AbstractRequestPathValve {

    public static final String PROTOCOL_PATH_PATTERN = "([\\w|\\-]+)\\.([\\w|\\.]+)";

    public static final String KV_PATH_PATTERN = "(&?[\\w\\%\\-\\.\\_]+\\=[\\w\\%\\-\\.\\_\\,]+)+";

    public static boolean isKVPath(String current) {
        return Pattern.matches(KV_PATH_PATTERN, current);
    }

    public static boolean isProtocolPath(String value) {
        return Pattern.matches(PROTOCOL_PATH_PATTERN, value);
    }

    public static String getArtifactIdFromProtocolPath(String val) {
        return val.substring(0, val.indexOf("."));
    }

}
