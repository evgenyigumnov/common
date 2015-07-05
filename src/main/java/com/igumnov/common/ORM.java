package com.igumnov.common;

import com.igumnov.common.dependency.DependencyException;
import com.igumnov.common.orm.DDLHistory;
import com.igumnov.common.orm.Transaction;
import com.igumnov.common.reflection.ReflectionException;
import org.apache.commons.dbcp2.BasicDataSource;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class ORM {
    private static BasicDataSource ds;

    public static void connectionPool(String driverClass, String url, String user, String password, int minPoolSize, int maxPoolSize) throws DependencyException {
        ds = new BasicDataSource();
        ds.setDriverClassName(driverClass);
        ds.setUrl(url);
        ds.setUsername(user);
        ds.setPassword(password);
        ds.setInitialSize(minPoolSize);
        ds.setMaxTotal(maxPoolSize);
        Dependency.bind("dataSource", ds);

    }

    public static void applyDDL(String sqlFolder) throws SQLException, IOException, ReflectionException, IllegalAccessException, InstantiationException {
        Connection con = null;
        int i = 1;
        ResultSet tables = null;
        try {
            con = ds.getConnection();
            DatabaseMetaData dbm = con.getMetaData();
            tables = dbm.getTables(null, null, "DDLHISTORY", null);

            if (tables.next()) {
                ArrayList<Object> ret = findBy("true order by id desc limit 1", DDLHistory.class);
                i = 1 + ((DDLHistory) ret.get(0)).getId();
            } else {
                ResultSet tables2 = null;
                try {
                    tables2 = dbm.getTables(null, null, "DDLHistory", null);
                    if (tables2.next()) {
                        ArrayList<Object> ret = findBy("true order by id desc limit 1", DDLHistory.class);
                        i = 1 + ((DDLHistory) ret.get(0)).getId();
                    } else {
                        Statement stmt = null;
                        try {
                            stmt = con.createStatement();
                            String sql = "CREATE TABLE DDLHistory (id INT PRIMARY KEY, applyDate DATE)";
                            stmt.execute(sql);
                            Log.debug(sql);
                        } finally {
                            if (stmt != null) {
                                stmt.close();
                            }
                        }
                    }

                } finally {
                    if (tables2 != null) tables.close();
                }
            }

            tables.close();

        } finally {
            if (con != null) con.close();
            if (tables != null) tables.close();

        }

        try {
            for (; true; i++) {
                Connection c = ds.getConnection();
                try {
                    c.setAutoCommit(false);
                    File.readLines(sqlFolder + "/" + i + ".sql").forEach((line) -> {
                        Statement stmt = null;
                        try {
                            stmt = c.createStatement();
                            Log.debug(line);
                            stmt.execute(line);
                        } catch (SQLException e) {
                            Log.error("SQL error: ", e);
                        } finally {
                            try {
                                if (stmt != null) stmt.close();
                            } catch (Exception e) {
                                Log.error("SQL error: ", e);
                            }
                        }
                    });
                    c.commit();
                    DDLHistory ddl = new DDLHistory();
                    ddl.setId(i);
                    ddl.setApplyDate(new java.util.Date());
                    insert(ddl);
                } finally {
                    if (c != null) {
                        c.setAutoCommit(true);
                        c.close();
                    }
                }
            }
        } catch (NoSuchFileException e) {
            //It is last sql file
        }

    }

    public static Transaction beginTransaction() throws SQLException {
        return new Transaction(ds.getConnection());
    }


    public static Object update(Object obj) throws IllegalAccessException, SQLException {

        Object ret;
        Transaction tx = ORM.beginTransaction();
        ret = tx.update(obj);
        tx.commit();
        return ret;

    }


    public static Object insert(Object obj) throws IllegalAccessException, SQLException, ReflectionException {
        Object ret;
        Transaction tx = ORM.beginTransaction();
        ret = tx.insert(obj);
        tx.commit();
        return ret;

    }

    public static ArrayList<Object> findBy(String where, Class classObject, Object... params) throws SQLException, IllegalAccessException, InstantiationException, ReflectionException, IOException {
        ArrayList<Object> ret;
        Transaction tx = ORM.beginTransaction();
        ret = tx.findBy(where, classObject, params);
        tx.commit();
        return ret;
    }

    public static Object findOne(Class className, Object primaryKey) throws SQLException, ReflectionException, InstantiationException, IllegalAccessException, IOException {
        Object ret;
        Transaction tx = ORM.beginTransaction();
        ret = tx.findOne(className, primaryKey);
        tx.commit();
        return ret;
    }

    public static int deleteBy(String where, Class classObject, Object... params) throws SQLException {
        int ret;
        Transaction tx = ORM.beginTransaction();
        ret = tx.deleteBy(where, classObject, params);
        tx.commit();
        return ret;
    }

    public static ArrayList<HashMap<String, Object>> selectQuery(String sqlQuery, Object... params) throws SQLException {
        ArrayList<HashMap<String, Object>> ret;
        Transaction tx = ORM.beginTransaction();
        ret = tx.selectQuery(sqlQuery, params);
        tx.commit();
        return ret;
    }


    public static int delete(Object obj) throws IllegalAccessException, SQLException {
        int ret;
        Transaction tx = ORM.beginTransaction();
        ret = tx.delete(obj);
        tx.commit();
        return ret;
    }

    public static ArrayList<Object> findAll(Class classObject) throws SQLException, ReflectionException, InstantiationException, IllegalAccessException, IOException {

        ArrayList<Object> ret;
        Transaction tx = ORM.beginTransaction();
        ret = tx.findAll(classObject);
        tx.commit();
        return ret;
    }


}
