package com.igumnov.common.orm;


import com.igumnov.common.Log;
import com.igumnov.common.Reflection;
import com.igumnov.common.reflection.ReflectionException;

import java.io.IOException;
import java.io.Reader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class Transaction {
    private Connection connection;

    public Transaction(Connection c) throws SQLException {
        connection = c;
        c.setAutoCommit(false);

    }

    public void commit() throws SQLException {
        try {
            connection.commit();
        } finally {
            try {
                connection.setAutoCommit(false);
            } finally {
                connection.close();
            }
        }
    }

    public void rollback() throws SQLException {
        try {
            connection.rollback();
        } finally {
            try {
                connection.setAutoCommit(false);
            } finally {
                connection.close();
            }
        }
    }



    public Object update(Object obj) throws IllegalAccessException, SQLException {


        LinkedHashMap<String, Object> fields = new LinkedHashMap<>();
        String pkField = null;
        Object pkFieldValue = null;
        for (Field field : obj.getClass().getDeclaredFields()) {
            if(!java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                boolean noAnnotation = true;
                for (Annotation annotation : field.getDeclaredAnnotations())
                    if (annotation.annotationType().equals(Id.class)) {
                        noAnnotation = false;
                        pkField = field.getName();
                        field.setAccessible(true);
                        pkFieldValue = field.get(obj);
                    }

                if (noAnnotation) {
                    field.setAccessible(true);
                    fields.put(field.getName(), field.get(obj));
                }
            }
        }

        Set<String> fieldsSet = fields.keySet();
        String names = fieldsSet.stream().collect(Collectors.joining("=?,", "", "=?"));

        String sql = "update  " + obj.getClass().getSimpleName() + " set " + names + " where " + pkField + "=?";

//        System.out.println(sql);

        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement(sql);


            Iterator<String> it = fieldsSet.iterator();
            int i = 1;
            while (it.hasNext()) {
                Object value = fields.get(it.next());
                preparedStatement.setObject(i, value);
//                System.out.println(value);
                ++i;
            }
            preparedStatement.setObject(i, pkFieldValue);
//            System.out.println(pkFieldValue);
            Log.debug(sql);
            preparedStatement.executeUpdate();


        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            } catch (Exception e) {
                /* ignore */
            }
        }
        return obj;


    }




    public Object insert(Object obj) throws IllegalAccessException, SQLException, ReflectionException {

        LinkedHashMap<String, Object> fields = new LinkedHashMap<>();
        boolean autoGenerated = false;
        String autoGeneratedField = null;
        for (Field field : obj.getClass().getDeclaredFields()) {
            if(!java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                boolean noAnnotation = true;
                for (Annotation annotation : field.getDeclaredAnnotations())
                    if (annotation.annotationType().equals(Id.class)) {
                        Boolean autoIncremental;
                        if(Reflection.getFieldValue(obj,field.getName()) == null) {
                            autoIncremental = ((Id) annotation).autoIncremental();
                        } else {
                            autoIncremental = false;
                        }
                        if (autoIncremental) {
                            noAnnotation = false;
                            autoGenerated = true;
                            autoGeneratedField = field.getName();
                        }
                    }

                if (noAnnotation) {
                    field.setAccessible(true);
                    fields.put(field.getName(), field.get(obj));
                }
            }
        }

        Set<String> fieldsSet = fields.keySet();
        String names = fieldsSet.stream().collect(Collectors.joining(","));
        String values = fieldsSet.stream().map(it -> "?").collect(Collectors.joining(","));

        String sql = "insert into " + obj.getClass().getSimpleName() + "(" + names + ") values (" + values + ")";
        PreparedStatement preparedStatement = null;
        try {
            if (!autoGenerated) {
                preparedStatement = connection.prepareStatement(sql);
            } else {
                preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            }


            Iterator<String> it = fieldsSet.iterator();
            int i = 1;
            while (it.hasNext()) {
                preparedStatement.setObject(i, fields.get(it.next()));
                ++i;
            }
            Log.debug(sql);
            preparedStatement.executeUpdate();

            if (autoGenerated) {
                ResultSet tableKeys = preparedStatement.getGeneratedKeys();
                tableKeys.next();
                Object autoGeneratedID = tableKeys.getObject(1);
                Reflection.setField(obj, autoGeneratedField, autoGeneratedID);
            }
        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            } catch (Exception e) {
                /* ignore */
            }
        }
        return obj;
    }

    public ArrayList<Object> findBy(String where, Class classObject, Object... params) throws SQLException, IllegalAccessException, InstantiationException, ReflectionException, IOException {
        ArrayList<Object> ret = new ArrayList<>();
        String names = "";
        for (Field field : classObject.getDeclaredFields()) {
            if(!java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                field.setAccessible(true);
                if (names.length() > 0) {
                    names = names + ",";
                }
                names = names + field.getName();
            }
        }


        String sql;
        if (where == null) {
            sql = "select " + names + " from  " + classObject.getSimpleName();
        } else {
            sql = "select " + names + " from  " + classObject.getSimpleName() + " where " + where;
        }
        //System.out.println(sql);
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement(sql);
            int i = 1;
            for (Object param : params) {
                preparedStatement.setObject(i, param);
                ++i;
            }
            Log.debug(sql);
            ResultSet r = preparedStatement.executeQuery();
            while (r.next()) {
                Object row = classObject.newInstance();
                for (Field field : classObject.getDeclaredFields()) {
                    if(!java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                        field.setAccessible(true);
                        Object value = r.getObject(field.getName());
                        if(value instanceof java.sql.Clob) {

                            Reader reader = ((Clob) value).getCharacterStream();

                            int intValueOfChar;
                            String targetString = "";
                            while ((intValueOfChar = reader.read()) != -1) {
                                targetString += (char) intValueOfChar;
                            }
                            reader.close();

                            Reflection.setField(row, field.getName(), targetString);
                        } else {
                            Reflection.setField(row, field.getName(), value);
                        }
                    }
                }
                ret.add(row);
            }

            r.close();

        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            } catch (Exception e) {
                /* ignore */
            }

        }

        return ret;
    }

    public Object findOne(Class className, Object primaryKey) throws SQLException, ReflectionException, InstantiationException, IllegalAccessException, IOException {
        String pkName = null;
        for (Field field : className.getDeclaredFields()) {
            for (Annotation annotation : field.getDeclaredAnnotations())
                if (annotation.annotationType().equals(Id.class)) {
                    pkName = field.getName();
                    if(field.getType().getName().equals("java.lang.Long") && primaryKey instanceof String) {
                        ArrayList<Object> ret = findBy(pkName + "=?", className, Long.valueOf((String) primaryKey));
                        if(ret.size()>0) {
                            return ret.get(0);
                        } else {
                            return null;
                        }
                    }
                    if(field.getType().getName().equals("java.lang.Double") && primaryKey instanceof String) {
                        ArrayList<Object> ret = findBy(pkName + "=?", className, Double.valueOf((String) primaryKey));
                        if(ret.size()>0) {
                            return ret.get(0);
                        } else {
                            return null;
                        }
                    }
                    if(field.getType().getName().equals("java.lang.Float") && primaryKey instanceof String) {
                        ArrayList<Object> ret = findBy(pkName + "=?",className, Float.valueOf((String)primaryKey));
                        if(ret.size()>0) {
                            return ret.get(0);
                        } else {
                            return null;
                        }
                    }
                    if(field.getType().getName().equals("java.lang.Integer") && primaryKey instanceof String) {
                        ArrayList<Object> ret = findBy(pkName + "=?",className, Integer.valueOf((String)primaryKey));
                        if(ret.size()>0) {
                            return ret.get(0);
                        } else {
                            return null;
                        }
                    }


                    ArrayList<Object> ret = findBy(pkName + "=?", className, primaryKey);
                    if(ret.size()>0) {
                        return ret.get(0);
                    } else {
                        return null;
                    }

                }
        }
        throw new ReflectionException("Cant find PK attribute");
    }

    public int deleteBy(String where, Class classObject, Object... params) throws SQLException {



        String sql = "delete from " + classObject.getSimpleName() + " where " + where;

//        System.out.println(sql);

        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement(sql);


            int i = 1;
            for (Object param : params) {
                preparedStatement.setObject(i, param);
                ++i;
            }

            Log.debug(sql);
            return preparedStatement.executeUpdate();


        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            } catch (Exception e) {
                /* ignore */
            }
        }


    }

    public int delete(Object obj) throws IllegalAccessException, SQLException {

        String pkName = null;
        Object pkValue = null;
        for (Field field : obj.getClass().getDeclaredFields()) {
            for (Annotation annotation : field.getDeclaredAnnotations())
                if (annotation.annotationType().equals(Id.class)) {
                    pkName = field.getName();
                    field.setAccessible(true);
                    pkValue = field.get(obj);
                }
        }
        return deleteBy(pkName+"=?", obj.getClass(), pkValue);
    }

    public ArrayList<Object> findAll(Class classObject) throws SQLException, ReflectionException, InstantiationException, IllegalAccessException, IOException {
        return findBy(null, classObject);
    }
}
