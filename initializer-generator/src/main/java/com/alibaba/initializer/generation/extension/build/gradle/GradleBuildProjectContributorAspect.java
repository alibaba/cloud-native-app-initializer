package com.alibaba.initializer.generation.extension.build.gradle;

import io.spring.initializr.generator.buildsystem.gradle.GradleBuild;
import io.spring.initializr.generator.io.IndentingWriter;
import io.spring.initializr.generator.io.IndentingWriterFactory;
import io.spring.initializr.generator.spring.build.gradle.GradleBuildProjectContributor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 * @see GradleBuildProjectContributor
 */
@Aspect
public class GradleBuildProjectContributorAspect {

    @Autowired
    private EnhancedGradleBuildWriter enhancedGradleBuildWriter;

    @Autowired
    private GradleBuild build;

    @Autowired
    private IndentingWriterFactory indentingWriterFactory;

    @Around("target(io.spring.initializr.generator.spring.build.gradle.GradleBuildProjectContributor) && execution(* *.contribute(..))")
    public void contribute(ProceedingJoinPoint joinPoint) throws IOException, NoSuchFieldException, IllegalAccessException {
        Path projectRoot = (Path) joinPoint.getArgs()[0];

        Field buildFileNameField = joinPoint.getTarget().getClass().getDeclaredField("buildFileName");
        buildFileNameField.setAccessible(true);
        String buildFileName = (String) buildFileNameField.get(joinPoint.getTarget());

        Path buildGradle = Files.createFile(projectRoot.resolve(buildFileName));
        doWriteBuild(Files.newBufferedWriter(buildGradle));
    }

    @Around("target(io.spring.initializr.generator.spring.build.gradle.GradleBuildProjectContributor) && execution(* *.writeBuild(..))")
    public void writeBuild(ProceedingJoinPoint joinPoint) throws IOException {
        doWriteBuild((Writer) joinPoint.getArgs()[0]);
    }

    private void doWriteBuild(Writer out) throws IOException {
        try (IndentingWriter writer = indentingWriterFactory.createIndentingWriter("gradle", out)) {
            enhancedGradleBuildWriter.writeTo(writer, build);
        }
    }

}
