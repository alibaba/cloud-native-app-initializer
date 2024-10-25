package com.alibaba.initializer.generation.extension.build.gradle;

import com.alibaba.initializer.metadata.Module;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 */
@Aspect
public class GradleWrapperContributorAspect {

    @Autowired
    private Module module;

    @Around("target(io.spring.initializr.generator.spring.build.gradle.GradleWrapperContributor) " +
            "&& execution(void io.spring.initializr.generator.project.contributor.MultipleResourcesProjectContributor.contribute(..))")
    public Object arround(ProceedingJoinPoint joinpoint) throws Throwable {
        if (!module.isRoot()) {
            return null;
        }

        return joinpoint.proceed();
    }
}
