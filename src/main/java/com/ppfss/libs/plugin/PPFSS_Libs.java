// PPFSS_Libs Plugin
// Авторские права (c) 2025 PPFSS
// Лицензия: MIT
package com.ppfss.libs.plugin;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

@SuppressWarnings("unused")
public final class PPFSS_Libs extends PPFSS_Template {
    @Getter
    public static PPFSS_Libs instance;

    @Override
    public void onEnable() {
        log.info("[PPFSSLibs] Enabled");

        log.info(getPluginIoC().get(JavaPlugin.class).getName());
    }

}
