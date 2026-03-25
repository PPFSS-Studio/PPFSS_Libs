// PPFSS_Libs Plugin 
// Авторские права (c) 2026 PPFSS
// Лицензия: MIT

package com.ppfss.libs.ioc.handlers.impl;

import com.ppfss.libs.ioc.IoCContainer;
import com.ppfss.libs.ioc.annotation.AutoListener;
import com.ppfss.libs.ioc.handlers.ClassAnnotationHandler;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.event.Listener;

import java.util.Set;

@SuppressWarnings("unchecked")
@Slf4j
public class AutoListenerHandler implements ClassAnnotationHandler<AutoListener> {
    private final Set<Class<? extends Listener>> listeners;

    public AutoListenerHandler(Set<Class<? extends Listener>> classes) {
        listeners = classes;
    }

    @Override
    public Class<AutoListener> getAnnotation() {
        return AutoListener.class;
    }

    @Override
    public void handle(IoCContainer container, Class<?> clazz, AutoListener annotation) {
        if (!Listener.class.isAssignableFrom(clazz)) {
            log.error("Class {} is not a Listener", clazz.getName());
            return;
        }

        listeners.add((Class<? extends Listener>) clazz);
    }


}
