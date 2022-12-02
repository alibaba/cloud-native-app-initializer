package com.alibaba.initializer.generation.extension.codes.language;

import com.alibaba.initializer.generation.InitializerProjectGenerationConfiguration;
import com.alibaba.initializer.generation.condition.ConditionalOnModule;
import io.spring.initializr.generator.condition.ConditionalOnLanguage;
import io.spring.initializr.generator.language.kotlin.KotlinLanguage;
import io.spring.initializr.generator.spring.code.kotlin.KotlinProjectGenerationConfiguration;

import org.springframework.context.annotation.Import;

/**
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 */
@InitializerProjectGenerationConfiguration
@ConditionalOnModule(main = true)
@ConditionalOnLanguage(KotlinLanguage.ID)
@Import(KotlinProjectGenerationConfiguration.class)
public class ArchedKotlinProjectGenerationConfiguration {

}
