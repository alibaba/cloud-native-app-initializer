package com.alibaba.initializer.generation.extension.build.gradle;

import com.alibaba.initializer.metadata.Architecture;
import com.alibaba.initializer.metadata.Module;
import com.alibaba.initializer.project.InitializerProjectDescription;
import io.spring.initializr.generator.buildsystem.Dependency;
import io.spring.initializr.generator.buildsystem.gradle.GradleBuild;
import io.spring.initializr.generator.spring.build.BuildCustomizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;

import java.util.List;

/**
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 */
public class MulitModuleGradleBuildCustoimzer implements BuildCustomizer<GradleBuild> {

    @Autowired
    private Module module;

    @Autowired
    protected InitializerProjectDescription description;

    @Override
    public void customize(GradleBuild build) {

        Architecture arch = description.getArchitecture();

        if (module.isMain()) {
            // main module depend all other submodules
            List<Module> subModules = arch.getSubModules();
            for (Module subModule : subModules) {
                if (subModule == module) {
                    continue;
                }
                addModuleDependency(build, subModule.getName());
            }
        } else {
            List<String> dependModules = module.getDependModules();
            if (dependModules != null) {
                for (String dependModule : dependModules) {
                    addModuleDependency(build, dependModule);
                }
            }
        }
    }

    private void addModuleDependency(GradleBuild build, String subModule) {
        build.dependencies().add(subModule, Dependency.withCoordinates(description.getGroupId(), subModule).type("submodule"));
    }


    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
