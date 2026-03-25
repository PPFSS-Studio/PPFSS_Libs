// PPFSS_Libs Plugin
// Авторские права (c) 2026 PPFSS
// Лицензия: MIT

package com.ppfss.libs.ioc;

import com.ppfss.libs.config.YamlConfig;
import com.ppfss.libs.config.YamlConfigLoader;
import com.ppfss.libs.ioc.handlers.impl.ComponentHandler;
import com.ppfss.libs.ioc.handlers.impl.GsonAdapterHandler;
import com.ppfss.libs.ioc.handlers.impl.InjectFieldHandler;
import com.ppfss.libs.util.AnnotationScanner;
import lombok.Getter;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Modifier;
import java.util.Set;

@SuppressWarnings("unused")
public class PluginIoC {

    /**
     * -- GETTER --
     *  Получить IoCContainer для ручного доступа
     */
    @Getter
    private final IoCContainer container = new IoCContainer();

    @SuppressWarnings("unchecked")
    public PluginIoC(JavaPlugin plugin) {

        registerInstance(JavaPlugin.class, plugin);
        registerInstance(Plugin.class, plugin);

        registerDefaultHandlers();

        Set<Class<?>> classes = AnnotationScanner.scanPlugin(plugin);


        for (Class<?> clazz : classes) {
            if (YamlConfig.class.isAssignableFrom(clazz) && !Modifier.isAbstract(clazz.getModifiers())) {
                YamlConfig cfg = container.create((Class<YamlConfig>) clazz);
                container.registerInstance((Class<YamlConfig>) clazz, cfg);
            }
        }

        YamlConfigLoader configLoader = new YamlConfigLoader(plugin, container);
        registerInstance(YamlConfigLoader.class, configLoader);

        container.initialize(classes);
    }

    /**
     * Регистрация стандартных обработчиков аннотаций
     */
    private void registerDefaultHandlers() {

        // Классы с @Component
        container.registerClassHandler(new ComponentHandler());
        container.registerClassHandler(new GsonAdapterHandler());

        // Поля с @Inject
        container.registerFieldHandler(new InjectFieldHandler());

    }

    /**
     * Получить instance класса из IoC
     */
    public <T> T get(Class<T> type) {
        return container.get(type);
    }

    /**
     * Зарегистрировать вручную конкретный компонент
     */
    public void registerComponent(Class<?> clazz) {
        container.registerComponent(clazz);
    }

    /**
     * Зарегистрировать реализацию интерфейса
     */
    public <T> void registerImplementation(Class<T> type, Class<? extends T> impl) {
        container.registerImplementation(type, impl);
    }

    /**
     * Зарегистрировать уже созданный instance
     */
    public <T> void registerInstance(Class<T> type, T instance) {
        container.registerInstance(type, instance);
    }
}