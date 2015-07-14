package com.igumnov.common;


import java.util.Random;

public class Number {
    private static Random randomGenerator = new Random();

    public static int randomIntByRange(int fromValue, int toValue) {

        int bound = toValue - fromValue;
        return randomGenerator.nextInt(Math.abs(bound)) + fromValue;

    }


    public static double randomDoubleByRange(double fromValue, double toValue) {
        double bound = toValue - fromValue;
        return randomGenerator.nextDouble() * Math.abs(bound) + fromValue;
    }

    public static long randomLongByRange(long fromValue, long toValue) {
        long bound = toValue - fromValue;
        return (long) (randomGenerator.nextDouble() * Math.abs(bound)) + fromValue;
    }


    public static String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for (byte b : a)
            sb.append(String.format("%02x", b & 0xff));
        return sb.toString();
    }
}
