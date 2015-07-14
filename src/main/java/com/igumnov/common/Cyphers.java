package com.igumnov.common;


import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

public class Cyphers {

    public static String md5(String src) {

        MessageDigest messageDigest = null;

        try {
            messageDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            Log.error(e.getMessage(), e);
        }

        messageDigest.reset();
        messageDigest.update(src.getBytes());
        final byte[] resultByte = messageDigest.digest();
        return Number.byteArrayToHex(resultByte);
    }


    public static String encryptAES(String value, String password) throws UnsupportedEncodingException, NoSuchPaddingException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        SecretKey key = new SecretKeySpec(safePassword(password).getBytes("UTF-8"), "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding", "SunJCE");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encrypted = cipher.doFinal(value.getBytes("UTF-8"));
        return hex(encrypted);
    }



    public static String decryptAES(String value, String password) throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
        byte[] encypted = fromHex(value);
        SecretKey key = new SecretKeySpec(safePassword(password).getBytes("UTF-8"), "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding", "SunJCE");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decrypted = cipher.doFinal(encypted);
        return new String(decrypted, "UTF-8");
    }


    private static String safePassword(String unsafe) {
        String safe = unsafe;
        if (safe.length() > 16) {
            safe = safe.substring(0, 16);
        }
        int nn = safe.length();
        for (int i = nn - 1; i < 15; i++) {
            safe = safe + "*";
        }
        return safe;
    }


    private static String hex(byte[] bytes) {
        String r = "";
        for (int i = 0; i < bytes.length; i++) {
            r = r + pad2(Integer.toHexString(bytes[i] + 128));
        }
        return r;
    }

    private static byte[] fromHex(String enc) {
        byte[] r = new byte[enc.length() / 2];
        for (int i = 0; i < r.length; i++) {
            int n = parseInt2(enc.substring(i * 2, i * 2 + 2)) - 128;
            r[i] = (byte) n;
        }
        return r;
    }


    private static String pad2(String n) {
        if (n.length() < 2) {
            return "0" + n;
        } else {
            return n;
        }
    }

    private static int parseInt2(String s) {
        return (new java.math.BigInteger(s, 16)).intValue();
    }

}
