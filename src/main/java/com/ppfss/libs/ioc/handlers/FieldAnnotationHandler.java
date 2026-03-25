// PPFSS_Libs Plugin
// Авторские права (c) 2026 PPFSS
// Лицензия: MIT

package com.ppfss.libs.ioc.handlers;

import com.ppfss.libs.ioc.IoCContainer;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

public interface FieldAnnotationHandler<A extends Annotation> {

    Class<A> getAnnotation();

    void handle(
            IoCContainer container,
            Object instance,
            Field field,
            A annotation
    );

}