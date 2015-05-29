package com.igumnov.common;

import com.igumnov.common.dependency.DependencyException;
import com.igumnov.common.dependency.Inject;
import com.igumnov.common.dependency.Named;
import org.apache.commons.dbcp2.BasicDataSource;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Dependency {

    private static ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private static HashMap<String, Object> singlentons = new HashMap<>();

    public static void scan(String prefix) throws IOException, URISyntaxException, ClassNotFoundException, IllegalAccessException, InstantiationException, DependencyException {
        ArrayList<String> classes = Reflection.getClassNamesFromPackage(prefix);

        for (String c : classes) {
            Class classObj = Class.forName(c);
            for (Annotation annotation : classObj.getDeclaredAnnotations()) {
                if (annotation.annotationType().equals(Named.class)) {
                    String nameInjection = ((Named) annotation).value();
                    add(classObj.newInstance(), nameInjection);
                }
            }
        }

        for (Object obj : singlentons.values()) {
            inject(obj);
        }
    }

    public static void inject(Object obj) throws IllegalAccessException, DependencyException {

        for (Field field : obj.getClass().getDeclaredFields()) {
            for (Annotation annotation : field.getDeclaredAnnotations()) {
                if (annotation.annotationType().equals(Inject.class)) {
                    String nameInjection = ((Inject) annotation).value();
                    field.setAccessible(true);
                    field.set(obj, get(nameInjection));
                }
            }
        }

    }

    public static Object find(String name) throws DependencyException {
        return get(name);
    }

    public static Object createInstance(String name, Class className) throws DependencyException, IllegalAccessException, InstantiationException {
        Object obj = className.newInstance();
        inject(obj);
        return add(obj, name);
    }


    public static void bind(String name, Object object) throws DependencyException {
        add(object, name);
    }

    private static Object add(Object injectionObject, String name) throws DependencyException {
        injectionObject.toString();
        try {
            lock.writeLock().lock();
            if (singlentons.containsKey(name)) {
                throw new DependencyException("Object already injected with name: " + name);
            } else {
                singlentons.put(name, injectionObject);
            }

        } finally {
            lock.writeLock().unlock();
        }
        return injectionObject;
    }


    private static Object get(String name) throws DependencyException {
        try {
            lock.readLock().lock();
            if (singlentons.containsKey(name)) {
                return singlentons.get(name);
            } else {
                throw new DependencyException("Object absent with name: " + name);
            }
        } finally {
            lock.readLock().unlock();
        }
    }

}
