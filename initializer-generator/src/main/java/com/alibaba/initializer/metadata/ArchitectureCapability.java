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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.spring.initializr.metadata.DefaultMetadataElement;
import io.spring.initializr.metadata.Defaultable;
import io.spring.initializr.metadata.ServiceCapability;
import io.spring.initializr.metadata.ServiceCapabilityType;
import org.apache.commons.lang3.StringUtils;

/**
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 */
public class ArchitectureCapability extends ServiceCapability<List<Architecture>>
		implements Defaultable<Architecture> {

	private final List<Architecture> content = new CopyOnWriteArrayList<>();

	@JsonCreator
	ArchitectureCapability(@JsonProperty("id") String id) {
		this(id, null, null);
	}

	public ArchitectureCapability(String id, String title, String description) {
		super(id, ServiceCapabilityType.SINGLE_SELECT, title, description);
	}

	@Override
	public List<Architecture> getContent() {
		return this.content;
	}

	/**
	 * Return the default element of this capability.
	 */
	@Override
	public Architecture getDefault() {
		return this.content.stream().filter(DefaultMetadataElement::isDefault).findFirst()
				.orElse(null);
	}

	/**
	 * Return the element with the specified id or {@code null} if no such element exists.
	 * @param id the ID of the element to find
	 * @return the element or {@code null}
	 */
	public Architecture get(String id) {
		Architecture none = null;
		for (Architecture architecture : this.content) {
			if (StringUtils.equals(architecture.getId(), id)) {
				return architecture;
			}
			if (StringUtils.equals(architecture.getId(), "none")) {
				none = architecture;
			}
		}

		return none;
	}

	@Override
	public void merge(List<Architecture> otherContent) {
		this.content.clear();
		this.content.addAll(otherContent);
	}

}
