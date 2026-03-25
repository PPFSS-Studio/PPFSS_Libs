// PPFSS_Libs Plugin
// Авторские права (c) 2026 PPFSS
// Лицензия: MIT

package com.ppfss.libs.plugin;

import com.ppfss.libs.ioc.PluginIoC;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class PPFSS_Template extends JavaPlugin {
    @Getter
    private static PluginIoC pluginIoC;

    protected static Logger log;

    @Override
    public void onLoad() {
        log = LoggerFactory.getLogger(this.getClass());

        pluginIoC = new PluginIoC(this);
    }
}
