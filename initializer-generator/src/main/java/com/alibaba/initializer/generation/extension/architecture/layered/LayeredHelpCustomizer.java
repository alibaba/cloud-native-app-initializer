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

package com.alibaba.initializer.generation.extension.architecture.layered;

import io.spring.initializr.generator.spring.documentation.HelpDocument;
import io.spring.initializr.generator.spring.documentation.HelpDocumentCustomizer;

/**
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 */
public class LayeredHelpCustomizer implements HelpDocumentCustomizer {

    @Override
    public void customize(HelpDocument document) {
        document.gettingStarted().addGuideLink("https://github.com/alibaba/p3c", "《阿里巴巴Java开发手册》");
    }
}
