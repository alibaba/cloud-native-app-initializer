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

package com.alibaba.initializer.protocol.git;

import java.nio.file.Path;
import java.time.ZoneId;
import java.util.Date;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.initializer.core.ProjectGenerationRequest;
import com.alibaba.initializer.core.ProjectGenerationResult;
import com.alibaba.initializer.core.ProjectGenerator;
import com.alibaba.initializer.protocol.resolver.RequestConverter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.resolver.RepositoryResolver;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * output generated code with git protocol
 *
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 */
@Slf4j
public class ProjectGenerationResolver implements RepositoryResolver<HttpServletRequest> {

    @Autowired
    private ProjectGenerator routeGenerator;

    @Autowired
    private RequestConverter requestConverter;

    @Override
    public Repository open(HttpServletRequest servletRequest, String name) throws RepositoryNotFoundException {
        ProjectGenerationRequest request = requestConverter.convert(servletRequest);

        ProjectGenerationResult result = executeGeneration(request);

        return transformGitRepo(name, result.getProjectRoot().resolve(result.getName()));
    }

    private Repository transformGitRepo(String name, Path repoRoot) throws RepositoryNotFoundException {
        try {
            FileRepositoryBuilder builder = new FileRepositoryBuilder()
                    .setGitDir(repoRoot.resolve(".git").toFile())
                    .setWorkTree(repoRoot.toFile())
                    .setup();
            Repository resp = new FileRepository(builder);
            resp.create(false);

            Git git = new Git(resp);
            git.add().addFilepattern(".").call();
            CommitCommand commit = git.commit();
            commit.setAuthor(new PersonIdent("theonefx", "chenxilzx1@gmail.com", new Date(0), TimeZone.getTimeZone(ZoneId.systemDefault())));
            commit.setCommitter(new PersonIdent("theonefx", "chenxilzx1@gmail.com", new Date(0), TimeZone.getTimeZone(ZoneId.systemDefault())));
            commit.setMessage("project init").call();

            git.push();

            return resp;
        } catch (Exception e) {
            log.error("trans project 2 git patten error", e);
            throw new RepositoryNotFoundException(name, e);
        }
    }

    private ProjectGenerationResult executeGeneration(ProjectGenerationRequest request) {
        ProjectGenerationResult result = routeGenerator.generate(request);
        return result;
    }
}
