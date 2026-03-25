// PPFSS_Libs Plugin
// Авторские права (c) 2026 PPFSS
// Лицензия: MIT

package com.ppfss.libs.ioc.handlers;

import com.ppfss.libs.ioc.IoCContainer;

import java.lang.annotation.Annotation;

public interface ClassAnnotationHandler<A extends Annotation> {

    Class<A> getAnnotation();

    void handle(IoCContainer container, Class<?> clazz, A annotation);

}