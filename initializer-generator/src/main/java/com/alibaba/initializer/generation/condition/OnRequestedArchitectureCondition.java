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

package com.alibaba.initializer.generation.condition;

import com.alibaba.initializer.project.InitializerProjectDescription;
import io.spring.initializr.generator.condition.ProjectGenerationCondition;
import io.spring.initializr.generator.project.ProjectDescription;
import org.apache.commons.lang3.StringUtils;

import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 */
public class OnRequestedArchitectureCondition extends ProjectGenerationCondition {

	@Override
	protected boolean matches(ProjectDescription description, ConditionContext context,
			AnnotatedTypeMetadata metadata) {

		String id = (String) metadata.getAnnotationAttributes(
				ConditionalOnRequestedArchitecture.class.getName()).get("value");

		if (description instanceof InitializerProjectDescription) {
			InitializerProjectDescription aDescription = (InitializerProjectDescription) description;
			return StringUtils.equals(aDescription.getArchitecture().getId(), id);
		}
		return false;
	}
}
