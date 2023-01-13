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

package com.alibaba.initializer.project;

import com.alibaba.initializer.metadata.Architecture;
import io.spring.initializr.generator.project.MutableProjectDescription;

/**
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 */
public class InitializerProjectDescription extends MutableProjectDescription {

    private Architecture architecture;

    public InitializerProjectDescription() {

    }

    public InitializerProjectDescription(InitializerProjectDescription desc) {
        super(desc);
        this.architecture = desc.getArchitecture();
    }

    public Architecture getArchitecture() {
        return architecture;
    }

    public void setArchitecture(Architecture architecture) {
        this.architecture = architecture;
    }

    @Override
    public MutableProjectDescription createCopy() {
        return new InitializerProjectDescription(this);
    }
}
