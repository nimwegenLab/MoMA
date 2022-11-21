package com.jug.export;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class AbstractColumn<T> {

    private final Class<T> javaGenericType;
//    private Class<T> springGenericType;

    public AbstractColumn() {

        // Pure Java solution to get generic type
        Class<?> thisClass = getClass();
        Type genericSuperclass = thisClass.getGenericSuperclass();
        ParameterizedType parameterizedGenericSuperclass = (ParameterizedType) genericSuperclass;
        Type[] typeArgs = parameterizedGenericSuperclass.getActualTypeArguments();
        this.javaGenericType = (Class<T>) typeArgs[0];

//        // Spring solution to get generic type
//        this.springGenericType = (Class<T>) GenericTypeResolver.resolveTypeArgument(getClass(), AbstractFoo.class);
    }

    protected Class<?> getJavaGenericType() {
        return this.javaGenericType;
    }

//    protected Class<?> getSpringGenericType() {
//        return this.springGenericType;
//    }
}
