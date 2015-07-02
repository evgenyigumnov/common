package com.igumnov.common;

import java.io.*;
import java.util.Base64;
import java.util.stream.Stream;

public class Strings {
    public static Stream<Character> stream(String s) {
        return s.chars().mapToObj(i -> (char) i);
    }

    /**
     * Read the object from Base64 string.
     */
    public static Object deserializeObject(String objectString) throws IOException, ClassNotFoundException {
        byte[] data = Base64.getDecoder().decode(objectString);
        ObjectInputStream ois = new ObjectInputStream(
                new ByteArrayInputStream(data));
        Object o = ois.readObject();
        ois.close();
        return o;
    }

    /**
     * Write the object to a Base64 string.
     */
    public static String serializeObject(Serializable o) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(o);
        oos.close();
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

}
