// OntaDev_Libs Plugin
// Авторские права (c) 2026 OntaDev
// Лицензия: MIT

package com.ontadev.libs.service;

import com.ontadev.libs.exception.AddonNotFoundException;
import com.ontadev.libs.ioc.PluginIoC;
import com.ontadev.libs.ioc.annotation.injection.Inject;
import com.ontadev.libs.ioc.annotation.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import net.luckperms.api.LuckPerms;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

@Slf4j
@Component(priority = 100)
public class AddonProvider {
    private final PluginIoC pluginIoC;
    private final Plugin plugin;

    @Inject
    public AddonProvider(JavaPlugin plugin, PluginIoC pluginIoC) {
        this.pluginIoC = pluginIoC;
        this.plugin = plugin;

        registerAddon(LuckPerms.class, "LuckPerms", true);
    }

    @SuppressWarnings("SameParameterValue")
    private <T> void registerAddon(
            Class<T> clazz,
            String name,
            boolean required
    ) {
        RegisteredServiceProvider<T> provider =
                plugin.getServer()
                        .getServicesManager()
                        .getRegistration(clazz);

        if (provider == null) {

            if (required) {
                throw new AddonNotFoundException(
                        name + " not found"
                );
            }

            log.warn("{} not found", name);
            return;
        }

        pluginIoC.registerInstance(
                clazz,
                provider.getProvider()
        );
    }
}
