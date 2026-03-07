package com.ppfss.libs.util;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.annotation.Annotation;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public final class PluginDiscovery {
    private PluginDiscovery() {
    }

    public static List<Class<?>> scanPluginForAnnotation(JavaPlugin plugin, Class<? extends Annotation> annotationClass) {
        if (plugin == null || annotationClass == null) {
            return Collections.emptyList();
        }
        Path pluginJar = resolvePluginJar(plugin);
        if (pluginJar == null) {
            return Collections.emptyList();
        }

        try (ScanResult scan = new ClassGraph()
                .overrideClassLoaders(plugin.getClass().getClassLoader())
                .overrideClasspath(pluginJar.toFile())
                .enableClassInfo()
                .enableAnnotationInfo()
                .scan()) {
            return scan.getClassesWithAnnotation(annotationClass.getName()).loadClasses(true);
        } catch (Exception exception) {
            LogUtils.error("Failed to scan plugin for annotation " + annotationClass.getName(), exception);
            return Collections.emptyList();
        }
    }

    public static List<Class<?>> scanPackageForAnnotation(ClassLoader loader, String basePackage, Class<? extends Annotation> annotationClass) {
        if (loader == null || annotationClass == null || basePackage == null || basePackage.isBlank()) {
            return Collections.emptyList();
        }

        try (ScanResult scan = new ClassGraph()
                .overrideClassLoaders(loader)
                .acceptPackages(basePackage)
                .enableClassInfo()
                .enableAnnotationInfo()
                .scan()) {
            return scan.getClassesWithAnnotation(annotationClass.getName()).loadClasses(true);
        } catch (Exception exception) {
            LogUtils.error("Failed to scan package " + basePackage + " for annotation " + annotationClass.getName(), exception);
            return Collections.emptyList();
        }
    }

    private static Path resolvePluginJar(JavaPlugin plugin) {
        try {
            return Path.of(plugin.getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (URISyntaxException exception) {
            LogUtils.error("Unable to resolve plugin JAR for " + plugin.getName(), exception);
            return null;
        }
    }
}
