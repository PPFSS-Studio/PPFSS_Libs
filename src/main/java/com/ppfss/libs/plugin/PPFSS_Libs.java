// PPFSS_Libs Plugin
// Авторские права (c) 2025 PPFSS
// Лицензия: MIT
package com.ppfss.libs.plugin;

import com.ppfss.libs.config.YamlConfigLoader;
import com.ppfss.libs.serialization.GsonAdapter;
import com.ppfss.libs.util.LogUtils;
import com.ppfss.libs.util.PluginDiscovery;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unused")
public final class PPFSS_Libs extends JavaPlugin {
    @Getter
    public static PPFSS_Libs instance;

    private static final String INTERNAL_ADAPTER_PACKAGE = "com.ppfss.libs.serialization.adapters";

    private final Set<Class<?>> registeredAdapters = ConcurrentHashMap.newKeySet();
    @Getter
    private YamlConfigLoader configLoader;

    @Override
    public void onEnable() {
        instance = this;
        addAdapters(PluginDiscovery.scanPackageForAnnotation(getClass().getClassLoader(), INTERNAL_ADAPTER_PACKAGE, GsonAdapter.class), false);
        configLoader = new YamlConfigLoader(this, new ArrayList<>(registeredAdapters));

        LogUtils.info("[PPFSSLibs] Enabled");
    }

    @Override
    public void onLoad() {
        LogUtils.init(this);
    }

    public void registerMe(JavaPlugin plugin) {
        if (plugin == null || configLoader == null) {
            return;
        }

        List<Class<?>> adapters = PluginDiscovery.scanPluginForAnnotation(plugin, GsonAdapter.class);
        addAdapters(adapters, true);
    }

    private void addAdapters(Collection<Class<?>> adapters, boolean reloadIfReady) {
        if (adapters == null || adapters.isEmpty()) {
            return;
        }

        List<Class<?>> newlyAdded = new ArrayList<>();
        for (Class<?> adapter : adapters) {
            if (adapter != null && registeredAdapters.add(adapter)) {
                newlyAdded.add(adapter);
            }
        }

        if (!newlyAdded.isEmpty() && reloadIfReady && configLoader != null) {
            configLoader.reloadAdapters(new ArrayList<>(registeredAdapters));
        }
    }

    public Collection<Class<?>> getRegisteredAdapters() {
        return Collections.unmodifiableSet(registeredAdapters);
    }

}
