package com.igumnov.common;


import org.junit.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class CyphersTest {

    @Test
    public void testAES() throws NoSuchPaddingException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException {
        String crypted = Cyphers.encryptAES("test", "pwd");
        assertEquals("49f4fa67dd7b9bc647ad60028e241a25", crypted);
        crypted = Cyphers.encryptAES("test1", "pwd");
        assertNotEquals("49f4fa67dd7b9bc647ad60028e241a25", crypted);
        String uncrypted = Cyphers.decryptAES("49f4fa67dd7b9bc647ad60028e241a25","pwd");
        assertEquals("test", uncrypted);
        try {
            uncrypted = Cyphers.decryptAES("49f4fa67dd7b9bc647ad60028e241a25","pwd1");
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(true);
        }
    }
}
