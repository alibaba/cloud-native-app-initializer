package com.alibaba.initializer.generation.extension.codes.language;

import com.alibaba.initializer.generation.InitializerProjectGenerationConfiguration;
import com.alibaba.initializer.generation.condition.ConditionalOnModule;
import io.spring.initializr.generator.condition.ConditionalOnLanguage;
import io.spring.initializr.generator.language.groovy.GroovyLanguage;
import io.spring.initializr.generator.spring.code.groovy.GroovyProjectGenerationConfiguration;

import org.springframework.context.annotation.Import;

/**
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 */
@InitializerProjectGenerationConfiguration
@ConditionalOnModule(main = true)
@ConditionalOnLanguage(GroovyLanguage.ID)
@Import(GroovyProjectGenerationConfiguration.class)
public class ArchedGroovyProjectGenerationConfiguration {
}
