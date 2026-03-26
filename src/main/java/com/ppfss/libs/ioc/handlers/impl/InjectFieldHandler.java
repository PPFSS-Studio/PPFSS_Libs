// PPFSS_Libs Plugin
// Авторские права (c) 2026 PPFSS
// Лицензия: MIT

package com.ppfss.libs.ioc.handlers.impl;

import com.ppfss.libs.ioc.IoCContainer;
import com.ppfss.libs.ioc.annotation.Inject;
import com.ppfss.libs.ioc.handlers.FieldAnnotationHandler;

import java.lang.reflect.Field;

public class InjectFieldHandler implements FieldAnnotationHandler<Inject> {

    @Override
    public Class<Inject> getAnnotation() {
        return Inject.class;
    }

    @Override
    public void handle(
            IoCContainer container,
            Object instance,
            Field field,
            Inject annotation
    ) {

        Object dependency = container.get(field.getType());

        try {

            field.setAccessible(true);
            field.set(instance, dependency);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}