package com.alibaba.initializer.generation.extension.build.gradle;

import com.alibaba.initializer.metadata.Architecture;
import com.alibaba.initializer.metadata.Module;
import com.alibaba.initializer.project.InitializerProjectDescription;
import io.spring.initializr.generator.buildsystem.gradle.GradleBuild;
import io.spring.initializr.generator.buildsystem.gradle.GradleBuildSettings;
import io.spring.initializr.generator.buildsystem.gradle.GradleBuildWriter;
import io.spring.initializr.generator.buildsystem.gradle.StandardGradlePlugin;
import io.spring.initializr.generator.io.IndentingWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 * @see GradleBuildWriter
 */
public class EnhancedGradleBuildWriter {

    @Autowired
    private GradleBuildWriter delegated;

    @Autowired
    private Module module;

    @Autowired
    private InitializerProjectDescription description;

    public void writeTo(IndentingWriter writer, GradleBuild build) {
        Architecture arch = description.getArchitecture();

        if (CollectionUtils.isEmpty(arch.getSubModules())) {
            delegated.writeTo(writer, build);
            return;
        }

        GradleBuildSettings settings = build.getSettings();
        if (module.isRoot()) {
            run("writeImports", writer, build.tasks());
            run("writeBuildscript", writer, build);
            run("writePlugins", writer, build);
            run("writeConfigurations", writer, build.configurations());
            run("writeProperties", writer, build.properties());
            writer.println();

            writer.println("allprojects {");
            writer.indented(() -> {
                run("writeProperty", writer, "group", settings.getGroup());
                run("writeProperty", writer, "version", settings.getVersion());
                run("writeRepositories", writer, build);
            });
            writer.println("}");
            writer.println();

            writer.println("subprojects {");
            writer.indented(() -> {
                writeApplies(writer, build);
                run("writeJavaSourceCompatibility", writer, settings);
                run("writeBoms", writer, build);
                run("writeTasks", writer, build.tasks());
            });
            writer.println("}");
        } else {
            writeDependencies(writer, build);
        }
    }

    private void writeApplies(IndentingWriter writer, GradleBuild build) {
        List<StandardGradlePlugin> extractStandardPlugins = run("extractStandardPlugin", build);
        extractStandardPlugins.forEach(plugin -> writer.println("apply plugin: '" + plugin.getId() + "'"));
    }

    private void writeDependencies(IndentingWriter writer, GradleBuild build) {
        run("writeDependencies", writer, build);
    }

    private <T> T run(String methodName, Object... args) {
        Method method = null;
        Class<?> clazz = delegated.getClass();
        for (; ; ) {
            Method[] methods = clazz.getDeclaredMethods();
            for (Method m : methods) {
                if (m.getName().equalsIgnoreCase(methodName)) {
                    method = m;
                    break;
                }
            }
            if (method != null) {
                break;
            }
            clazz = clazz.getSuperclass();
            if (clazz == Object.class) {
                break;
            }
        }

        if (method == null) {
            throw new RuntimeException("can't find method " + methodName);
        }

        try {
            method.setAccessible(true);
            return (T) method.invoke(delegated, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
