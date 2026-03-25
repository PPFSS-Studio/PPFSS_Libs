package com.ppfss.libs.ioc.handlers.impl;

import com.google.gson.GsonBuilder;
import com.ppfss.libs.ioc.IoCContainer;
import com.ppfss.libs.ioc.handlers.ClassAnnotationHandler;
import com.ppfss.libs.serialization.GsonAdapter;

public class GsonAdapterHandler implements ClassAnnotationHandler<GsonAdapter> {

    @Override
    public Class<GsonAdapter> getAnnotation() {
        return GsonAdapter.class;
    }

    @Override
    public void handle(IoCContainer container, Class<?> clazz, GsonAdapter annotation) {
        GsonBuilder builder = container.get(GsonBuilder.class);
        try {
            Object adapterInstance = container.create(clazz);
            builder.registerTypeAdapter(annotation.value(), adapterInstance);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to register Gson adapter: " + clazz.getName(), ex);
        }
    }
}