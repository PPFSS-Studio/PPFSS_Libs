// PPFSS_Libs Plugin
// Авторские права (c) 2026 PPFSS
// Лицензия: MIT

package com.ppfss.libs.util;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;

@Slf4j
public final class AnnotationScanner {
    private AnnotationScanner() {
    }

    public static Set<Class<?>> scanPlugin(JavaPlugin plugin) {
        Set<Class<?>> classes = new HashSet<>();

        ClassLoader pluginLoader = plugin.getClass().getClassLoader();

        try (ScanResult scanResult = new ClassGraph()
                .enableClassInfo()

                .overrideClassLoaders(pluginLoader)

                .ignoreParentClassLoaders()

                .rejectPackages(
                        "org.bukkit",
                        "net.minecraft",
                        "com.mojang",
                        "org.spigotmc",
                        "io.papermc",
                        "io.github.classgraph",
                        "nonapi.io.github.classgraph",
                        "org.slf4j",
                        "ch.qos.logback",
                        "com.google.common",
                        "com.google.gson",
                        "org.apache.commons",
                        "org.jetbrains.annotations"
                )

                .scan()) {

            scanResult.getAllClasses().forEach(classInfo -> {
                try {
                    classes.add(classInfo.loadClass());
                } catch (Throwable throwable) {
                    log.warn("Failed to load class {}", classInfo.getName(), throwable);
                }
            });
        }

        return classes;
    }
}
