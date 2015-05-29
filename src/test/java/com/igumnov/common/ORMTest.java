package com.igumnov.common;


import com.igumnov.common.dependency.DependencyException;
import com.igumnov.common.orm.Transaction;
import com.igumnov.common.reflection.ReflectionException;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;


public class ORMTest {

    @Before
    public void before() {
        java.io.File d = new java.io.File("tmp");
        if (!d.exists()) {
            d.mkdir();
        } else {
            Folder.deleteWithContent("tmp");
            d.mkdir();
        }
        Log.setLogLevel(Log.DEBUG);
    }

    @Test
    public void testORM() throws IOException, SQLException, IllegalAccessException, ReflectionException, InstantiationException, DependencyException {
        new java.io.File("tmp/sql_folder").mkdir();

        ORM.connectionPool("org.h2.Driver", "jdbc:h2:mem:test", "SA", "", 10, 30);

        File.appendLine("CREATE TABLE ObjectDTO (id BIGINT AUTO_INCREMENT PRIMARY KEY)", "tmp/sql_folder/1.sql");
        File.appendLine("ALTER TABLE ObjectDTO ADD name VARCHAR(255)", "tmp/sql_folder/1.sql");
        ORM.applyDDL("tmp/sql_folder");

        File.appendLine("ALTER TABLE ObjectDTO ADD salary INT", "tmp/sql_folder/2.sql");
        ORM.applyDDL("tmp/sql_folder");



        //Transaction mode
        Transaction tx = null;
        try {
            tx = ORM.beginTransaction();
            ObjectDTO obj = new ObjectDTO();
            obj.setName("aaa");
            obj.setSalary(111);
            obj = (ObjectDTO) tx.insert(obj);
            ObjectDTO object1 = (ObjectDTO) tx.findOne(ObjectDTO.class, 1L);
            assertEquals(object1.getName(), "aaa");
            obj.setName("ccc");
            obj.setSalary(333);
            obj = (ObjectDTO) tx.update(obj);
            ObjectDTO object2 = (ObjectDTO) tx.findOne(ObjectDTO.class, 1L);
            assertEquals(object2.getName(), "ccc");
        } finally {
            if (tx != null) {
                tx.commit(); 
            }
        }


        try {
            tx = ORM.beginTransaction();
            ArrayList<Object> objectsFind = tx.findBy("id > ?", ObjectDTO.class, 0L);
            assertEquals(objectsFind.size(), 1);
            int affectedRecords = tx.deleteBy("id > ?", ObjectDTO.class, 0L);
            assertEquals(affectedRecords,1);
            ArrayList<Object> objectsFind2 = tx.findBy("id > ?", ObjectDTO.class, 0L);
            assertEquals(objectsFind2.size(), 0);

        } finally {
            tx.commit();
        }


        try {
            tx = ORM.beginTransaction();
            ObjectDTO o = new ObjectDTO();
            o.setName("2");
            o.setSalary(0);
            o = (ObjectDTO) tx.insert(o);
            ArrayList<Object> objectsFind = tx.findAll(ObjectDTO.class);
            assertEquals(objectsFind.size(), 1);
            int affectedRecords = tx.delete(o);
            assertEquals(affectedRecords,1);
            ArrayList<Object> objectsFind2 = tx.findBy("id > ?", ObjectDTO.class, 0L);
            assertEquals(objectsFind2.size(), 0);

        } finally {
            tx.commit();
        }


        try {
            tx = ORM.beginTransaction();
            ObjectDTO o = new ObjectDTO();
            o.setName("2");
            o.setSalary(0);
            o = (ObjectDTO) tx.insert(o);
            ArrayList<Object> objectsFind = tx.findAll(ObjectDTO.class);
            assertEquals(objectsFind.size(), 1);


        } finally {
            tx.rollback();
        }

        try {
            tx = ORM.beginTransaction();
            ArrayList<Object> objectsFind = tx.findAll(ObjectDTO.class);
            assertEquals(objectsFind.size(), 0);


        } finally {
            tx.commit();
        }

        // Autocommit mode

        ObjectDTO test1 = new ObjectDTO();
        test1.setName("a");
        test1.setSalary(1);
        test1 = (ObjectDTO )ORM.insert(test1);
        ObjectDTO test2 = (ObjectDTO)ORM.findOne(ObjectDTO.class, test1.getId());
        assertEquals(test1.getId(), test2.getId());
        assertEquals(test2.getName(), "a");
        test1.setName("b");
        ORM.update(test1);
        test2 = (ObjectDTO)ORM.findOne(ObjectDTO.class, test1.getId());
        assertEquals(test2.getName(), "b");


    }


}
