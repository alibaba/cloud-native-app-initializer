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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Path;
import java.util.stream.Stream;

import com.alibaba.initializer.core.ProjectGenerationRequest;
import org.apache.commons.lang3.StringUtils;

import org.springframework.core.annotation.Order;

import static com.alibaba.initializer.generation.constants.BootstrapTemplateRenderConstants.KEY_ARTIFACT_ID;

/**
 * <pre>
 *     /project/artifactId.zip?xxxx=123
 *     /project/userkey/artifactId.zip?xxxx=123
 * </pre>
 *
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 */
@Order
public class DefaultRequestPathValve extends AbstractRequestPathValve implements IRequestPathValve {

    @Override
    public void invoke(Path paths, ProjectGenerationRequest projReq) {

        for (Path path : paths) {
            String current = path.toString();

            parse:
            {
                if (isKVPath(current)) {
                    // kv path
                    String params;
                    try {
                        params = URLDecoder.decode(current, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        params = URLDecoder.decode(current);
                    }

                    Stream.of(params.split("&"))
                            .filter(StringUtils::isNotBlank)
                            .map(data -> data.split("="))

                            .filter(data -> data.length == 2)
                            .forEach(data -> {
                                String key = data[0];
                                String val = data[1];
                                projReq.setParam(key, val);
                            });
                    break parse;
                }

                if (isProtocolPath(current)) {
                    // it's is a protocol path, such as "*.git"、"*.zip"、"*.tar.gz"
                    // ignore the rest
                    if (projReq.getParam(KEY_ARTIFACT_ID) == null) {
                        projReq.setParam(KEY_ARTIFACT_ID, getArtifactIdFromProtocolPath(current));
                    }
                    break;
                }

            }

        }
    }

}
