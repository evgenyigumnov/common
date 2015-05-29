package com.igumnov.common;


import com.igumnov.common.orm.Id;
import com.igumnov.common.reflection.ReflectionException;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class Reflection {


    // TODO Add unit test
    public static ArrayList<String> getClassNamesFromPackage(String packageName) throws IOException, URISyntaxException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL packageURL;
        ArrayList<String> names = new ArrayList<>();

        packageName = packageName.replace(".", "/");
        packageURL = classLoader.getResource(packageName);

        if(packageURL != null) {
            if (packageURL.getProtocol().equals("jar")) {
                String jarFileName;
                JarFile jf;
                Enumeration<JarEntry> jarEntries;
                String entryName;

                jarFileName = URLDecoder.decode(packageURL.getFile(), "UTF-8");
                jarFileName = jarFileName.substring(5, jarFileName.indexOf("!"));
                //System.out.println(">" + jarFileName);
                jf = new JarFile(jarFileName);
                jarEntries = jf.entries();
                while (jarEntries.hasMoreElements()) {
                    entryName = jarEntries.nextElement().getName();
                    if (entryName.startsWith(packageName) && entryName.length() > packageName
                        .length() + 5) {
                        entryName = entryName.substring(
                            packageName.length(), entryName.lastIndexOf('.')
                        );
                        names.add(entryName);
                    }
                }

            } else {
                URI uri = new URI(packageURL.toString());
                java.io.File folder = new java.io.File(uri.getPath());
                // TODO Rename local variable - contenuti
                // What does it means?
                java.io.File[] contenuti = folder.listFiles();
                String entryName;
                if (contenuti != null) {
                    for (java.io.File actual : contenuti) {
                        entryName = actual.getName();
                        if (entryName.lastIndexOf('.') != -1) {
                            entryName = entryName.substring(0, entryName.lastIndexOf('.'));
                            names.add(packageName.replace("/", ".") + "." + entryName);
                        } else {
                            ArrayList<String> ret = getClassNamesFromPackage(
                                packageName.replace("/", ".") + "." + entryName
                            );
                            names.addAll(ret);
                        }

                    }
                }
            }
        }
        return names;
    }

    public static void setField(Object object, String fieldName, Object value) throws IllegalAccessException, ReflectionException {
        boolean noField = true;
        for (Field field : object.getClass().getDeclaredFields()) {
            if (field.getName().equals(fieldName)) {
                field.setAccessible(true);
                field.set(object, value);
                noField = false;
            }
        }
        if (noField) {
            throw new ReflectionException("No field");
        }
    }

    public static Object getFieldValue(Object object, String fieldName) throws IllegalAccessException, ReflectionException {
        for (Field field : object.getClass().getDeclaredFields()) {
            if (field.getName().equals(fieldName)) {
                field.setAccessible(true);
                return field.get(object);

            }
        }
        throw new ReflectionException("No field");
    }

    public static String stackTraceToString(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
}
