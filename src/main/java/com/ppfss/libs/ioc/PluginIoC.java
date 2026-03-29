// PPFSS_Libs Plugin
// Авторские права (c) 2026 PPFSS
// Лицензия: MIT

package com.ppfss.libs.ioc;

import com.ppfss.libs.command.AbstractCommand;
import com.ppfss.libs.config.YamlConfig;
import com.ppfss.libs.config.YamlConfigLoader;
import com.ppfss.libs.ioc.handlers.impl.AutoListenerHandler;
import com.ppfss.libs.ioc.handlers.impl.ComponentHandler;
import com.ppfss.libs.ioc.handlers.impl.GsonAdapterHandler;
import com.ppfss.libs.ioc.handlers.impl.InjectFieldHandler;
import com.ppfss.libs.util.AnnotationScanner;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Modifier;
import java.util.*;


@Slf4j
@SuppressWarnings("unused")
public class PluginIoC {

    /**
     * -- GETTER --
     *  Получить IoCContainer для ручного доступа
     */
    @Getter
    private final IoCContainer container = new IoCContainer();
    private final Plugin plugin;
    private final PluginManager pluginManager;
    private final Set<Class<? extends Listener>> listeners = new HashSet<>();
    private final YamlConfigLoader configLoader;

    /**
     * Инициализирует IoC контейнер для плагина
     * <p>
     * Выполняет:<p>
     * - Регистрацию базовых зависимостей (Plugin, JavaPlugin)<p>
     * - Сканирование классов плагина<p>
     * - Регистрацию YamlConfig<p>
     * - Инициализацию IoC контейнера<p>
     * - Регистрацию Listener и Command<p>
     * <p>
     * @param plugin JavaPlugin экземпляр плагина
     */
    @SuppressWarnings("unchecked")
    public PluginIoC(JavaPlugin plugin) {
        this.plugin = plugin;
        this.pluginManager = plugin.getServer().getPluginManager();

        registerInstance(JavaPlugin.class, plugin);
        registerInstance(Plugin.class, plugin);
        registerInstance(PluginIoC.class, this);

        registerDefaultHandlers();

        Set<Class<?>> classes = AnnotationScanner.scanPlugin(plugin);

        configLoader = new YamlConfigLoader(plugin, container);
        registerInstance(YamlConfigLoader.class, configLoader);

        List<Class<? extends YamlConfig>> configClasses = new ArrayList<>();
        List<Class<? extends AbstractCommand>> commandClasses = new ArrayList<>();

        for (Class<?> clazz : classes) {
            if (Modifier.isAbstract(clazz.getModifiers())) {
                continue;
            }

            if (YamlConfig.class.isAssignableFrom(clazz)) {
                configClasses.add((Class<? extends YamlConfig>) clazz);
            }

            if (AbstractCommand.class.isAssignableFrom(clazz)) {
                commandClasses.add((Class<? extends AbstractCommand>) clazz);
            }
        }

        // Регистрируем конфиги
        configClasses.forEach(this::registerYamlConfig);

        // Инициализируем контейнер
        container.initialize(classes);

        // Регистрируем слушателей и команды
        listeners.forEach(this::registerListener);
        commandClasses.forEach(this::registerCommand);
    }


    /**
     * Создаёт и регистрирует команду через IoC контейнер
     * <p>
     * Если команда уже существует в контейнере — используется существующий instance
     * Если команда ещё не зарегистрирована — выполняется регистрация
     */
    private <T extends AbstractCommand> void registerCommand(Class<T> clazz){
        T command = container.getIfExists(clazz);

        if (command == null){
            command = container.create(clazz);

            container.registerInstance(clazz, command);
        }

        if (!command.isRegistered()){
            command.register(plugin);
        }
    }

    /**
     * <p> Загружает <b>YamlConfig</b> через <b>YamlConfigLoader</b></p>
     * <p> и регистрирует в <b>IoC</b> контейнере </p>
     * <p>
     * Если конфиг уже зарегистрирован — повторная загрузка не выполняется
     */
    private <T extends YamlConfig> void registerYamlConfig(Class<T> clazz) {
        // TODO: LazyLoad
        T config = container.getIfExists(clazz);

        if  (config != null) {
            return;
        }

        config = configLoader.loadFromClass(clazz);

        container.registerInstance(clazz, config);
    }

    private <T extends Listener> void registerListener(Class<T> clazz) {
        T listener = container.getIfExists(clazz);

        if (listener != null){
            pluginManager.registerEvents(listener, plugin);

            log.info("Registered listener for plugin {}", clazz.getName());
        }
    }

    /**
     * Регистрация стандартных обработчиков аннотаций
     */
    private void registerDefaultHandlers() {

        // Классы
        container.registerClassHandler(new ComponentHandler());
        container.registerClassHandler(new GsonAdapterHandler());
        container.registerClassHandler(new AutoListenerHandler(listeners));

        // Поля
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