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

package com.alibaba.initializer.generation.extension.build;

import io.spring.initializr.generator.buildsystem.BillOfMaterials;
import io.spring.initializr.generator.buildsystem.DependencyScope;
import lombok.Getter;

/**
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 */
@Getter
public class DependencyBillOfMaterials extends BillOfMaterials {

    private final String type;

    private final DependencyScope scope;

    protected DependencyBillOfMaterials(Builder builder) {
        super(builder);
        this.type = builder.type;
        this.scope = builder.scope;
    }

    /**
     * Initialize a new BOM {@link BillOfMaterials.Builder} with the specified coordinates.
     *
     * @param groupId    the group ID of the bom
     * @param artifactId the artifact ID of the bom
     * @return a new builder
     */
    public static Builder withCoordinates(String groupId, String artifactId) {
        return new Builder(groupId, artifactId);
    }

    /**
     * Builder for a Bill of Materials.
     */
    public static class Builder extends BillOfMaterials.Builder {

        private String type;

        private DependencyScope scope;

        protected Builder(String groupId, String artifactId) {
            super(groupId, artifactId);
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder scope(DependencyScope scope) {
            this.scope = scope;
            return this;
        }

        /**
         * Build a {@link BillOfMaterials} with the current state of this builder.
         *
         * @return a {@link BillOfMaterials}
         */
        public DependencyBillOfMaterials build() {
            return new DependencyBillOfMaterials(this);
        }
    }
}
