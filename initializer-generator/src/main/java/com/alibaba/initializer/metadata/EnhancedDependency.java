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

package com.alibaba.initializer.metadata;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.spring.initializr.metadata.Dependency;

import java.util.Map;

/**
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class EnhancedDependency extends Dependency {

    private boolean hide = false;

    private Map<String, DependencyArchConfig> archCfg;

    public Map<String, DependencyArchConfig> getArchCfg() {
        return archCfg;
    }

    public void setArchCfg(Map<String, DependencyArchConfig> archCfg) {
        this.archCfg = archCfg;
    }

    public boolean isHide() {
        return hide;
    }

    public void setHide(boolean hide) {
        this.hide = hide;
    }
}
