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

package com.alibaba.initializer.metadata;

import java.util.Collections;
import java.util.List;

import io.spring.initializr.metadata.DefaultMetadataElement;
import io.spring.initializr.metadata.Describable;

/**
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 */
public class Architecture extends DefaultMetadataElement implements Describable {

    private String description;

    private List<String> requiredDependency;

    private List<Module> subModules;

    public Architecture() {
    }

    public Architecture(String id, String name, List<String> requiredDependency) {
        setId(id);
        setName(name);
        if (requiredDependency == null) {
            setRequiredDependency(Collections.emptyList());
        } else {
            setRequiredDependency(Collections.unmodifiableList(requiredDependency));
        }
    }

    public List<String> getRequiredDependency() {
        return requiredDependency;
    }

    public void setRequiredDependency(List<String> requiredDependency) {
        this.requiredDependency = requiredDependency;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Module> getSubModules() {
        return subModules;
    }

    public void setSubModules(List<Module> subModules) {
        this.subModules = subModules;
    }

    public boolean isMultipleModule() {
        return subModules != null && subModules.size() > 0;
    }

}
