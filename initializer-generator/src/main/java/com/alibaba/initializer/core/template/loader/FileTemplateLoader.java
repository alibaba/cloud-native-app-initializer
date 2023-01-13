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

package com.alibaba.initializer.core.template.loader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import com.alibaba.initializer.core.template.CodeTemplate;
import com.alibaba.initializer.core.template.CodeTemplateRepo;
import com.alibaba.initializer.core.template.CodeTemplateRepoLoader;
import com.google.common.collect.Lists;
import lombok.SneakyThrows;

/**
 * load one file from remote URL
 *
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 */
public class FileTemplateLoader implements CodeTemplateRepoLoader {

	@SneakyThrows
	@Override
	public CodeTemplateRepo load(String uriStr) {
		URL url = new URL(uriStr);

		List<CodeTemplate> templates = Lists.newArrayList();

		templates.add(new CodeTemplate(null, url.getFile()) {

			@Override
			public Reader getReader() throws IOException {
				URLConnection conn = url.openConnection();

				return new InputStreamReader(conn.getInputStream());
			}

			@Override
			public void cleanup() {
			}
		});

		return new CodeTemplateRepo(url.toURI(), templates);
	}

	@Override
	public String getProtocol() {
		return "file";
	}
}
