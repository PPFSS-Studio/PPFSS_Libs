// PPFSS_Libs Plugin
// Авторские права (c) 2026 PPFSS
// Лицензия: MIT

package com.ppfss.libs.ioc.handlers.impl;

import com.ppfss.libs.ioc.IoCContainer;
import com.ppfss.libs.ioc.annotation.Component;
import com.ppfss.libs.ioc.handlers.ClassAnnotationHandler;

public class ComponentHandler implements ClassAnnotationHandler<Component> {

    @Override
    public Class<Component> getAnnotation() {
        return Component.class;
    }

    @Override
    public void handle(IoCContainer container, Class<?> clazz, Component annotation) {
        container.registerComponent(clazz);
    }

}